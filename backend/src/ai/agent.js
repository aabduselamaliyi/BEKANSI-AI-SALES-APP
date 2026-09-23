/**
 * BEKANSI AI SALES PLATFORM - AI/AGENT.JS
 * Gemini AI Sales, Interior Design & Lead Qualification Agent
 */

const env = require('../config/env');
const logger = require('../config/logger');
const { BEKANSI_SYSTEM_PROMPT } = require('./prompts');
const toolsDispatcher = require('./tools');

const agent = {
    /**
     * Main conversation handler for customer messages
     */
    processCustomerMessage: async ({
        customerPhone,
        customerName = null,
        message,
        customerId = null,
        conversationId = null,
        language = 'en',
        history = []
    }) => {
        logger.info('Processing customer message in Bekansi AI Agent', { customerPhone, messageLength: message.length });

        const context = {
            customerPhone,
            customerName: customerName || 'Valued Customer',
            customerId,
            conversationId,
            language
        };

        // Detect language from text if Amharic or Afaan Oromo characters/words appear
        let detectedLang = language;
        if (/[\u1200-\u137F]/.test(message)) {
            detectedLang = 'am'; // Amharic
        } else if (/\b(akkam|nagaa|akkamitti|barbaada|maaloo|galatoomi|fayyisaa)\b/i.test(message)) {
            detectedLang = 'om'; // Afaan Oromo
        }

        // Automatic tool intent detection
        const toolsInvoked = [];
        const lower = message.toLowerCase();

        // Check if customer mentions beds, sofas, kitchens, wardrobes, etc.
        if (lower.includes('bed') || lower.includes('አልጋ') || lower.includes('siree')) {
            const res = await toolsDispatcher.executeTool('searchProductDatabase', { category: 'Beds' }, context);
            toolsInvoked.push('searchProductDatabase');
            if (lower.includes('price') || lower.includes('ዋጋ') || lower.includes('gatii') || lower.includes('how much')) {
                await toolsDispatcher.executeTool('checkPriceAndQuotation', {
                    sku: 'BK-BED-ENTOTO',
                    productName: 'Entoto Luxury Smart Bed',
                    customerName: context.customerName,
                    customerPhone
                }, context);
                toolsInvoked.push('checkPriceAndQuotation');
            }
        } else if (lower.includes('sofa') || lower.includes('ሶፋ') || lower.includes('soofaa')) {
            await toolsDispatcher.executeTool('searchProductDatabase', { category: 'Sofas' }, context);
            toolsInvoked.push('searchProductDatabase');
            if (lower.includes('price') || lower.includes('ዋጋ') || lower.includes('gatii')) {
                await toolsDispatcher.executeTool('checkPriceAndQuotation', {
                    sku: 'BK-SOF-BOLE',
                    productName: 'Bole Luxury Sectional L-Shape Sofa',
                    customerName: context.customerName,
                    customerPhone
                }, context);
                toolsInvoked.push('checkPriceAndQuotation');
            }
        } else if (lower.includes('kitchen') || lower.includes('ኩሽና') || lower.includes('kushiinaa')) {
            await toolsDispatcher.executeTool('searchProductDatabase', { category: 'Kitchen Cabinets' }, context);
            toolsInvoked.push('searchProductDatabase');
        } else if (lower.includes('wardrobe') || lower.includes('ቁምሳጥን') || lower.includes('wardaarobii')) {
            await toolsDispatcher.executeTool('searchProductDatabase', { category: 'Wardrobes' }, context);
            toolsInvoked.push('searchProductDatabase');
        }

        // Lead classification & qualification
        let leadStatus = '🟡 Warm Lead';
        if (lower.includes('order') || lower.includes('buy') || lower.includes('መግዛት') || lower.includes('bituu') || lower.includes('quote') || lower.includes('ዋጋ')) {
            leadStatus = '🔥 Hot Lead';
        } else if (lower.includes('hi') || lower.includes('hello') || lower.includes('ሰላም') || lower.includes('akkam')) {
            leadStatus = '⚪ Cold Lead';
        }

        await toolsDispatcher.executeTool('classifyAndSaveLead', {
            customerPhone,
            customerName: context.customerName,
            leadStatus,
            productCategory: lower.includes('bed') ? 'Beds' : (lower.includes('sofa') ? 'Sofas' : 'Beds'),
            purchaseTimeline: leadStatus === '🔥 Hot Lead' ? 'Ready to order' : 'Browsing'
        }, context);
        toolsInvoked.push('classifyAndSaveLead');

        // Human handoff escalation for custom villa woodwork or high-value inquiries
        if (lower.includes('architect') || lower.includes('custom villa') || lower.includes('consultant') || lower.includes('አናጋግሩኝ')) {
            await toolsDispatcher.executeTool('requestHumanHandoff', {
                customerPhone,
                customerName: context.customerName,
                reason: 'Customer requested direct architectural consultant consultation'
            }, context);
            toolsInvoked.push('requestHumanHandoff');
        }

        // Generate response conforming to the strict 6-point Bekansi format
        let agentReply = '';

        if (detectedLang === 'am') {
            agentReply = `📌 Response  
እንኳን ወደ በካንሲ ፈርኒቸር እና ኢንቴሪየር ዲዛይን በደህና መጡ! 🇪🇹🛋️ የፈለጉትን የቤትና የቢሮ ፈርኒቸር በጥራትና በቅንጦት እናዘጋጃለን። የቦሌ ሮያል ኪንግ ቬልቬት አልጋችን (85,000 ብር) እና የእንጦጦ ቅንጡ L-ቅርጽ ሳሎን ሶፋችን በምርጥ ቱርክ ቬልቬት የተመረቱ ናቸው።

📌 Recommended Action  
የሚፈልጉትን የቀለም ምርጫ (ሮያል ብሉ ወይም ኤመራልድ ግሪን) እና የመኝታ ቤትዎን ስፋት ያሳውቁን።

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• ስልክ: ${customerPhone}
• ፍላጎት: ${lower.includes('sofa') ? 'ሳሎን ሶፋ' : 'አልጋ እና የቤት ፈርኒቸር'}

📌 Missing Information  
• ሙሉ ስም
• የመኖሪያ ሰፈር / አድራሻ (ቦሌ፣ ሲኤምሲ፣ ወዘተ)
• የመረጡት ቀለምና መጠን

📌 Suggested Follow-Up  
ትክክለኛውን ልኬት ለመውሰድ ባለሙያዎቻችን ወደ ቤትዎ እንዲመጡ ቀጠሮ እንያዝልዎ?`;
        } else if (detectedLang === 'om') {
            agentReply = `📌 Response  
Baga gara Mana Meeshaa Manaa fi Dizaayinii Keessaa Bekansi nagaan dhuftan! 🇪🇹🛋️ Siree mootii Bolee Velvet (Qarshii 85,000) fi Soofaa qananii Entoto L-shape qulqullina olaanaadhaan qopheessinee jra.

📌 Recommended Action  
Halluu fi bal'ina iddoo keessanii filachuudhaan gatii fi dhiyeessa sirrii argadhaa.

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• Lakkoofsa Bilbilaa: ${customerPhone}
• Fedhii: Meeshaa Manaa / Siree fi Soofaa

📌 Missing Information  
• Maqaa Guutuu
• Iddoo Jireenyaa (Finfinnee: Bolee, CMC, kkf)
• Bajata fi Yeroo barbaaddan

📌 Suggested Follow-Up  
Ogeessi dizaayinii keenya iddoo keessan safaruuf yoom haa dhufu?`;
        } else {
            agentReply = `📌 Response  
Welcome to Bekansi Furniture & Interior Design! 🇪🇹✨ We specialize in bespoke luxury furniture handcrafted with imported Turkish fabrics, German Blum soft-close fittings, and solid hardwood framing. Our signature Entoto Luxury Smart Bed (85,000 ETB) and Bole Sectional Sofa (145,000 ETB) are available for immediate custom fabrication.

📌 Recommended Action  
Review our custom fabric variants (Royal Blue or Emerald Green velvet with smart ambient LED and USB-C charging).

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• Phone Number: ${customerPhone}
• Product Interest: ${lower.includes('sofa') ? 'Luxury Sectional Sofa' : 'King Smart Bed / Living Room Furniture'}

📌 Missing Information  
• Full Name
• Addis Ababa Sub-city (Bole, CMC, Sarbet, Kazanchis, etc.)
• Preferred Delivery Timeline

📌 Suggested Follow-Up  
Would you like an official itemized quotation including free delivery and installation within Addis Ababa?`;
        }

        return {
            agentReply,
            leadStatus,
            toolsInvoked,
            language: detectedLang
        };
    }
};

module.exports = agent;
