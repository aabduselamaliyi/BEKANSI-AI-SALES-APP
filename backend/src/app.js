/**
 * ============================================================================
 * BEKANSI AI SALES PLATFORM - EXPRESS APPLICATION ROOT
 * ============================================================================
 */
const express = require('express');
const cors = require('cors');
const { pool } = require('./config/dbPool');
const logger = require('./config/logger');

// Import modular routes
const authRoutes = require('./routes/auth.routes');
const customersRoutes = require('./routes/customers.routes');
const leadsRoutes = require('./routes/leads.routes');
const quotationsRoutes = require('./routes/quotations.routes');
const erpRoutes = require('./routes/erp.routes');
const catalogRouter = require('../../catalogRouter');

const app = express();

app.use(cors({
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With']
}));

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

/**
 * Health & Readiness Check Endpoint
 * GET /api/v1/health
 */
app.get('/api/v1/health', async (req, res) => {
    let dbStatus = 'disconnected';
    try {
        await pool.query('SELECT 1');
        dbStatus = 'connected';
    } catch (e) {
        dbStatus = 'unreachable';
    }

    return res.status(200).json({
        success: true,
        data: {
            service: 'Bekansi AI Sales Platform API',
            version: '1.0.0',
            status: 'healthy',
            database: dbStatus,
            environment: process.env.NODE_ENV || 'development',
            timestamp: new Date().toISOString()
        }
    });
});

// Mount Routes
app.use('/api/v1/auth', authRoutes);
app.use('/api/v1/products', catalogRouter);
app.use('/api/v1/customers', customersRoutes);
app.use('/api/v1/leads', leadsRoutes);
app.use('/api/v1/quotations', quotationsRoutes);
app.use('/api/v1/erp', erpRoutes);

// Centralized 404 Handler
app.use((req, res) => {
    res.status(404).json({
        success: false,
        error: {
            code: 'NOT_FOUND',
            message: `Route ${req.method} ${req.originalUrl} not found on this server.`
        }
    });
});

// Centralized Global Error Handler
app.use((err, req, res, next) => {
    logger.error('Unhandled Application Error', {
        error: err.message,
        stack: err.stack,
        path: req.originalUrl,
        method: req.method
    });

    res.status(err.status || 500).json({
        success: false,
        error: {
            code: err.code || 'INTERNAL_SERVER_ERROR',
            message: process.env.NODE_ENV === 'production' ? 'An internal error occurred.' : err.message
        }
    });
});

module.exports = app;
