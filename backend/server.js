/**
 * ============================================================================
 * BEKANSI AI SALES & INTERIOR DESIGN PLATFORM - SERVER.JS
 * ============================================================================
 * Clean architectural root entrypoint for Bekansi Furniture & Interior Design
 */

const express = require('express');
const cors = require('cors');
const env = require('./src/config/env');
const logger = require('./src/config/logger');

// Sub-systems
const webhookRoutes = require('./src/whatsapp/webhook');
const productService = require('./src/products/products');
const pricingService = require('./src/products/pricing');
const customerCrm = require('./src/crm/customers');
const conversationCrm = require('./src/crm/conversations');
const leadCrm = require('./src/crm/leads');
const toolsDispatcher = require('./src/ai/tools');

const app = express();

// Security & Parsing Middlewares
app.use(cors({ origin: env.corsOrigin }));
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
    const prices = pricingService.getAllPrices();
    return res.status(200).json({ success: true, count: prices.length, data: prices });
});

// 4. CRM API
app.get('/api/crm/customers', (req, res) => {
    const customers = customerCrm.getAllCustomers();
    return res.status(200).json({ success: true, count: customers.length, data: customers });
});

app.get('/api/crm/leads', (req, res) => {
    const leads = leadCrm.getAllLeads();
    return res.status(200).json({ success: true, count: leads.length, data: leads });
});

app.get('/api/crm/conversations', (req, res) => {
    const conversations = conversationCrm.getAllConversations();
    return res.status(200).json({ success: true, count: conversations.length, data: conversations });
});

app.get('/api/crm/ai-logs', (req, res) => {
    const logs = toolsDispatcher.getToolLogs();
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

// Start Server
if (require.main === module) {
    app.listen(env.port, () => {
        logger.info(`🚀 Bekansi AI Sales Backend running on port ${env.port} [${env.nodeEnv}]`);
        logger.info(`👉 WhatsApp Webhook active at http://localhost:${env.port}/webhook`);
    });
}

module.exports = app;
