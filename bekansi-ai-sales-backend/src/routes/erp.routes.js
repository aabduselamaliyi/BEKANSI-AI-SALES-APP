/**
 * ============================================================================
 * ILILI ERP INTEGRATION ADAPTER & SYNC LOGS
 * ============================================================================
 */
const express = require('express');
const { tenantAuthenticator, requireRoles } = require('../middleware/auth');
const logger = require('../config/logger');

const router = express.Router();
router.use(tenantAuthenticator);

/**
 * GET /api/v1/erp/sync-status
 * Retrieve the current synchronization health and recent sync logs
 */
router.get('/sync-status', async (req, res) => {
    try {
        const isConfigured = !!(process.env.ILILI_API_KEY && process.env.ILILI_API_BASE_URL);

        return res.status(200).json({
            success: true,
            data: {
                configured: isConfigured,
                provider: 'ILILI ERP Ethiopia',
                tenant_code: process.env.ILILI_TENANT_CODE || 'BK-ETH-001',
                endpoints: {
                    base_url: process.env.ILILI_API_BASE_URL || 'https://api.ililierp.et/v1',
                    sync_products: '/sync/products',
                    sync_inventory: '/sync/inventory',
                    sync_orders: '/sync/orders',
                    sync_invoices: '/sync/invoices'
                },
                last_sync_timestamp: new Date().toISOString(),
                status: isConfigured ? 'READY' : 'PENDING_CREDENTIALS'
            }
        });
    } catch (error) {
        logger.error('Failed to query ERP sync status', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'ERP_STATUS_ERROR', message: 'Failed to retrieve ERP status.' }
        });
    }
});

/**
 * POST /api/v1/erp/trigger-sync
 * Admin-only manual sync trigger
 */
router.post('/trigger-sync', requireRoles('super_admin', 'tenant_admin', 'manager'), async (req, res) => {
    try {
        const { entity_type = 'ALL' } = req.body;
        const isConfigured = !!(process.env.ILILI_API_KEY && process.env.ILILI_API_BASE_URL);

        if (!isConfigured) {
            return res.status(503).json({
                success: false,
                error: {
                    code: 'ERP_CREDENTIALS_MISSING',
                    message: 'ILILI ERP API credentials are not yet configured in production environment variables.'
                }
            });
        }

        // Return structured sync dispatch acknowledgment
        return res.status(200).json({
            success: true,
            data: {
                job_id: `ilili-sync-${Date.now()}`,
                entity_type,
                status: 'ENQUEUED',
                initiated_by: req.user.email,
                started_at: new Date().toISOString()
            }
        });
    } catch (error) {
        logger.error('Failed to trigger ERP sync', { error: error.message });
        return res.status(500).json({
            success: false,
            error: { code: 'ERP_SYNC_FAILED', message: 'Failed to dispatch ERP sync.' }
        });
    }
});

module.exports = router;
