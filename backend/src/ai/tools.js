/**
 * BEKANSI AI SALES PLATFORM - AI/TOOLS.JS
 * Tool execution dispatcher binding AI functions to database and CRM services
 */

const productService = require('../products/products');
const pricingService = require('../products/pricing');
const customerCrm = require('../crm/customers');
const conversationCrm = require('../crm/conversations');
const leadCrm = require('../crm/leads');
const logger = require('../config/logger');

const aiToolLogs = [];

const toolsDispatcher = {
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
                    result = await pricingService.generateQuotation({
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
                    const customer = await customerCrm.upsertCustomer({
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
                    const lead = await leadCrm.classifyAndSaveLead({
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

    getToolLogs: () => aiToolLogs
};

module.exports = toolsDispatcher;
