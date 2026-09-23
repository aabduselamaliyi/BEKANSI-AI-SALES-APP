/**
 * ============================================================================
 * META WHATSAPP CLOUD API INTEGRATION SERVICE
 * ============================================================================
 * Handles Meta webhook handshakes, payload parsing, and outbound message dispatch.
 */

const https = require('https');
const logger = require('../config/logger');

const WHATSAPP_API_TOKEN = process.env.WHATSAPP_API_TOKEN || process.env.WHATSAPP_TOKEN || '';
const WHATSAPP_PHONE_NUMBER_ID = process.env.WHATSAPP_PHONE_NUMBER_ID || process.env.WHATSAPP_PHONE_ID || '104857692018374';
const WHATSAPP_VERIFY_TOKEN = process.env.WHATSAPP_VERIFY_TOKEN || 'bekansi_wa_verify_token_2026';

const whatsappService = {

    /**
     * Webhook Verification Handshake
     * Responds to Meta's GET verification challenge
     */
    verifyWebhook: (req, res) => {
        const mode = req.query['hub.mode'];
        const token = req.query['hub.verify_token'];
        const challenge = req.query['hub.challenge'];

        logger.info('Meta WhatsApp Webhook verification request received', { mode, tokenReceived: !!token });

        if (mode && token) {
            if (mode === 'subscribe' && token === WHATSAPP_VERIFY_TOKEN) {
                logger.info('Meta WhatsApp Webhook successfully verified');
                return res.status(200).send(challenge);
            } else {
                logger.warn('Meta WhatsApp Webhook verification token mismatch', { expected: WHATSAPP_VERIFY_TOKEN, received: token });
                return res.status(403).send('Forbidden: Token mismatch');
            }
        }

        return res.status(400).send('Bad Request: Missing hub parameters');
    },

    /**
     * Parse inbound webhook event from Meta Cloud API
     */
    parseInboundMessage: (reqBody) => {
        try {
            const entry = reqBody.entry?.[0];
            const change = entry?.changes?.[0];
            const value = change?.value;
            const message = value?.messages?.[0];
            const contact = value?.contacts?.[0];

            if (!message) {
                return null; // Delivery status update or non-message event
            }

            const senderPhone = message.from;
            const senderName = contact?.profile?.name || 'Bekansi WhatsApp Client';
            const messageId = message.id;
            const timestamp = message.timestamp;

            let textContent = '';
            let messageType = message.type;

            if (message.type === 'text') {
                textContent = message.text?.body || '';
            } else if (message.type === 'interactive') {
                textContent = message.interactive?.button_reply?.title || message.interactive?.list_reply?.title || '';
            } else if (message.type === 'button') {
                textContent = message.button?.text || '';
            } else if (message.type === 'image') {
                textContent = `[Image Received: ${message.image?.caption || 'Customer sent room photo / design reference'}]`;
            } else if (message.type === 'audio') {
                textContent = '[Voice Note Received]';
            }

            return {
                messageId,
                senderPhone,
                senderName,
                textContent,
                messageType,
                timestamp,
                raw: message
            };
        } catch (error) {
            logger.error('Error parsing inbound WhatsApp message payload', { error: error.message });
            return null;
        }
    },

    /**
     * Send Outbound WhatsApp Text Message via Meta Cloud API
     */
    sendWhatsAppMessage: async (to, messageText) => {
        logger.info('Dispatching WhatsApp response to customer', { to, textLength: messageText.length });

        // If credentials are configured, call Meta Graph API
        if (WHATSAPP_API_TOKEN && WHATSAPP_PHONE_NUMBER_ID) {
            const payload = JSON.stringify({
                messaging_product: 'whatsapp',
                recipient_type: 'individual',
                to: to.replace(/[^0-9]/g, ''),
                type: 'text',
                text: { body: messageText }
            });

            return new Promise((resolve) => {
                const options = {
                    hostname: 'graph.facebook.com',
                    path: `/v19.0/${WHATSAPP_PHONE_NUMBER_ID}/messages`,
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${WHATSAPP_API_TOKEN}`,
                        'Content-Type': 'application/json',
                        'Content-Length': Buffer.byteLength(payload)
                    }
                };

                const req = https.request(options, (res) => {
                    let data = '';
                    res.on('data', (chunk) => { data += chunk; });
                    res.on('end', () => {
                        logger.info('Meta Cloud API dispatch response', { statusCode: res.statusCode });
                        resolve({ success: res.statusCode === 200, response: data });
                    });
                });

                req.on('error', (e) => {
                    logger.error('Failed to post to Meta Cloud API', { error: e.message });
                    resolve({ success: false, error: e.message });
                });

                req.write(payload);
                req.end();
            });
        }

        // Development / local simulation mode
        logger.info('WhatsApp Cloud API simulation mode active (WHATSAPP_API_TOKEN not set in environment). Outbound message simulated successfully.');
        return {
            success: true,
            simulated: true,
            dispatchedTo: to,
            message: messageText
        };
    }
};

module.exports = whatsappService;
