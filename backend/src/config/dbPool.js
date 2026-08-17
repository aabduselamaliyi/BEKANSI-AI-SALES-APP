/**
 * ============================================================================
 * BEKANSI AI SALES PLATFORM - PRODUCTION BACKEND
 * PostgreSQL Database Connection Pool & Context-Aware Execution Engine
 * ============================================================================
 */

const { Pool } = require('pg');
const logger = require('./logger');

const connectionString = process.env.DATABASE_URL || 'postgresql://localhost:5432/bekansi_production_db';

const poolConfig = {
    connectionString,
    max: parseInt(process.env.DB_POOL_MAX || '20', 10),
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
    ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false
};

const pool = new Pool(poolConfig);

pool.on('error', (err) => {
    logger.error('Unexpected error on idle PostgreSQL client pool', { error: err.message, stack: err.stack });
});

pool.on('connect', () => {
    logger.debug('New PostgreSQL client connected to Bekansi pool');
});

/**
 * Execute a query with automatic Tenant Row-Level Security (RLS) enforcement
 * using session variable `app.current_tenant_id`.
 */
const withTenantTransaction = async (tenantId, callback) => {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');
        if (tenantId) {
            await client.query(`SET LOCAL app.current_tenant_id = $1`, [tenantId]);
        }
        const result = await callback(client);
        await client.query('COMMIT');
        return result;
    } catch (error) {
        await client.query('ROLLBACK');
        logger.error('Database transaction rollback in tenant scope', {
            tenantId,
            error: error.message
        });
        throw error;
    } finally {
        client.release();
    }
};

/**
 * Execute a standard query directly on the pool
 */
const query = (text, params) => pool.query(text, params);

module.exports = {
    pool,
    query,
    withTenantTransaction
};
