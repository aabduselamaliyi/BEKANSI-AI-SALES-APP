/**
 * ============================================================================
 * BEKANSI AI SALES & INTERIOR DESIGN PLATFORM - SERVER.JS (ESM)
 * ============================================================================
 */

import express from 'express';
import { fileURLToPath } from 'url';
import env from './src/config/env.js';
import logger from './src/config/logger.js';

// Sub-systems
import webhookRoutes from './src/whatsapp/webhook.js';
import productService from './src/products/products.js';
import crm from './src/crm/crm.js';
import tools from './src/ai/tools.js';

const app = express();

// Security & Parsing Middlewares
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', env.corsOrigin);
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
    res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    if (req.method === 'OPTIONS') return res.sendStatus(200);
    next();
});

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request Logger
app.use((req, res, next) => {
    logger.info(`${req.method} ${req.url}`);
    next();
});

// 1. WhatsApp Cloud API Webhook
app.use('/webhook', webhookRoutes);

// 2. Products API
app.get('/api/products', async (req, res) => {
    try {
        const products = await productService.getAllProducts();
        return res.status(200).json({ success: true, count: products.length, data: products });
    } catch (err) {
        return res.status(500).json({ success: false, error: err.message });
    }
});

app.get('/api/products/variants', async (req, res) => {
    try {
        const variants = await productService.getProductVariants({ sku: req.query.sku });
        return res.status(200).json({ success: true, count: variants.length, data: variants });
    } catch (err) {
        return res.status(500).json({ success: false, error: err.message });
    }
});

// 3. Pricing API
app.get('/api/prices', (req, res) => {
    const prices = productService.getAllPrices();
    return res.status(200).json({ success: true, count: prices.length, data: prices });
});

// 4. CRM API
app.get('/api/crm/customers', (req, res) => {
    const customers = crm.getAllCustomers();
    return res.status(200).json({ success: true, count: customers.length, data: customers });
});

app.get('/api/crm/leads', (req, res) => {
    const leads = crm.getAllLeads();
    return res.status(200).json({ success: true, count: leads.length, data: leads });
});

app.get('/api/crm/conversations', (req, res) => {
    const conversations = crm.getAllConversations();
    return res.status(200).json({ success: true, count: conversations.length, data: conversations });
});

app.get('/api/crm/ai-logs', (req, res) => {
    const logs = tools.getToolLogs();
    return res.status(200).json({ success: true, count: logs.length, data: logs });
});

// Health check
app.get('/health', (req, res) => {
    return res.status(200).json({
        status: 'OK',
        platform: 'Bekansi AI Sales Platform',
        version: '1.0.0',
        environment: env.nodeEnv,
        uptimeSeconds: Math.floor(process.uptime()),
        timestamp: new Date().toISOString()
    });
});

// Root welcome
app.get('/', (req, res) => {
    return res.status(200).json({
        message: 'Welcome to Bekansi AI Sales Platform (Ethiopia 🇪🇹)',
        tagline: 'Luxury Furniture & Interior Design Automation',
        endpoints: {
            health: '/health',
            webhook: '/webhook',
            pipeline: '/webhook/pipeline',
            simulate: 'POST /webhook/simulate',
            products: '/api/products',
            prices: '/api/prices',
            customers: '/api/crm/customers',
            leads: '/api/crm/leads'
        }
    });
});

// Start Server if executed directly
const isDirectExecution = process.argv[1] && fileURLToPath(import.meta.url) === process.argv[1];
if (isDirectExecution) {
    app.listen(env.port, () => {
        logger.info(`🚀 Bekansi AI Sales Backend running on port ${env.port} [${env.nodeEnv}] (ESM)`);
        logger.info(`👉 WhatsApp Webhook active at http://localhost:${env.port}/webhook`);
    });
}

export default app;
