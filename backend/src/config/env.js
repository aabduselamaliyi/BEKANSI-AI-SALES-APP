/**
 * BEKANSI AI SALES PLATFORM - CONFIG/ENV.JS
 * Validates and exports environment configurations.
 */

const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '../../.env') });

const env = {
    port: parseInt(process.env.PORT || '8080', 10),
    nodeEnv: process.env.NODE_ENV || 'development',
    databaseUrl: process.env.DATABASE_URL || 'postgresql://bekansi_user:bekansi_password@localhost:5432/bekansi_production_db',
    corsOrigin: process.env.CORS_ORIGIN || '*',

    // WhatsApp Cloud API
    whatsapp: {
        verifyToken: process.env.WHATSAPP_VERIFY_TOKEN || 'bekansi_verify_token_2026',
        accessToken: process.env.WHATSAPP_ACCESS_TOKEN || '',
        phoneNumberId: process.env.WHATSAPP_PHONE_NUMBER_ID || '',
        apiVersion: process.env.WHATSAPP_API_VERSION || 'v19.0',
        baseUrl: 'https://graph.facebook.com'
    },

    // Gemini AI Engine
    ai: {
        apiKey: process.env.GEMINI_API_KEY || process.env.API_KEY || '',
        model: process.env.GEMINI_MODEL || 'gemini-2.5-flash'
    },

    // ILILI ERP Integration
    erp: {
        baseUrl: process.env.ILILI_API_BASE_URL || 'https://api.ililierp.et/v1',
        apiKey: process.env.ILILI_API_KEY || '',
        tenantCode: process.env.ILILI_TENANT_CODE || 'BK-ETH-001'
    }
};

module.exports = env;
