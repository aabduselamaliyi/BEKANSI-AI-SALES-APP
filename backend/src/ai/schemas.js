/**
 * BEKANSI AI SALES PLATFORM - AI/SCHEMAS.JS
 * Tool / Function Calling Schemas for Gemini AI
 */

const AI_TOOLS_SCHEMA = [
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

module.exports = {
    AI_TOOLS_SCHEMA
};
