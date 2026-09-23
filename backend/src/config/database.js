/**
 * BEKANSI AI SALES PLATFORM - CONFIG/DATABASE.JS
 * PostgreSQL Connection Pool with robust offline/in-memory fallback
 */

const env = require('./env');
const logger = require('./logger');

let pool = null;
let isConnected = false;

try {
    const { Pool } = require('pg');
    pool = new Pool({
        connectionString: env.databaseUrl,
        ssl: env.nodeEnv === 'production' ? { rejectUnauthorized: false } : false,
        max: 20,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 5000,
    });

    pool.on('connect', () => {
        isConnected = true;
    });

    pool.on('error', (err) => {
        logger.warn('PostgreSQL pool connection error, falling back to in-memory store', { error: err.message });
        isConnected = false;
    });
} catch (err) {
    logger.warn('pg driver not loaded, running in resilient standalone memory mode', { error: err.message });
}

async function query(text, params) {
    if (pool) {
        try {
            return await pool.query(text, params);
        } catch (err) {
            logger.warn('Database query failed, passing to in-memory fallback', { query: text, error: err.message });
            throw err;
        }
    }
    throw new Error('Database pool not available');
}

module.exports = {
    pool,
    query,
    isDbConnected: () => isConnected
};
