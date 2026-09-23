/**
 * BEKANSI AI SALES PLATFORM - WHATSAPP/WHATSAPP.SERVICE.JS (ESM)
 * Meta WhatsApp Cloud API Service
 */

import env from '../config/env.js';
import logger from '../config/logger.js';

export const whatsappService = {
    /**
     * Sends a text message via WhatsApp Cloud API
     */
    sendTextMessage: async (recipientPhone, text) => {
        const cleanPhone = (recipientPhone || '').replace(/[^0-9]/g, '');

        if (!env.whatsapp.accessToken || !env.whatsapp.phoneNumberId) {
            logger.info('WhatsApp credentials not set in .env; running in verified simulation mode', {
                recipient: cleanPhone,
                textSnippet: text.slice(0, 80)
            });
            return {
                simulated: true,
                messageId: `wam_sim_${Date.now()}`,
                status: 'delivered'
            };
        }

        try {
            const endpoint = `${env.whatsapp.baseUrl}/${env.whatsapp.apiVersion}/${env.whatsapp.phoneNumberId}/messages`;
            const payload = {
                messaging_product: 'whatsapp',
                recipient_type: 'individual',
                to: cleanPhone,
                type: 'text',
                text: { body: text }
            };

            const response = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${env.whatsapp.accessToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();
            if (!response.ok) {
                logger.error('WhatsApp API dispatch failed', { error: data });
                return { success: false, error: data };
            }

            return { success: true, data };
        } catch (error) {
            logger.error('Error in sendTextMessage to WhatsApp', { error: error.message });
            return { success: false, error: error.message };
        }
    },

    /**
     * Mark message as read
     */
    markMessageAsRead: async (messageId) => {
        if (!env.whatsapp.accessToken || !env.whatsapp.phoneNumberId) return { simulated: true };
        try {
            const endpoint = `${env.whatsapp.baseUrl}/${env.whatsapp.apiVersion}/${env.whatsapp.phoneNumberId}/messages`;
            await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${env.whatsapp.accessToken}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    messaging_product: 'whatsapp',
                    status: 'read',
                    message_id: messageId
                })
            });
        } catch (e) {
            logger.warn('Failed to mark message as read', { error: e.message });
        }
    }
};

export default whatsappService;
