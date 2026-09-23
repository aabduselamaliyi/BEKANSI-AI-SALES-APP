/**
 * ============================================================================
 * BEKANSI WHATSAPP CLOUD API WEBHOOK ROUTER
 * ============================================================================
 * Pipeline:
 * Customer -> WhatsApp -> Meta Cloud API -> Express Webhook ->
 * Supabase/Postgres CRM -> Gemini AI Agent -> Bekansi Tools -> WhatsApp Response
 */

const express = require('express');
const logger = require('../config/logger');
const whatsappService = require('../services/whatsappService');
const geminiAgentService = require('../services/geminiAgentService');
const crmToolsService = require('../services/crmToolsService');

const router = express.Router();

/**
 * GET /webhook - Meta Webhook Handshake Verification
 */
router.get('/', (req, res) => {
    return whatsappService.verifyWebhook(req, res);
});

/**
 * POST /webhook - Inbound Event from Meta WhatsApp Cloud API
 */
router.post('/', async (req, res) => {
    try {
        const parsed = whatsappService.parseInboundMessage(req.body);

        // Immediate 200 OK acknowledgement required by Meta
        res.status(200).json({ status: 'EVENT_RECEIVED' });

        if (!parsed || !parsed.textContent) {
            return;
        }

        const { senderPhone, senderName, textContent, messageId } = parsed;

        logger.info('Inbound WhatsApp message received for processing', {
            senderPhone,
            senderName,
            textContent
        });

        // Track Conversation lifecycle
        const conv = await crmToolsService.getOrCreateConversation({
            customerId: null,
            whatsappPhone: senderPhone,
            channel: 'whatsapp',
            leadStage: 'NEW'
        });

        // 1. Record Inbound Customer Message
        await crmToolsService.recordMessage({
            conversationId: conv ? conv.id : null,
            whatsappMessageId: messageId,
            direction: 'inbound',
            senderType: 'customer',
            messageType: 'text',
            text: textContent,
            deliveryStatus: 'received'
        });

        // Check if an active human handoff is currently assigned
        if (crmToolsService.isHandoffActive(senderPhone)) {
            logger.info('Customer is currently in active human handoff. Routing to showroom staff.', { senderPhone });
            return;
        }

        // Invoke Gemini AI Agent with Bekansi Tools
        const agentResult = await geminiAgentService.processCustomerMessage({
            customerPhone: senderPhone,
            customerName: senderName,
            messageText: textContent
        });

        // Dispatch outbound WhatsApp response to customer via Meta Cloud API
        await whatsappService.sendWhatsAppMessage(senderPhone, agentResult.agentReply);

        // 2. Record Outbound AI Message
        await crmToolsService.recordMessage({
            conversationId: conv ? conv.id : null,
            direction: 'outbound',
            senderType: 'ai',
            messageType: 'text',
            text: agentResult.agentReply,
            aiGenerated: true,
            aiModel: 'models/gemini-2.5-flash',
            aiConfidence: 0.98,
            toolCalled: agentResult.toolCalls && agentResult.toolCalls.length > 0 ? agentResult.toolCalls.join(', ') : null,
            deliveryStatus: 'sent'
        });

        // Update conversation state with AI timestamp and lead stage
        await crmToolsService.updateConversationAiMessage(senderPhone, agentResult.leadStatus);

    } catch (error) {
        logger.error('Error handling WhatsApp webhook event', { error: error.message, stack: error.stack });
    }
});

/**
 * POST /webhook/simulate
 * Convenient testing endpoint to trigger the entire pipeline without a live WhatsApp phone number
 */
router.post('/simulate', async (req, res) => {
    try {
        const { customerPhone = '+251911223344', customerName = 'Bole Client', message = 'ሰላም I want to buy a luxury sofa' } = req.body;

        logger.info('Simulated WhatsApp conversation event', { customerPhone, customerName, message });

        const conv = await crmToolsService.getOrCreateConversation({
            customerId: null,
            whatsappPhone: customerPhone,
            channel: 'whatsapp',
            leadStage: 'NEW'
        });

        await crmToolsService.recordMessage({
            conversationId: conv ? conv.id : null,
            whatsappMessageId: `sim_in_${Date.now()}`,
            direction: 'inbound',
            senderType: 'customer',
            messageType: 'text',
            text: message,
            deliveryStatus: 'received'
        });

        const agentResult = await geminiAgentService.processCustomerMessage({
            customerPhone,
            customerName,
            messageText: message
        });

        const dispatchResult = await whatsappService.sendWhatsAppMessage(customerPhone, agentResult.agentReply);

        await crmToolsService.recordMessage({
            conversationId: conv ? conv.id : null,
            whatsappMessageId: `sim_out_${Date.now()}`,
            direction: 'outbound',
            senderType: 'ai',
            messageType: 'text',
            text: agentResult.agentReply,
            aiGenerated: true,
            aiModel: 'models/gemini-2.5-flash',
            aiConfidence: 0.98,
            toolCalled: agentResult.toolCalls && agentResult.toolCalls.length > 0 ? agentResult.toolCalls.join(', ') : null,
            deliveryStatus: 'sent'
        });

        await crmToolsService.updateConversationAiMessage(customerPhone, agentResult.leadStatus);

        return res.status(200).json({
            success: true,
            pipeline: {
                inbound: { customerPhone, customerName, message },
                toolsUsed: agentResult.toolCalls,
                leadStatus: agentResult.leadStatus,
                outboundResponse: agentResult.agentReply,
                metaCloudApiStatus: dispatchResult
            }
        });
    } catch (err) {
        logger.error('Failed to execute simulation pipeline', { error: err.message });
        return res.status(500).json({ success: false, error: err.message });
    }
});

/**
 * GET /webhook/pipeline
 * Live summary of WhatsApp leads, CRM customers, conversations, messages, and active human handoffs
 */
router.get('/pipeline', (req, res) => {
    return res.status(200).json({
        success: true,
        data: {
            products: crmToolsService.getAllProducts(),
            productVariants: crmToolsService.getAllProductVariants(),
            prices: crmToolsService.getAllPrices(),
            conversations: crmToolsService.getAllConversations(),
            messages: crmToolsService.getAllMessages().slice(0, 30),
            leads: crmToolsService.getAllLeads(),
            customers: crmToolsService.getAllCustomers(),
            handoffTickets: crmToolsService.getAllHandoffs(),
            timestamp: new Date().toISOString()
        }
    });
});

/**
 * GET /webhook/logs
 * Live stream of AI tool calling logs (ai_tool_logs)
 */
router.get('/logs', (req, res) => {
    return res.status(200).json({
        success: true,
        data: {
            count: crmToolsService.getAllAiToolLogs().length,
            logs: crmToolsService.getAllAiToolLogs()
        }
    });
});

/**
 * POST /webhook/handoff/:phone/resolve
 * Showroom agent resolves human handoff ticket
 */
router.post('/handoff/:phone/resolve', (req, res) => {
    const { phone } = req.params;
    const resolved = crmToolsService.resolveHandoff(phone);
    return res.status(200).json({
        success: resolved,
        message: resolved ? `Handoff ticket for ${phone} resolved.` : `Ticket not found for ${phone}.`
    });
});

module.exports = router;
