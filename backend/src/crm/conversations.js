/**
 * BEKANSI AI SALES PLATFORM - CRM/CONVERSATIONS.JS
 * Manages the conversations and messages tables
 */

const { pool } = require('../config/database');
const logger = require('../config/logger');

const memoryConversations = new Map();
const memoryMessages = [];

const conversationCrm = {
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
        direction = 'inbound', // inbound | outbound
        senderType = 'customer', // customer | ai | human_agent | system
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
    getAllMessages: () => memoryMessages
};

module.exports = conversationCrm;
