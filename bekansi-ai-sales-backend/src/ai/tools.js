/**
 * BEKANSI AI SALES PLATFORM - AI/TOOLS.JS (ESM)
 * AI Tool definitions and dispatcher integrating Products, Pricing, and CRM
 */

import productService from '../products/products.js';
import crm from '../crm/crm.js';
import logger from '../config/logger.js';

export const AI_TOOLS_SCHEMA = [
    {
        name: 'searchProductDatabase',
        description: 'Search Bekansi Furniture Ethiopian master catalog across Beds, Sofas, Kitchen Cabinets, Wardrobes, Dining Tables, TV Stands, and Office Furniture.',
        parameters: {
            type: 'OBJECT',
            properties: {
                query: { type: 'STRING', description: 'Keyword such as "King Bed", "L-shape sofa", "UV acrylic kitchen", "dining table"' },
                category: { type: 'STRING', description: 'Product category: Beds, Sofas, Kitchen Cabinets, Wardrobes, Dining Tables, TV Stands, Office Furniture' },
                maxPrice: { type: 'NUMBER', description: 'Maximum budget in Ethiopian Birr (ETB)' }
            }
        }
    },
    {
        name: 'checkPriceAndQuotation',
        description: 'Generate an accurate, official structured quotation with unit price, delivery, and installation in Ethiopian Birr (ETB). Never fabricate prices.',
        parameters: {
            type: 'OBJECT',
            properties: {
                customerName: { type: 'STRING', description: 'Customer full name' },
                customerPhone: { type: 'STRING', description: 'Customer phone number' },
                sku: { type: 'STRING', description: 'Product SKU (e.g., BK-BED-ENTOTO, BK-SOF-BOLE)' },
                productName: { type: 'STRING', description: 'Full name of the furniture item' },
                quantity: { type: 'INTEGER', description: 'Quantity requested (default 1)' },
                color: { type: 'STRING', description: 'Specific color or fabric variant' },
                deliveryLocation: { type: 'STRING', description: 'Sub-city or delivery neighborhood in Addis Ababa (e.g., Bole, CMC, Sarbet)' }
            },
            required: ['sku']
        }
    },
    {
        name: 'upsertCustomerCRM',
        description: 'Save or update customer contact information, preferred language, and delivery location in the CRM database.',
        parameters: {
            type: 'OBJECT',
            properties: {
                fullName: { type: 'STRING', description: 'Customer full name' },
                phoneNumber: { type: 'STRING', description: 'Customer phone number with country code (e.g. +251911223344)' },
                preferredLanguage: { type: 'STRING', description: 'Customer language: am (Amharic), om (Afaan Oromo), or en (English)' },
                city: { type: 'STRING', description: 'City (e.g., Addis Ababa)' },
                subCity: { type: 'STRING', description: 'Addis Ababa Sub-city (e.g., Bole, Yeka, Kirkos, Nefas Silk)' },
                deliveryLocation: { type: 'STRING', description: 'Specific neighborhood or landmark' }
            },
            required: ['phoneNumber']
        }
    },
    {
        name: 'classifyAndSaveLead',
        description: 'Classify customer lead temperature (🔥 Hot, 🟡 Warm, ⚪ Cold) and save custom furniture specs (size, color, fabric, finish, led, socket) to the sales pipeline.',
        parameters: {
            type: 'OBJECT',
            properties: {
                customerPhone: { type: 'STRING', description: 'Customer phone number' },
                customerName: { type: 'STRING', description: 'Customer name' },
                productCategory: { type: 'STRING', description: 'Category: Beds, Sofas, Kitchen Cabinets, Wardrobes, etc.' },
                quantity: { type: 'INTEGER', description: 'Quantity requested' },
                size: { type: 'STRING', description: 'Size requested (e.g. King 180x200cm, 5-seater)' },
                color: { type: 'STRING', description: 'Color choice (e.g. Royal Blue, Emerald Green)' },
                fabric: { type: 'STRING', description: 'Fabric choice (e.g. Imported Velvet, Linen Blend)' },
                finish: { type: 'STRING', description: 'Wood/metal finish (e.g. High Gloss Walnut, Matte Oak)' },
                ledRequired: { type: 'BOOLEAN', description: 'Whether LED ambient lighting is requested' },
                socketRequired: { type: 'BOOLEAN', description: 'Whether integrated USB/power sockets are requested' },
                budgetMin: { type: 'NUMBER', description: 'Minimum budget in ETB' },
                budgetMax: { type: 'NUMBER', description: 'Maximum budget in ETB' },
                purchaseTimeline: { type: 'STRING', description: 'Timeline (e.g. Ready this week, 2-4 weeks)' },
                leadStatus: { type: 'STRING', description: 'Classification: 🔥 Hot Lead, 🟡 Warm Lead, or ⚪ Cold Lead' },
                missingInformation: { type: 'ARRAY', items: { type: 'STRING' }, description: 'Missing fields' }
            },
            required: ['customerPhone']
        }
    },
    {
        name: 'requestHumanHandoff',
        description: 'Escalate complex custom architectural woodwork, full villa interior fitouts, or high-value VIP orders to a human sales consultant in our Addis Ababa showroom.',
        parameters: {
            type: 'OBJECT',
            properties: {
                customerPhone: { type: 'STRING', description: 'Customer phone' },
                customerName: { type: 'STRING', description: 'Customer name' },
                reason: { type: 'STRING', description: 'Reason for showroom escalation' },
                urgency: { type: 'STRING', description: 'Urgency level: normal, high, or immediate' }
            },
            required: ['customerPhone', 'reason']
        }
    }
];

