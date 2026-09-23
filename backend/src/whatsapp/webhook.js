/**
 * BEKANSI AI SALES PLATFORM - WHATSAPP/WEBHOOK.JS
 * Express router handling Meta WhatsApp Webhook events and simulation endpoints
 */

const express = require('express');
const router = express.Router();
const env = require('../config/env');
const logger = require('../config/logger');
const whatsappService = require('./whatsapp.service');
const agent = require('../ai/agent');
const customerCrm = require('../crm/customers');
const conversationCrm = require('../crm/conversations');
const leadCrm = require('../crm/leads');
const productService = require('../products/products');
const pricingService = require('../products/pricing');
const toolsDispatcher = require('../ai/tools');

/**
 * GET /webhook
 * Meta Webhook Verification
 */
router.get('/', (req, res) => {
    const mode = req.query['hub.mode'];
    const token = req.query['hub.verify_token'];
    const challenge = req.query['hub.challenge'];

    if (mode && token) {
        if (mode === 'subscribe' && token === env.whatsapp.verifyToken) {
            logger.info('WhatsApp Webhook successfully verified with Meta');
            return res.status(200).send(challenge);
        } else {
            logger.warn('WhatsApp Webhook verification failed - token mismatch', { providedToken: token });
            return res.sendStatus(403);
        }
    }
    return res.status(400).json({ error: 'Missing hub.mode or hub.verify_token' });
});

/**
 * POST /webhook
 * Meta Incoming Webhook Events
 */
router.post('/', async (req, res) => {
    const body = req.body;

    // Immediately respond 200 OK to Meta to prevent retry loops
    res.status(200).send('EVENT_RECEIVED');

    if (body.object === 'whatsapp_business_account' || body.entry) {
        try {
            for (const entry of body.entry || []) {
                for (const change of entry.changes || []) {
                    const value = change.value;
                    if (!value || !value.messages || value.messages.length === 0) continue;

                    const contact = (value.contacts && value.contacts[0]) || {};
                    const customerName = contact.profile ? contact.profile.name : 'Valued Customer';
                    const messageObj = value.messages[0];
                    const fromPhone = messageObj.from;
                    const messageId = messageObj.id;
                    const messageType = messageObj.type;

                    let incomingText = '';
                    if (messageType === 'text') {
                        incomingText = messageObj.text.body;
                    } else if (messageType === 'interactive') {
                        incomingText = (messageObj.interactive.button_reply && messageObj.interactive.button_reply.title) ||
                                       (messageObj.interactive.list_reply && messageObj.interactive.list_reply.title) || '';
                    } else {
                        incomingText = `[Received ${messageType} attachment]`;
                    }

                    logger.info(`Received WhatsApp message from ${fromPhone}: "${incomingText}"`);
                    await whatsappService.markMessageAsRead(messageId);

                    // 1. Sync Customer in CRM
                    const customer = await customerCrm.upsertCustomer({
                        fullName: customerName,
                        phone: fromPhone,
                        source: 'WHATSAPP'
                    });

                    // 2. Sync Conversation in CRM
                    const conversation = await conversationCrm.getOrCreateConversation({
                        customerId: customer.id,
                        whatsappPhone: fromPhone
                    });

                    // 3. Record Inbound Message
                    await conversationCrm.recordMessage({
                        conversationId: conversation.id,
                        whatsappMessageId: messageId,
                        direction: 'inbound',
                        senderType: 'customer',
                        messageType,
                        text: incomingText
                    });

                    // 4. Process with Bekansi AI Agent
                    const agentResult = await agent.processCustomerMessage({
                        customerPhone: fromPhone,
                        customerName,
                        message: incomingText,
                        customerId: customer.id,
                        conversationId: conversation.id
                    });

                    // 5. Send Outbound WhatsApp Message
                    await whatsappService.sendTextMessage(fromPhone, agentResult.agentReply);

                    // 6. Record Outbound AI Message
                    await conversationCrm.recordMessage({
                        conversationId: conversation.id,
                        direction: 'outbound',
                        senderType: 'ai',
                        messageType: 'text',
                        text: agentResult.agentReply,
                        aiGenerated: true,
                        aiModel: env.ai.model,
                        aiConfidence: 0.98,
                        toolCalled: agentResult.toolsInvoked.join(', '),
                        deliveryStatus: 'sent'
                    });
                }
            }
        } catch (err) {
            logger.error('Error handling incoming WhatsApp webhook event', { error: err.message });
        }
    }
});

/**
 * POST /webhook/simulate
 * Interactive test harness for simulating WhatsApp conversations
 */
router.post('/simulate', async (req, res) => {
    const { customerPhone = '+251911223344', customerName = 'Sara Tesfaye', message = 'Hi, I need a luxury king bed' } = req.body;

    try {
        // 1. Customer
        const customer = await customerCrm.upsertCustomer({
            fullName: customerName,
            phone: customerPhone,
            source: 'SIMULATOR'
        });

        // 2. Conversation
        const conversation = await conversationCrm.getOrCreateConversation({
            customerId: customer.id,
            whatsappPhone: customerPhone
        });

        // 3. Inbound
        await conversationCrm.recordMessage({
            conversationId: conversation.id,
            direction: 'inbound',
            senderType: 'customer',
            text: message
        });

        // 4. Agent
        const agentResult = await agent.processCustomerMessage({
            customerPhone,
            customerName,
            message,
            customerId: customer.id,
            conversationId: conversation.id
        });

        // 5. Outbound
        await conversationCrm.recordMessage({
            conversationId: conversation.id,
            direction: 'outbound',
            senderType: 'ai',
            text: agentResult.agentReply,
            aiGenerated: true,
            aiModel: env.ai.model,
            toolCalled: agentResult.toolsInvoked.join(', ')
        });

        return res.status(200).json({
            success: true,
            pipeline: {
                inbound: { customerPhone, customerName, message },
                toolsUsed: agentResult.toolsInvoked,
                leadStatus: agentResult.leadStatus,
                agentReply: agentResult.agentReply
            }
        });
    } catch (err) {
        logger.error('Error in /webhook/simulate', { error: err.message });
        return res.status(500).json({ success: false, error: err.message });
    }
});

/**
 * GET /webhook/pipeline
 * Real-time monitoring of all pipeline entities
 */
router.get('/pipeline', async (req, res) => {
    const products = await productService.getAllProducts();
    const productVariants = await productService.getProductVariants({});
    const prices = pricingService.getAllPrices();
    const conversations = conversationCrm.getAllConversations();
    const messages = conversationCrm.getAllMessages().slice(0, 50);
    const leads = leadCrm.getAllLeads();
    const customers = customerCrm.getAllCustomers();
    const aiToolLogs = toolsDispatcher.getToolLogs();

    return res.status(200).json({
        success: true,
        data: {
            products,
            productVariants,
            prices,
            conversations,
            messages,
            leads,
            customers,
            aiToolLogs,
            timestamp: new Date().toISOString()
        }
    });
});

module.exports = router;
