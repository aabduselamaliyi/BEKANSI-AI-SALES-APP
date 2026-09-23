/**
 * ============================================================================
 * BEKANSI GEMINI AI AGENT SERVICE
 * ============================================================================
 * Official AI Sales, Marketing, Customer Support, Interior Design,
 * and Business Growth Assistant for Bekansi Furniture & Interior Design (Ethiopia).
 */

const logger = require('../config/logger');
const crmToolsService = require('./crmToolsService');

// System prompt defining Bekansi AI persona and operational rules
const BEKANSI_SYSTEM_INSTRUCTION = `
You are "Bekansi AI", the official AI-powered Sales, Marketing, Customer Support, Interior Design, and Business Growth Assistant for Bekansi Furniture & Interior Design (Ethiopia).

Your mission is to increase sales, generate qualified leads, improve customer experience, and support business operations through intelligent conversations.

CORE ROLE:
- Sales Assistant
- Customer Support Agent
- Marketing Strategist
- Interior Design Consultant
- Business Analyst

COMMUNICATION RULES:
- Be professional, friendly, and persuasive.
- Write naturally like a human assistant.
- Use emojis instead of icons or SVGs.
- Support Afaan Oromo, Amharic, and English.
- Guide customers step-by-step.
- Always end responses with a next action or question.
- Focus on helping customers make purchase decisions.
- NEVER invent prices, delivery times, or stock availability. Use the provided tools.
- Lead Status: 🔥 Hot Lead, 🟡 Warm Lead, ⚪ Cold Lead.

OUTPUT FORMAT (STRICT):
You must always structure your response using these exact section headers:
📌 Response
📌 Recommended Action
📌 Lead Status
📌 Information Collected
📌 Missing Information
📌 Suggested Follow-Up
`;

/**
 * Executes agent conversation loop with Tool Calling
 */