const aiToolLogs = [];

export const tools = {
    executeTool: async (toolName, args, context = {}) => {
        logger.info(`AI Tool Invocation: ${toolName}`, { args });
        const startTime = Date.now();
        let result = null;
        let status = 'SUCCESS';
        let errorMessage = null;

        try {
            switch (toolName) {
                case 'searchProductDatabase': {
                    const products = await productService.searchProducts({
                        query: args.query || '',
                        category: args.category || ''
                    });
                    result = { count: products.length, products };
                    break;
                }

                case 'checkPriceAndQuotation': {
                    result = await productService.generateQuotation({
                        customerName: args.customerName || context.customerName,
                        customerPhone: args.customerPhone || context.customerPhone,
                        productName: args.productName,
                        sku: args.sku,
                        quantity: args.quantity || 1,
                        color: args.color,
                        deliveryLocation: args.deliveryLocation || context.deliveryLocation || 'Addis Ababa'
                    });
                    break;
                }

                case 'upsertCustomerCRM': {
                    const customer = await crm.upsertCustomer({
                        fullName: args.fullName,
                        phone: args.phoneNumber,
                        preferredLanguage: args.preferredLanguage,
                        city: args.city,
                        subCity: args.subCity,
                        deliveryLocation: args.deliveryLocation
                    });
                    result = { success: true, customer };
                    break;
                }

                case 'classifyAndSaveLead': {
                    const lead = await crm.classifyAndSaveLead({
                        customerId: context.customerId,
                        conversationId: context.conversationId,
                        customerPhone: args.customerPhone || context.customerPhone,
                        customerName: args.customerName || context.customerName,
                        productCategory: args.productCategory,
                        quantity: args.quantity || 1,
                        size: args.size,
                        color: args.color,
                        fabric: args.fabric,
                        finish: args.finish,
                        ledRequired: args.ledRequired,
                        socketRequired: args.socketRequired,
                        budgetMin: args.budgetMin,
                        budgetMax: args.budgetMax,
                        purchaseTimeline: args.purchaseTimeline,
                        leadStatus: args.leadStatus,
                        missingInformation: args.missingInformation || []
                    });
                    result = { success: true, lead };
                    break;
                }

                case 'requestHumanHandoff': {
                    result = {
                        success: true,
                        ticketId: `HANDOFF-${Date.now().toString().slice(-6)}`,
                        status: 'ASSIGNED_TO_SHOWROOM',
                        assignedConsultant: 'Dawit Mengistu (Bole Showroom Manager)',
                        phone: '+251911445566',
                        note: `Escalation received for ${args.customerName || 'Customer'} (${args.customerPhone}): ${args.reason}`
                    };
                    break;
                }

                default:
                    throw new Error(`Unrecognized AI tool name: ${toolName}`);
            }
        } catch (err) {
            status = 'ERROR';
            errorMessage = err.message;
            logger.error(`Error executing AI tool ${toolName}`, { error: err.message });
            result = { success: false, error: err.message };
        }

        const logEntry = {
            id: `LOG-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
            tool_name: toolName,
            input_arguments: args,
            output_result: result,
            status,
            error_message: errorMessage,
            duration_ms: Date.now() - startTime,
            created_at: new Date().toISOString()
        };
        aiToolLogs.unshift(logEntry);
        if (aiToolLogs.length > 200) aiToolLogs.pop();

        return result;
    },

    getToolLogs: () => aiToolLogs,
    AI_TOOLS_SCHEMA
};

export default tools;
