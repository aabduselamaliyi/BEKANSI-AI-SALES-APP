/**
 * BEKANSI AI SALES PLATFORM - CONFIG/ENV.JS (ESM)
 */

import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

dotenv.config({ path: path.resolve(__dirname, '../../.env') });

const env = {
    port: parseInt(process.env.PORT || '3000', 10),
    nodeEnv: process.env.NODE_ENV || 'development',
    logLevel: process.env.LOG_LEVEL || 'info',
    corsOrigin: process.env.CORS_ORIGIN || '*',

    // Bekansi Brand Details
    brand: {
        name: process.env.BEKANSI_NAME || 'BEKANSI FURNITURE & INTERIOR DESIGN',
        whatsapp: process.env.BEKANSI_WHATSAPP || '+251988828861',
        location: process.env.BEKANSI_LOCATION || 'Dukem, in front of Daroni Hotel, beside Oromia Bank, next to Dibora Restaurant',
        freeDelivery: process.env.BEKANSI_FREE_DELIVERY || process.env['BEKANSI_FREE DELIVERY'] || 'Across Ethiopia'
    },

    // WhatsApp Cloud API
    whatsapp: {
        verifyToken: process.env.WHATSAPP_VERIFY_TOKEN || 'CREATE_A_RANDOM_SECRET_HERE',
        accessToken: process.env.WHATSAPP_ACCESS_TOKEN || '',
        phoneNumberId: process.env.WHATSAPP_PHONE_NUMBER_ID || '',
        businessAccountId: process.env.WHATSAPP_BUSINESS_ACCOUNT_ID || '',
        appSecret: process.env.WHATSAPP_APP_SECRET || '',
        apiVersion: process.env.WHATSAPP_API_VERSION || 'v23.0',
        baseUrl: 'https://graph.facebook.com'
    },

    // Gemini AI Engine
    ai: {
        apiKey: process.env.GEMINI_API_KEY || process.env.API_KEY || '',
        model: process.env.GEMINI_MODEL || 'gemini-3.8-flash'
    },

    // Supabase Configuration
    supabase: {
        url: process.env.SUPABASE_URL || '',
        serviceRoleKey: process.env.SUPABASE_SERVICE_ROLE_KEY || ''
    }
};

export default env;
export { env };
