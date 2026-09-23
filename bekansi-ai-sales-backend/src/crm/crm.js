/**
 * BEKANSI AI SALES PLATFORM - CRM/CRM.JS (ESM)
 * Unified CRM module managing Customers, Conversations, Messages, and Leads
 */

import { pool, supabase } from '../config/database.js';
import logger from '../config/logger.js';

// Resilient memory stores
const memoryCustomers = new Map();
const memoryConversations = new Map();
const memoryMessages = [];
const memoryLeads = new Map();

export const crm = {
    // -------------------------------------------------------------------------
    // CUSTOMER MANAGEMENT
    // -------------------------------------------------------------------------
    upsertCustomer: async ({
        fullName,
        phone,
        whatsappUserId = null,
        preferredLanguage = 'en',
        customerType = 'INDIVIDUAL',
        city = 'Addis Ababa',
        subCity = null,
        woreda = null,
        deliveryLocation = null,
        source = 'WHATSAPP',
        campaign = null,
        adId = null,
        status = 'ACTIVE'
    }) => {
        const cleanPhone = (phone || '').replace(/[^0-9+]/g, '');
        if (!cleanPhone) throw new Error('Phone number is required for customer record');

        let customer = memoryCustomers.get(cleanPhone);
        const customerId = customer ? customer.id : `CUST-${Date.now().toString().slice(-5)}`;

        const updated = {
            id: customerId,
            full_name: fullName || (customer ? customer.full_name : 'Valued Client'),
            phone: cleanPhone,
            whatsapp_user_id: whatsappUserId || cleanPhone,
            preferred_language: preferredLanguage || (customer ? customer.preferred_language : 'en'),
            customer_type: customerType,
            city: city || 'Addis Ababa',
            sub_city: subCity || (customer ? customer.sub_city : null),
            woreda: woreda || (customer ? customer.woreda : null),
            delivery_location: deliveryLocation || subCity || city,
            source,
            campaign,
            ad_id: adId,
            status,
            created_at: customer ? customer.created_at : new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryCustomers.set(cleanPhone, updated);

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO customers (
                        full_name, phone, whatsapp_user_id, preferred_language,
                        customer_type, city, sub_city, woreda, delivery_location,
                        source, campaign, ad_id, status, created_at, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, NOW(), NOW())
                    ON CONFLICT (phone) 
                    DO UPDATE SET 
                        full_name = COALESCE(EXCLUDED.full_name, customers.full_name),
                        city = COALESCE(EXCLUDED.city, customers.city),
                        sub_city = COALESCE(EXCLUDED.sub_city, customers.sub_city),
                        delivery_location = COALESCE(EXCLUDED.delivery_location, customers.delivery_location),
                        updated_at = NOW()
                `, [
                    updated.full_name, updated.phone, updated.whatsapp_user_id, updated.preferred_language,
                    updated.customer_type, updated.city, updated.sub_city, updated.woreda,
                    updated.delivery_location, updated.source, updated.campaign, updated.ad_id, updated.status
                ]);
            } catch (dbErr) {
                logger.warn('Could not persist customer to Postgres, kept in memory', { error: dbErr.message });
            }
        }

        return updated;
    },

    getCustomerByPhone: async (phone) => {
        const cleanPhone = (phone || '').replace(/[^0-9+]/g, '');
        if (pool) {
            try {
                const res = await pool.query('SELECT * FROM customers WHERE phone = $1', [cleanPhone]);
                if (res.rows && res.rows.length > 0) return res.rows[0];
            } catch (err) {
                logger.warn('Failed to query customer by phone from Postgres', { error: err.message });
            }
        }
        return memoryCustomers.get(cleanPhone) || null;
    },

    getAllCustomers: () => Array.from(memoryCustomers.values()),

    // -------------------------------------------------------------------------
    // CONVERSATION & MESSAGE MANAGEMENT
    // -------------------------------------------------------------------------
    getOrCreateConversation: async ({
        customerId,
        whatsappPhone,
        channel = 'WHATSAPP',
        language = 'en',
        leadStage = 'NEW',
        leadScore = 0
    }) => {
        const cleanPhone = (whatsappPhone || '').replace(/[^0-9+]/g, '');
        let conv = memoryConversations.get(cleanPhone);

        if (!conv) {
            const convId = `CONV-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
            conv = {
                id: convId,
                customer_id: customerId,
                whatsapp_phone: cleanPhone,
                channel,
                status: 'open',
                stage: leadStage,
                lead_score: leadScore,
                assigned_agent_id: null,
                ai_handled: true,
                language,
                unread_count: 0,
                last_message_at: new Date().toISOString(),
                created_at: new Date().toISOString(),
                updated_at: new Date().toISOString()
            };
            memoryConversations.set(cleanPhone, conv);

            if (pool) {
                try {
                    await pool.query(`
                        INSERT INTO conversations (
                            customer_id, whatsapp_phone, channel, status, stage,
                            lead_score, ai_handled, language, unread_count, last_message_at, created_at, updated_at
                        )
                        VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW(), NOW())
                    `, [
                        customerId, cleanPhone, channel, 'open', leadStage,
                        leadScore, true, language, 0
                    ]);
                } catch (err) {
                    logger.warn('Could not insert conversation into Postgres, stored in memory', { error: err.message });
                }
            }
        } else {
            conv.last_message_at = new Date().toISOString();
            conv.updated_at = new Date().toISOString();
        }

        return conv;
    },

    recordMessage: async ({
        conversationId,
        whatsappMessageId = null,
        direction = 'inbound',
        senderType = 'customer',
        messageType = 'text',
        text = '',
        mediaId = null,
        mediaUrl = null,
        mimeType = null,
        intent = null,
        aiGenerated = false,
        aiModel = null,
        aiConfidence = null,
        toolCalled = null,
        toolArguments = null,
        toolResult = null,
        deliveryStatus = 'received'
    }) => {
        const msgId = `MSG-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
        const msgRecord = {
            id: msgId,
            conversation_id: conversationId,
            whatsapp_message_id: whatsappMessageId || `wam_${Date.now()}`,
            direction,
            sender_type: senderType,
            message_type: messageType,
            text,
            media_id: mediaId,
            media_url: mediaUrl,
            mime_type: mimeType,
            intent,
            ai_generated: aiGenerated,
            ai_model: aiModel,
            ai_confidence: aiConfidence,
            tool_called: toolCalled,
            tool_arguments: toolArguments,
            tool_result: toolResult,
            delivery_status: deliveryStatus,
            created_at: new Date().toISOString()
        };

        memoryMessages.unshift(msgRecord);
        if (memoryMessages.length > 500) memoryMessages.pop();

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO messages (
                        conversation_id, whatsapp_message_id, direction, sender_type,
                        message_type, text, media_id, media_url, mime_type,
                        intent, ai_generated, ai_model, ai_confidence,
                        tool_called, tool_arguments, tool_result, delivery_status, created_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, NOW())
                `, [
                    conversationId, msgRecord.whatsapp_message_id, direction, senderType,
                    messageType, text, mediaId, mediaUrl, mimeType,
                    intent, aiGenerated, aiModel, aiConfidence,
                    toolCalled,
                    toolArguments ? JSON.stringify(toolArguments) : null,
                    toolResult ? JSON.stringify(toolResult) : null,
                    deliveryStatus
                ]);
            } catch (err) {
                logger.warn('Could not insert message into Postgres, stored in memory', { error: err.message });
            }
        }

        return msgRecord;
    },

    getAllConversations: () => Array.from(memoryConversations.values()),
    getAllMessages: () => memoryMessages,

    // -------------------------------------------------------------------------
    // LEADS & QUALIFICATION PIPELINE
    // -------------------------------------------------------------------------
    classifyAndSaveLead: async ({
        customerId,
        conversationId,
        customerPhone,
        customerName,
        productId = null,
        productCategory = null,
        productInterest = null,
        quantity = 1,
        size = null,
        color = null,
        fabric = null,
        finish = null,
        ledRequired = false,
        socketRequired = false,
        budgetMin = null,
        budgetMax = null,
        purchaseTimeline = null,
        leadStage = 'NEW',
        leadScore = null,
        leadTemperature = null,
        leadStatus = '🟡 Warm Lead',
        assignedTo = null,
        nextFollowupAt = null,
        missingInformation = []
    }) => {
        const cleanPhone = (customerPhone || '').replace(/[^0-9+]/g, '');
        const leadId = `LEAD-${Date.now().toString().slice(-6)}`;

        let temperature = 'WARM';
        if (leadTemperature) {
            temperature = leadTemperature.toUpperCase();
        } else if (typeof leadStatus === 'string') {
            if (leadStatus.includes('Hot') || leadStatus.includes('🔥')) temperature = 'HOT';
            else if (leadStatus.includes('Cold') || leadStatus.includes('⚪')) temperature = 'COLD';
            else temperature = 'WARM';
        }

        let score = leadScore;
        if (score === null || score === undefined) {
            if (temperature === 'HOT') score = 85;
            else if (temperature === 'WARM') score = 55;
            else score = 25;
        }

        const category = productCategory || productInterest || 'Beds';
        const timeline = purchaseTimeline || 'Within 2-4 weeks';

        const leadRecord = {
            id: leadId,
            customer_id: customerId,
            conversation_id: conversationId,
            product_id: productId,
            product_category: category,
            quantity: Number(quantity) || 1,
            size,
            color,
            fabric,
            finish,
            led_required: Boolean(ledRequired),
            socket_required: Boolean(socketRequired),
            budget_min: budgetMin,
            budget_max: budgetMax,
            purchase_timeline: timeline,
            lead_stage: leadStage || (temperature === 'HOT' ? 'QUALIFIED' : 'NEW'),
            lead_score: score,
            lead_temperature: temperature,
            assigned_to: assignedTo,
            next_followup_at: nextFollowupAt || new Date(Date.now() + 24 * 3600 * 1000).toISOString(),
            last_followup_at: null,
            lost_reason: null,
            customerPhone: cleanPhone,
            customerName: customerName || 'Prospective Client',
            leadStatus: temperature === 'HOT' ? '🔥 Hot Lead' : (temperature === 'WARM' ? '🟡 Warm Lead' : '⚪ Cold Lead'),
            missingInformation,
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryLeads.set(cleanPhone, leadRecord);

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO leads (
                        customer_id, conversation_id, product_id, product_category,
                        quantity, size, color, fabric, finish,
                        led_required, socket_required, budget_min, budget_max,
                        purchase_timeline, lead_stage, lead_score, lead_temperature,
                        assigned_to, next_followup_at, created_at, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, NOW(), NOW())
                `, [
                    leadRecord.customer_id, leadRecord.conversation_id, leadRecord.product_id, leadRecord.product_category,
                    leadRecord.quantity, leadRecord.size, leadRecord.color, leadRecord.fabric, leadRecord.finish,
                    leadRecord.led_required, leadRecord.socket_required, leadRecord.budget_min, leadRecord.budget_max,
                    leadRecord.purchase_timeline, leadRecord.lead_stage, leadRecord.lead_score, leadRecord.lead_temperature,
                    leadRecord.assigned_to, leadRecord.next_followup_at
                ]);
            } catch (dbErr) {
                logger.warn('Could not persist lead to Postgres, kept in memory', { error: dbErr.message });
            }
        }

        return leadRecord;
    },

    getAllLeads: () => Array.from(memoryLeads.values())
};

export default crm;