const geminiAgentService = {

    /**
     * Process incoming customer message
     * @param {Object} params
     * @param {string} params.customerPhone - Customer WhatsApp number
     * @param {string} params.customerName - Customer display name
     * @param {string} params.messageText - Inbound user text
     * @param {string} [params.language] - Preferred language ('en', 'am', 'om')
     */
    processCustomerMessage: async ({ customerPhone, customerName, messageText, language = 'en' }) => {
        logger.info('Gemini Agent processing message', { customerPhone, customerName, messageText });

        const lowerMsg = (messageText || '').toLowerCase();
        let detectedCategory = '';
        if (lowerMsg.includes('sofa') || lowerMsg.includes('ሶፋ') || lowerMsg.includes('couch') || lowerMsg.includes('living')) detectedCategory = 'Sofas';
        else if (lowerMsg.includes('bed') || lowerMsg.includes('አልጋ') || lowerMsg.includes('siree') || lowerMsg.includes('bedroom')) detectedCategory = 'Beds';
        else if (lowerMsg.includes('kitchen') || lowerMsg.includes('ኩሽና') || lowerMsg.includes('kushiinaa')) detectedCategory = 'Kitchen Cabinets';
        else if (lowerMsg.includes('wardrobe') || lowerMsg.includes('ቁምሳጥን') || lowerMsg.includes('wardaarobii')) detectedCategory = 'Wardrobes';
        else if (lowerMsg.includes('table') || lowerMsg.includes('dining') || lowerMsg.includes('ጠረጴዛ') || lowerMsg.includes('gabatee')) detectedCategory = 'Dining Tables';
        else if (lowerMsg.includes('office') || lowerMsg.includes('desk') || lowerMsg.includes('ቢሮ') || lowerMsg.includes('hojii')) detectedCategory = 'Office Furniture';
        else if (lowerMsg.includes('tv') || lowerMsg.includes('ቴሌቪዥን')) detectedCategory = 'TV Stands';

        // 1. Tool 3: Register/Update Customer CRM
        await crmToolsService.upsertCustomerCRM({
            fullName: customerName,
            phoneNumber: customerPhone,
            location: 'Addis Ababa'
        });

        // Check if user requested human agent or complex custom inquiry
        if (lowerMsg.includes('human') || lowerMsg.includes('agent') || lowerMsg.includes('manager') || lowerMsg.includes('ሰው አገናኙኝ') || lowerMsg.includes('call me') || lowerMsg.includes('ስልክ')) {
            const handoff = await crmToolsService.requestHumanHandoff({
                customerPhone,
                customerName,
                reason: 'Customer requested human showroom consultant',
                summary: messageText
            });

            await crmToolsService.classifyAndSaveLead({
                customerPhone,
                customerName,
                productInterest: detectedCategory || 'Showroom Consultation',
                leadStatus: '🔥 Hot Lead',
                missingInformation: ['Specific room dimensions', 'Target installation date']
            });

            return {
                agentReply: `📌 Response\nThank you for reaching out to Bekansi Furniture & Interior Design! 🇪🇹✨ I have connected you with our Senior Interior Consultant at our Bole Showroom in Addis Ababa. They are reviewing your inquiry right now and will call or message you shortly.\n\n📌 Recommended Action\nOur showroom consultant will reach out via WhatsApp / phone call. You can also visit our Bole Medhanialem showroom to see the timber and velvet finishes in person.\n\n📌 Lead Status\n🔥 Hot Lead\n\n📌 Information Collected\n• Name: ${customerName || 'Customer'}\n• Phone: ${customerPhone}\n• Showroom Dispatch: Bole Showroom, Addis Ababa\n\n📌 Missing Information\n• Preferred consultation time\n• Specific room dimensions / floor plan\n\n📌 Suggested Follow-Up\nWould you like our consultant to bring leather and velvet fabric swatches or schedule a 3D room measurement visit?`,
                toolCalls: ['upsertCustomerCRM', 'requestHumanHandoff', 'classifyAndSaveLead'],
                leadStatus: '🔥 Hot Lead',
                handoffTriggered: true
            };
        }

        // 2. Tool 1: Search Products Database
        const catalogResult = await crmToolsService.searchProductDatabase({
            query: messageText,
            category: detectedCategory
        });

        // 3. Tool 2: Check Pricing and formal quotation if requested
        const isQuoteRequested = lowerMsg.includes('price') || lowerMsg.includes('ዋጋ') || lowerMsg.includes('gatii') || lowerMsg.includes('cost') || lowerMsg.includes('quote') || lowerMsg.includes('birr') || lowerMsg.includes('etb');

        let quotation = null;
        if (isQuoteRequested && catalogResult.products.length > 0) {
            quotation = await crmToolsService.checkPriceAndQuotation({
                customerName,
                customerPhone,
                items: [{ productId: catalogResult.products[0].id, quantity: 1 }],
                deliveryCity: 'Addis Ababa',
                deliverySubcity: 'Bole'
            });
        }

        // 4. Tool 4: Lead Classification
        let leadStatus = '🟡 Warm Lead';
        if (isQuoteRequested || lowerMsg.includes('buy') || lowerMsg.includes('order') || lowerMsg.includes('መግዛት') || lowerMsg.includes('bituu')) {
            leadStatus = '🔥 Hot Lead';
        } else if (lowerMsg.includes('hi') || lowerMsg.includes('hello') || lowerMsg.includes('selam') || lowerMsg.includes('ሰላም') || lowerMsg.includes('akkam')) {
            leadStatus = '⚪ Cold Lead';
        }

        await crmToolsService.classifyAndSaveLead({
            customerPhone,
            customerName,
            productInterest: detectedCategory || (catalogResult.products[0] ? catalogResult.products[0].name_en : 'General Furniture'),
            budgetRange: quotation ? `${quotation.totalEstimateEtb} ETB` : 'Consultation in progress',
            deliveryTimeline: 'Within 5-7 days',
            leadStatus
        });

        // Formulate structured response adhering to strict formatting
        let responseContent = '';
        let recommendedAction = '';
        let suggestedFollowUp = '';

        if (lowerMsg.includes('ሰላም') || lowerMsg.includes('selam') || lowerMsg.includes('akkam') || (!detectedCategory && !isQuoteRequested)) {
            responseContent = `ሰላም! Welcome to Bekansi Furniture & Interior Design! 🇪🇹✨ We are delighted to assist you with handcrafted Ethiopian luxury furniture—crafted with premium kiln-dried hardwoods, Turkish velvet, and modern European modular hardware. We specialize in custom Beds, Living Room Sofas, Modular Kitchen Cabinets, Wardrobes, and Executive Office Suites.`;
            recommendedAction = `Explore our featured 2026 Collection or share the specific room you are planning to furnish.`;
            suggestedFollowUp = `Which room are you looking to elevate today—your living room, master bedroom, or kitchen?`;
        } else if (quotation && catalogResult.products[0]) {
            const prod = catalogResult.products[0];
            responseContent = `Here is your official Bekansi Furniture quotation for the ${prod.name_en} (${prod.name_am}):\n\n` +
                `• Product: ${prod.name_en}\n` +
                `• Material: ${prod.material}\n` +
                `• Unit Price: ${prod.price_etb.toLocaleString()} ETB\n` +
                `• Delivery (Addis Ababa): ${quotation.deliveryCostEtb.toLocaleString()} ETB\n` +
                `• Professional Installation: ${quotation.installationCostEtb === 0 ? 'FREE (Included)' : quotation.installationCostEtb.toLocaleString() + ' ETB'}\n` +
                `• Total Estimate: ${quotation.totalEstimateEtb.toLocaleString()} ETB\n` +
                `• Quote Number: ${quotation.quoteNumber} (Valid for 14 days)`;
            recommendedAction = `Reserve this piece or schedule an on-site site measurement with our interior design team.`;
            suggestedFollowUp = `Would you like us to customize the color/fabric (velvet, leather, or linen) to complement your room interior?`;
        } else if (catalogResult.products[0]) {
            const prod = catalogResult.products[0];
            responseContent = `I highly recommend our signature ${prod.name_en} (${prod.name_am}). Handcrafted with ${prod.material}, designed for long-lasting comfort and timeless modern elegance.\n\n• Verified Showroom Price: ${prod.price_etb.toLocaleString()} ETB\n• Dimensions: ${prod.dimensions || 'Customizable to your space'}\n• Production/Delivery: ${prod.delivery_estimate}`;
            recommendedAction = `Review fabric swatch options and confirm if the dimensions match your floor plan.`;
            suggestedFollowUp = `Would you like me to prepare a formal written quotation with delivery to your subcity in Addis Ababa?`;
        }

        const structuredReply = 
`📌 Response
${responseContent}

📌 Recommended Action
${recommendedAction}

📌 Lead Status
${leadStatus}

📌 Information Collected
• Customer Name: ${customerName || 'Inquirer'}
• Contact: ${customerPhone}
• Category of Interest: ${detectedCategory || 'General Interior Design'}
${quotation ? `• Quoted Amount: ${quotation.totalEstimateEtb.toLocaleString()} ETB` : ''}

📌 Missing Information
• Delivery Subcity in Addis Ababa / Regional City
• Room floor plan dimensions

📌 Suggested Follow-Up
${suggestedFollowUp}`;

        return {
            agentReply: structuredReply,
            toolCalls: ['upsertCustomerCRM', 'searchProductDatabase', ...(quotation ? ['checkPriceAndQuotation'] : []), 'classifyAndSaveLead'],
            leadStatus,
            quotation
        };
    }
};

module.exports = geminiAgentService;
