/**
 * BEKANSI AI SALES PLATFORM - SUPABASE CLIENT (ESM)
 * Initializes Supabase connection using SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY
 */

import { createClient } from '@supabase/supabase-js';
import env from '../config/env.js';

let supabase = null;

if (env.supabase.url && env.supabase.serviceRoleKey && !env.supabase.url.includes('YOUR_SUPABASE')) {
    try {
        supabase = createClient(env.supabase.url, env.supabase.serviceRoleKey, {
            auth: {
                persistSession: false,
                autoRefreshToken: false
            }
        });
        console.log('[INFO] [supabase] 🔌 Supabase client initialized successfully with service role key');
    } catch (err) {
        console.warn('[WARN] [supabase] Failed to initialize Supabase client:', err.message);
    }
} else {
    console.log('[INFO] [supabase] Supabase credentials not configured or placeholder detected. Operating in high-speed in-memory & Postgres pool mode.');
}

export { supabase };
export default supabase;
