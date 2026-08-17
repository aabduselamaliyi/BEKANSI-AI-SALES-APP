/**
 * ============================================================================
 * QUOTATIONS & ORDERS ROUTES (Canonical Pricing & Lifecycle Engine)
 * ============================================================================
 */
const express = require('express');
const { tenantAuthenticator } = require('../middleware/auth');
const logger = require('../config/logger');

const router = express.Router();
router.use(tenantAuthenticator);

/**
 * GET /api/v1/quotations - List quotations
 */
router.get('/', async (req, res) => {
    try {
        const { status, limit = 50, page = 1 } = req.query;
        const offset = (parseInt(page, 10) - 1) * parseInt(limit, 10);

        const quotes = await req.dbExecute(async (txClient) => {
            let sql = `
                SELECT q.*, 
                    c.first_name as customer_first_name,
                    c.last_name as customer_last_name,
                    c.phone_number as customer_phone
                FROM quotations q
                JOIN customers c ON q.customer_id = c.id
                WHERE 1=1
            `;
            const params = [];

            if (status) {
                sql += ` AND q.status = $1`;
                params.push(status);
            }

            sql += ` ORDER BY q.created_at DESC LIMIT $${params.length + 1} OFFSET $${params.length + 2}`;
            params.push(parseInt(limit, 10), offset);

            const result = await txClient.query(sql, params);
            return result.rows;
        });

        return res.status(200).json({ success: true, data: quotes });
    } catch (error) {
        logger.error('Failed to list quotations', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'DATABASE_ERROR', message: 'Failed to retrieve quotations.' }
        });
    }
});

/**
 * POST /api/v1/quotations - Create structured quotation with items
 */
router.post('/', async (req, res) => {
    try {
        const { customer_id, lead_id, items, discount = 0, tax = 0, notes, validity_days = 14 } = req.body;

        if (!customer_id || !Array.isArray(items) || items.length === 0) {
            return res.status(400).json({
                success: false,
                error: { code: 'VALIDATION_ERROR', message: 'customer_id and at least one item are required.' }
            });
        }

        const quote = await req.dbExecute(async (txClient) => {
            // Calculate subtotal from database product prices for security & consistency
            let subtotal = 0;
            const verifiedItems = [];

            for (const item of items) {
                const prodRes = await txClient.query('SELECT id, price FROM products WHERE id = $1', [item.product_id]);
                if (prodRes.rows.length === 0) {
                    throw new Error(`Product with ID ${item.product_id} not found.`);
                }
                const unitPrice = parseFloat(prodRes.rows[0].price);
                const quantity = parseInt(item.quantity, 10) || 1;
                const itemTotal = unitPrice * quantity;
                subtotal += itemTotal;

                verifiedItems.push({
                    product_id: item.product_id,
                    quantity,
                    unit_price: unitPrice,
                    total_price: itemTotal
                });
            }

            const parsedDiscount = parseFloat(discount) || 0;
            const parsedTax = parseFloat(tax) || 0;
            const grandTotal = subtotal - parsedDiscount + parsedTax;

            // Generate unique sequential quotation number
            const quoteNumber = `BK-Q-${new Date().getFullYear()}-${Math.floor(100000 + Math.random() * 900000)}`;
            const validUntil = new Date(Date.now() + validity_days * 24 * 60 * 60 * 1000);

            const quoteInsert = await txClient.query(
                `INSERT INTO quotations (
                    organization_id, customer_id, lead_id, created_by, quotation_number, valid_until, subtotal, discount, tax, total, status, notes
                ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, 'draft', $11)
                RETURNING *`,
                [req.tenantId, customer_id, lead_id || null, req.user.id, quoteNumber, validUntil, subtotal, parsedDiscount, parsedTax, grandTotal, notes || null]
            );

            const savedQuote = quoteInsert.rows[0];

            for (const item of verifiedItems) {
                await txClient.query(
                    `INSERT INTO quotation_items (
                        quotation_id, product_id, quantity, unit_price, total_price
                    ) VALUES ($1, $2, $3, $4, $5)`,
                    [savedQuote.id, item.product_id, item.quantity, item.unit_price, item.total_price]
                );
            }

            savedQuote.items = verifiedItems;
            return savedQuote;
        });

        return res.status(201).json({ success: true, data: quote });
    } catch (error) {
        logger.error('Failed to create quotation', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'CREATION_FAILED', message: error.message || 'Failed to create quotation.' }
        });
    }
});

module.exports = router;
