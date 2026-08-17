/**
 * ============================================================================
 * CUSTOMERS & CRM ROUTES
 * ============================================================================
 */
const express = require('express');
const { tenantAuthenticator } = require('../middleware/auth');
const logger = require('../config/logger');

const router = express.Router();
router.use(tenantAuthenticator);

/**
 * GET /api/v1/customers - Get list of customers
 */
router.get('/', async (req, res) => {
    try {
        const { search, limit = 50, page = 1 } = req.query;
        const offset = (parseInt(page, 10) - 1) * parseInt(limit, 10);

        const result = await req.dbExecute(async (txClient) => {
            let sql = `
                SELECT c.*, 
                    COUNT(DISTINCT l.id) as lead_count,
                    COUNT(DISTINCT q.id) as quotation_count
                FROM customers c
                LEFT JOIN leads l ON c.id = l.customer_id
                LEFT JOIN quotations q ON c.id = q.customer_id
                WHERE 1=1
            `;
            const params = [];

            if (search) {
                sql += ` AND (c.first_name ILIKE $1 OR c.last_name ILIKE $1 OR c.phone_number ILIKE $1 OR c.email ILIKE $1)`;
                params.push(`%${search}%`);
            }

            sql += ` GROUP BY c.id ORDER BY c.created_at DESC LIMIT $${params.length + 1} OFFSET $${params.length + 2}`;
            params.push(parseInt(limit, 10), offset);

            const resData = await txClient.query(sql, params);
            return resData.rows;
        });

        return res.status(200).json({ success: true, data: result });
    } catch (error) {
        logger.error('Failed to query customers', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'DATABASE_ERROR', message: 'Failed to retrieve customers.' }
        });
    }
});

/**
 * POST /api/v1/customers - Create a customer
 */
router.post('/', async (req, res) => {
    try {
        const { first_name, last_name, email, phone_number, preferred_language = 'am', telegram_id, facebook_psid, segment_tags } = req.body;

        if (!first_name || !phone_number) {
            return res.status(400).json({
                success: false,
                error: { code: 'VALIDATION_ERROR', message: 'first_name and phone_number are required.' }
            });
        }

        const customer = await req.dbExecute(async (txClient) => {
            const sql = `
                INSERT INTO customers (
                    organization_id, first_name, last_name, email, phone_number, preferred_language, telegram_id, facebook_psid, segment_tags
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                RETURNING *
            `;
            const params = [
                req.tenantId, first_name, last_name || null, email || null, phone_number,
                preferred_language, telegram_id || null, facebook_psid || null, segment_tags || []
            ];
            const insertRes = await txClient.query(sql, params);
            return insertRes.rows[0];
        });

        return res.status(201).json({ success: true, data: customer });
    } catch (error) {
        logger.error('Failed to create customer', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'DATABASE_ERROR', message: 'Failed to create customer record.' }
        });
    }
});

module.exports = router;
