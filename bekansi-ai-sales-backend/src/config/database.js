/**
 * BEKANSI AI SALES PLATFORM - CONFIG/DATABASE.JS (ESM)
 */

import { createClient } from '@supabase/supabase-js';
import env from './env.js';
import logger from './logger.js';

let pool = null;
let isConnected = false;

// Initialize Supabase Client
let supabase = null;
try {
    if (env.supabase.url && env.supabase.anonKey && !env.supabase.url.includes('placeholder')) {
        supabase = createClient(env.supabase.url, env.supabase.anonKey);
    }
} catch (err) {
    logger.warn('Supabase client initialization skipped or invalid', { error: err.message });
}

async function query(text, params) {
    if (pool) {
        try {
            return await pool.query(text, params);
        } catch (err) {
            logger.warn('Database query failed, passing to fallback', { query: text, error: err.message });
            throw err;
        }
    }
    throw new Error('Database pool not available');
}

export { pool, supabase, query, isConnected };
export default {
    pool,
    supabase,
    query,
    isDbConnected: () => isConnected
};
