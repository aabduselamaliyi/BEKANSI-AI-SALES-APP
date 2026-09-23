/**
 * ============================================================================
 * LEADS & SALES PIPELINE ROUTES
 * ============================================================================
 */
const express = require('express');
const { tenantAuthenticator } = require('../middleware/auth');
const logger = require('../config/logger');

const router = express.Router();
router.use(tenantAuthenticator);

/**
 * GET /api/v1/leads - Fetch all pipeline leads
 */
router.get('/', async (req, res) => {
    try {
        const { status, limit = 50, page = 1 } = req.query;
        const offset = (parseInt(page, 10) - 1) * parseInt(limit, 10);

        const leads = await req.dbExecute(async (txClient) => {
            let sql = `
                SELECT l.*, 
                    c.first_name as customer_first_name,
                    c.last_name as customer_last_name,
                    c.phone_number as customer_phone,
                    u.first_name as assigned_agent_name
                FROM leads l
                JOIN customers c ON l.customer_id = c.id
                LEFT JOIN users u ON l.assigned_agent_id = u.id
                WHERE 1=1
            `;
            const params = [];

            if (status) {
                sql += ` AND l.status = $1`;
                params.push(status);
            }

            sql += ` ORDER BY l.created_at DESC LIMIT $${params.length + 1} OFFSET $${params.length + 2}`;
            params.push(parseInt(limit, 10), offset);

            const result = await txClient.query(sql, params);
            return result.rows;
        });

        return res.status(200).json({ success: true, data: leads });
    } catch (error) {
        logger.error('Failed to query leads', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'DATABASE_ERROR', message: 'Failed to retrieve leads pipeline.' }
        });
    }
});

/**
 * POST /api/v1/leads - Create a new lead
 */
router.post('/', async (req, res) => {
    try {
        const { customer_id, source = 'whatsapp', status = 'new', lead_score = 50, requirements, estimated_budget, notes } = req.body;

        if (!customer_id) {
            return res.status(400).json({
                success: false,
                error: { code: 'VALIDATION_ERROR', message: 'customer_id is required.' }
            });
        }

        const lead = await req.dbExecute(async (txClient) => {
            const sql = `
                INSERT INTO leads (
                    organization_id, customer_id, assigned_agent_id, source, status, lead_score, requirements, estimated_budget, notes
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
                RETURNING *
            `;
            const params = [
                req.tenantId, customer_id, req.user.id, source, status, lead_score,
                requirements || null, estimated_budget || null, notes || null
            ];
            const insertRes = await txClient.query(sql, params);
            return insertRes.rows[0];
        });

        return res.status(201).json({ success: true, data: lead });
    } catch (error) {
        logger.error('Failed to create lead', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'DATABASE_ERROR', message: 'Failed to create lead.' }
        });
    }
});

module.exports = router;
