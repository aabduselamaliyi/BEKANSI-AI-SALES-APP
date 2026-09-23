/**
 * ============================================================================
 * BEKANSI SALES TOOLS SERVICE
 * ============================================================================
 * Implements the 5 core tools for the Gemini AI Sales Agent:
 * 1. Product Database (Catalog search across Beds, Sofas, Kitchens, etc.)
 * 2. Price Database & Quotation Engine (Strict ETB pricing, delivery, installation)
 * 3. Customer CRM (Contact details, city/subcity, purchase history)
 * 4. Lead CRM (Classification: Hot 🔥, Warm 🟡, Cold ⚪, pipeline updates)
 * 5. Human Handoff (Showroom sales consultant escalation & takeover)
 */

const { pool } = require('../config/dbPool');
const logger = require('../config/logger');

// Verified Bekansi Ethiopian Furniture Catalog & Price Database
const BEKANSI_VERIFIED_PRODUCTS = [
    {
        id: 'BK-BED-001',
        sku: 'BK-BED-ENTOTO',
        name: 'Bole Royal King Velvet Bed',
        name_en: 'Bole Royal King Velvet Bed',
        name_am: 'የቦሌ ሮያል ኪንግ ቬልቬት አልጋ',
        name_om: 'Siree Mootii Bolee Velvet',
        category: 'Beds',
        description: 'King size 200cm x 200cm bed with imported Turkish velvet, acoustic fluted headboard, smart LED ambient lighting, and solid eucalyptus/mahogany frame.',
        active: true,
        price_etb: 85000,
        material: 'Imported Turkish Velvet & Solid Eucalyptus / Mahogany Frame',
        dimensions: '200cm x 200cm (King Size)',
        in_stock: true,
        delivery_estimate: '3-5 business days within Addis Ababa'
    },
    {
        id: 'BK-SOF-001',
        sku: 'BK-SOF-BOLE',
        name: 'Entoto Luxury L-Shape Sectional Sofa',
        name_en: 'Entoto Luxury L-Shape Sectional Sofa',
        name_am: 'የእንጦጦ ቅንጡ L-ቅርጽ ሳሎን ሶፋ',
        name_om: 'Soofaa Qananii Entoto L-Shape',
        category: 'Sofas',
        description: 'High-density foam sectional with stain-resistant imported fabric and kiln-dried hardwood framing, 320cm x 240cm.',
        active: true,
        price_etb: 145000,
        material: 'High-density foam, stain-resistant fabric, kiln-dried hardwood',
        dimensions: '320cm x 240cm',
        in_stock: true,
        delivery_estimate: '4-7 business days'
    },
    {
        id: 'BK-KIT-001',
        sku: 'BK-KIT-MESKEL',
        name: 'Modern High-Gloss Modular Kitchen Cabinets',
        name_en: 'Modern High-Gloss Modular Kitchen Cabinets',
        name_am: 'ዘመናዊ የሚያብረቀርቅ የኩሽና ካቢኔት',
        name_om: 'Kaabineetii Kushiinaa Ammayyaa',
        category: 'Kitchen Cabinets',
        description: 'German UV Acrylic cabinets with Calacatta quartz stone countertop and soft-close Blum hardware.',
        active: true,
        price_etb: 38000, // per linear meter
        price_unit: 'ETB per linear meter',
        material: 'German UV Acrylic, Quartz stone countertop, soft-close Blum hardware',
        in_stock: true,
        delivery_estimate: '10-15 business days (Custom site measurement required)'
    },
    {
        id: 'BK-WAR-001',
        sku: 'BK-WRD-SEMEN',
        name: '6-Door Sliding Mirrored Wardrobe',
        name_en: '6-Door Sliding Mirrored Wardrobe',
        name_am: 'ባለ 6-በር የመስታወት ተንሸራታች ቁምሳጥን',
        name_om: 'Wardaarobii Fuula 6 Qabu',
        category: 'Wardrobes',
        description: 'Scratch-resistant melamine finish, internal motion-sensor LED lighting rails, and soft-closing German sliding mechanism.',
        active: true,
        price_etb: 92000,
        material: 'MDF with Scratch-resistant Melamine finish & LED sensor lights',
        dimensions: '240cm H x 270cm W x 65cm D',
        in_stock: true,
        delivery_estimate: '5-7 business days'
    },
    {
        id: 'BK-DIN-001',
        sku: 'BK-DIN-ENTOTO',
        name: 'Imperial 8-Seater Solid Oak Dining Table Set',
        name_en: 'Imperial 8-Seater Solid Oak Dining Table Set',
        name_am: 'የ8 ሰው የኦክ እንጨት የመመገቢያ ጠረጴዛ ስብስብ',
        name_om: 'Gabatee Nyaataa Nama 8 Mukaa',
        category: 'Dining Tables',
        description: 'Solid Oak & tempered glass accent dining table with 8 padded ergonomic dining chairs.',
        active: true,
        price_etb: 110000,
        material: 'Solid Oak & tempered glass accent with 8 padded ergonomic chairs',
        in_stock: true,
        delivery_estimate: '3-5 business days'
    },
    {
        id: 'BK-OFF-001',
        sku: 'BK-OFF-EXECUTIVE',
        name: 'Executive Presidential Office Desk Suite',
        name_en: 'Executive Presidential Office Desk Suite',
        name_am: 'የፕሬዝዳንታዊ ቢሮ ጠረጴዛ እና ካቢኔት ስብስብ',
        name_om: 'Teessuma Hojii Hogganaa Presidential',
        category: 'Office Furniture',
        description: 'Walnut veneer with genuine leather blotter, integrated wire management, and lockable security credenza.',
        active: true,
        price_etb: 125000,
        material: 'Walnut veneer with leather blotter and integrated wire channels',
        dimensions: '220cm x 100cm desk + 180cm side credenza',
        in_stock: true,
        delivery_estimate: '3-5 business days'
    },
    {
        id: 'BK-TVS-001',
        sku: 'BK-TVS-FINFINE',
        name: 'Floating Marble-Accent TV Console & Wall Panel',
        name_en: 'Floating Marble-Accent TV Console & Wall Panel',
        name_am: 'ተንሳፋፊ የእብነበረድ የቲቪ ማስቀመጫ እና የግድግዳ ጌጥ',
        name_om: 'Koonsolii Televijiinii Ammayyaa',
        category: 'TV Stands',
        description: 'Sintered stone backdrop with acoustic fluted panels and smart warm ambient LED lighting.',
        active: true,
        price_etb: 48000,
        material: 'Sintered stone, acoustic fluted panels, smart ambient LED',
        dimensions: '240cm length',
        in_stock: true,
        delivery_estimate: '2-4 business days'
    }
];

// Verified Bekansi Product Variants (Customization Options)
const BEKANSI_PRODUCT_VARIANTS = [
    {
        id: 'VAR-BED-001',
        product_id: 'BK-BED-001',
        sku: 'BK-BED-ENTOTO',
        size: 'King (180x200cm)',
        color: 'Royal Blue',
        fabric: 'Imported Turkish Velvet',
        finish: 'High Gloss Walnut',
        led_available: true,
        socket_available: true,
        active: true
    },
    {
        id: 'VAR-BED-002',
        product_id: 'BK-BED-001',
        sku: 'BK-BED-ENTOTO',
        size: 'King (180x200cm)',
        color: 'Emerald Green',
        fabric: 'Imported Turkish Velvet',
        finish: 'Matte Oak',
        led_available: true,
        socket_available: true,
        active: true
    },
    {
        id: 'VAR-BED-003',
        product_id: 'BK-BED-001',
        sku: 'BK-BED-ENTOTO',
        size: 'Queen (160x200cm)',
        color: 'Warm Beige',
        fabric: 'Textured Linen Blend',
        finish: 'Natural Teak',
        led_available: true,
        socket_available: false,
        active: true
    },
    {
        id: 'VAR-SOF-001',
        product_id: 'BK-SOF-001',
        sku: 'BK-SOF-BOLE',
        size: '5-Seater L-Shape (320x240cm)',
        color: 'Charcoal Grey',
        fabric: 'Stain-Resistant Turkish Fabric',
        finish: 'Solid Mahogany Feet',
        led_available: true,
        socket_available: false,
        active: true
    },
    {
        id: 'VAR-SOF-002',
        product_id: 'BK-SOF-001',
        sku: 'BK-SOF-BOLE',
        size: '5-Seater L-Shape (320x240cm)',
        color: 'Navy Blue',
        fabric: 'Water-Repellent Velvet',
        finish: 'Brushed Brass Feet',
        led_available: true,
        socket_available: false,
        active: true
    },
    {
        id: 'VAR-WRD-001',
        product_id: 'BK-WAR-001',
        sku: 'BK-WRD-SEMEN',
        size: '6-Door (240x270x65cm)',
        color: 'Matte White & Grey Glass',
        fabric: 'Melamine Carcass',
        finish: 'Scratch-Proof Matte',
        led_available: true,
        socket_available: false,
        active: true
    },
    {
        id: 'VAR-DIN-001',
        product_id: 'BK-DIN-001',
        sku: 'BK-DIN-ENTOTO',
        size: '8-Seater (220x100cm)',
        color: 'Calacatta Gold White Marble',
        fabric: 'High-Density Velvet Chairs',
        finish: 'Solid Oak & Brass',
        led_available: false,
        socket_available: false,
        active: true
    },
    {
        id: 'VAR-TVS-001',
        product_id: 'BK-TVS-001',
        sku: 'BK-TVS-FINFINE',
        size: '240cm Wall Unit',
        color: 'Grey Sintered Stone & Walnut',
        fabric: 'Acoustic Felt Slats',
        finish: 'Matte Lacquer',
        led_available: true,
        socket_available: true,
        active: true
    }
];

// Verified Bekansi Price Records matching prices table schema
const BEKANSI_PRICES = [
    // Base Product Prices
    {
        id: 'PRC-BED-BASE',
        product_id: 'BK-BED-001',
        variant_id: null,
        price: 85000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-SOF-BASE',
        product_id: 'BK-SOF-001',
        variant_id: null,
        price: 145000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-KIT-BASE',
        product_id: 'BK-KIT-001',
        variant_id: null,
        price: 38000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-WAR-BASE',
        product_id: 'BK-WAR-001',
        variant_id: null,
        price: 92000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-DIN-BASE',
        product_id: 'BK-DIN-001',
        variant_id: null,
        price: 110000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-OFF-BASE',
        product_id: 'BK-OFF-001',
        variant_id: null,
        price: 125000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-TVS-BASE',
        product_id: 'BK-TVS-001',
        variant_id: null,
        price: 48000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    // Variant Specific Prices
    {
        id: 'PRC-BED-VAR1',
        product_id: 'BK-BED-001',
        variant_id: 'VAR-BED-001',
        price: 88000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-BED-VAR2',
        product_id: 'BK-BED-001',
        variant_id: 'VAR-BED-002',
        price: 88000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-BED-VAR3',
        product_id: 'BK-BED-001',
        variant_id: 'VAR-BED-003',
        price: 78000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    },
    {
        id: 'PRC-SOF-VAR2',
        product_id: 'BK-SOF-001',
        variant_id: 'VAR-SOF-002',
        price: 152000,
        currency: 'ETB',
        delivery_included: false,
        effective_from: new Date().toISOString(),
        effective_until: null,
        active: true
    }
];

// In-Memory fallback store for CRM and Leads when DB pool is disconnected
const memoryStore = {
    customers: new Map(),
    leads: new Map(),
    handoffs: new Map(),
    conversations: new Map(),
    messages: [],
    variants: BEKANSI_PRODUCT_VARIANTS,
    prices: BEKANSI_PRICES,
    aiToolLogs: []
};

const crmToolsService = {

    /**
     * Tool 1: Product Database
     * Search Bekansi Furniture catalog by keyword, category, room type, or budget.
     */
    searchProductDatabase: async ({ query = '', category = '', maxPrice = null, locale = 'en' }) => {
        logger.info('Tool Executed: searchProductDatabase', { query, category, maxPrice, locale });
        
        try {
            // Check database if available
            if (pool) {
                try {
                    let sql = 'SELECT * FROM products WHERE 1=1';
                    const params = [];
                    if (category) {
                        params.push(`%${category}%`);
                        sql += ` AND category ILIKE $${params.length}`;
                    }
                    if (query) {
                        params.push(`%${query}%`);
                        sql += ` AND (name ILIKE $${params.length} OR description ILIKE $${params.length})`;
                    }
                    if (maxPrice) {
                        params.push(maxPrice);
                        sql += ` AND price <= $${params.length}`;
                    }
                    sql += ' LIMIT 10';
                    const dbRes = await pool.query(sql, params);
                    if (dbRes.rows && dbRes.rows.length > 0) {
                        return { source: 'database', count: dbRes.rows.length, products: dbRes.rows };
                    }
                } catch (dbErr) {
                    logger.warn('Database query failed in searchProductDatabase, using verified catalog fallback', { error: dbErr.message });
                }
            }

            // High-fidelity fallback catalog
            const filtered = BEKANSI_VERIFIED_PRODUCTS.filter(item => {
                const matchCategory = !category || item.category.toLowerCase().includes(category.toLowerCase());
                const matchQuery = !query || 
                    item.name_en.toLowerCase().includes(query.toLowerCase()) ||
                    item.name_am.includes(query) ||
                    item.name_om.toLowerCase().includes(query.toLowerCase()) ||
                    item.material.toLowerCase().includes(query.toLowerCase());
                const matchPrice = !maxPrice || item.price_etb <= maxPrice;
                return matchCategory && matchQuery && matchPrice;
            });

            const result = {
                source: 'bekansi_catalog_cache',
                count: filtered.length,
                products: filtered.length > 0 ? filtered : BEKANSI_VERIFIED_PRODUCTS.slice(0, 3)
            };
            await crmToolsService.logAiToolExecution({
                toolName: 'searchProductDatabase',
                input: { query, category, maxPrice, locale },
                output: { count: result.count, sample: (result.products[0] || {}).name_en }
            });
            return result;
        } catch (error) {
            logger.error('Error in searchProductDatabase', { error: error.message });
            await crmToolsService.logAiToolExecution({
                toolName: 'searchProductDatabase',
                input: { query, category, maxPrice, locale },
                output: null,
                status: 'ERROR',
                errorMessage: error.message
            });
            return { source: 'fallback', count: 0, products: [], error: error.message };
        }
    },

    /**
     * Tool 2: Price Database & Quotation Calculation
     * Computes verified ETB quotation with product price, delivery, and installation.
     */
    checkPriceAndQuotation: async ({
        customerName = 'Valued Customer',
        customerPhone = '',
        items = [],
        deliveryCity = 'Addis Ababa',
        deliverySubcity = 'Bole',
        includeInstallation = true
    }) => {
        logger.info('Tool Executed: checkPriceAndQuotation', { customerName, customerPhone, items, deliveryCity });

        let subtotal = 0;
        const lineItems = [];

        for (const reqItem of items) {
            const product = BEKANSI_VERIFIED_PRODUCTS.find(p => 
                p.id === reqItem.productId || 
                p.name_en.toLowerCase().includes((reqItem.name || '').toLowerCase()) ||
                p.category.toLowerCase() === (reqItem.category || '').toLowerCase()
            );

            if (product) {
                const qty = reqItem.quantity || 1;
                const lineTotal = product.price_etb * qty;
                subtotal += lineTotal;
                lineItems.push({
                    productId: product.id,
                    name: product.name_en,
                    nameAm: product.name_am,
                    unitPriceEtb: product.price_etb,
                    quantity: qty,
                    material: product.material,
                    lineTotalEtb: lineTotal
                });
            } else if (reqItem.unitPrice) {
                // If custom item with given price
                const qty = reqItem.quantity || 1;
                const lineTotal = reqItem.unitPrice * qty;
                subtotal += lineTotal;
                lineItems.push({
                    productId: 'CUSTOM',
                    name: reqItem.name || 'Custom Bekansi Furniture',
                    unitPriceEtb: reqItem.unitPrice,
                    quantity: qty,
                    lineTotalEtb: lineTotal
                });
            }
        }

        // Delivery calculation rules within Ethiopia
        let deliveryCost = 2500; // Standard inside Addis Ababa (Bole, CMC, Sarbet, Kazanchis, etc.)
        if (deliveryCity.toLowerCase() !== 'addis ababa') {
            deliveryCost = 9000; // Regional delivery (Hawassa, Adama, Bishoftu, Bahir Dar)
        }

        const installationCost = includeInstallation ? (subtotal > 100000 ? 0 : 3500) : 0; // Free installation for orders > 100,000 ETB
        const totalEstimate = subtotal + deliveryCost + installationCost;
        const quoteNumber = `BKQ-${Date.now().toString().slice(-6)}`;

        const quotation = {
            quoteNumber,
            customerName,
            customerPhone,
            deliveryLocation: `${deliverySubcity}, ${deliveryCity}`,
            lineItems,
            subtotalEtb: subtotal,
            deliveryCostEtb: deliveryCost,
            installationCostEtb: installationCost,
            totalEstimateEtb: totalEstimate,
            currency: 'ETB (Ethiopian Birr)',
            validityDays: 14,
            validUntil: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            paymentTerms: '50% advance upon contract signing, 50% upon delivery & professional installation'
        };

        await crmToolsService.logAiToolExecution({
            toolName: 'checkPriceAndQuotation',
            input: { customerName, customerPhone, items, deliveryCity, deliverySubcity, includeInstallation },
            output: { quoteNumber, totalEstimateEtb: totalEstimate, itemsCount: lineItems.length }
        });

        return quotation;
    },

    /**
     * Tool 3: Customer CRM
     * Retrieves or registers customer profile and contact details matching Supabase table schema.
     */
    upsertCustomerCRM: async ({
        fullName,
        phoneNumber,
        whatsappUserId = null,
        preferredLanguage = 'en',
        customerType = 'B2C',
        city = 'Addis Ababa',
        subCity = 'Bole',
        woreda = '',
        deliveryLocation = '',
        source = 'whatsapp_inbound',
        campaign = '2026_luxury_ethiopia',
        adId = null,
        status = 'active'
    }) => {
        logger.info('Tool Executed: upsertCustomerCRM', { fullName, phoneNumber, city, subCity });

        const cleanPhone = (phoneNumber || '').replace(/[^0-9+]/g, '');
        const existing = memoryStore.customers.get(cleanPhone) || {
            id: `CUST-${Date.now().toString().slice(-5)}`,
            created_at: new Date().toISOString()
        };

        const updated = {
            ...existing,
            full_name: fullName || existing.full_name || 'Valued Bekansi Customer',
            phone: cleanPhone,
            whatsapp_user_id: whatsappUserId || existing.whatsapp_user_id || cleanPhone,
            preferred_language: preferredLanguage || existing.preferred_language || 'en',
            customer_type: customerType || existing.customer_type || 'B2C',
            city: city || existing.city || 'Addis Ababa',
            sub_city: subCity || existing.sub_city || 'Bole',
            woreda: woreda || existing.woreda || '',
            delivery_location: deliveryLocation || `${subCity}, ${city}`,
            source: source || existing.source || 'whatsapp_inbound',
            campaign: campaign || existing.campaign || '2026_luxury_ethiopia',
            ad_id: adId || existing.ad_id,
            status: status || existing.status || 'active',
            updated_at: new Date().toISOString()
        };

        memoryStore.customers.set(cleanPhone, updated);

        // Also persist in PostgreSQL / Supabase if available
        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO customers (
                        full_name, phone, whatsapp_user_id, preferred_language, customer_type,
                        city, sub_city, woreda, delivery_location, source, campaign, ad_id, status, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, NOW())
                    ON CONFLICT (phone) 
                    DO UPDATE SET 
                        full_name = COALESCE(EXCLUDED.full_name, customers.full_name),
                        city = COALESCE(EXCLUDED.city, customers.city),
                        sub_city = COALESCE(EXCLUDED.sub_city, customers.sub_city),
                        delivery_location = COALESCE(EXCLUDED.delivery_location, customers.delivery_location),
                        updated_at = NOW()
                `, [
                    updated.full_name, updated.phone, updated.whatsapp_user_id, updated.preferred_language,
                    updated.customer_type, updated.city, updated.sub_city, updated.woreda,
                    updated.delivery_location, updated.source, updated.campaign, updated.ad_id, updated.status
                ]);
            } catch (dbErr) {
                logger.warn('Could not persist customer to Postgres, kept in memory store', { error: dbErr.message });
            }
        }

        await crmToolsService.logAiToolExecution({
            toolName: 'upsertCustomerCRM',
            input: { fullName, phone: cleanPhone, city, subCity },
            output: { customerId: updated.id, status: 'UPSERTED', phone: cleanPhone }
        });

        return { success: true, customer: updated };
    },

    /**
     * Tool 4: Lead CRM
     * Classifies and updates customer lead pipeline status matching leads table schema:
     * - 🔥 Hot Lead / HOT: Ready to buy, requested quote/measurement, specific timeline
     * - 🟡 Warm Lead / WARM: Interested in specific products, evaluating budget
     * - ⚪ Cold Lead / COLD: General browsing or casual inquiry
     */
    classifyAndSaveLead: async ({
        customerPhone,
        customerName,
        customerId = null,
        conversationId = null,
        productId = null,
        productCategory = null,
        productInterest = null,
        quantity = 1,
        size = null,
        color = null,
        fabric = null,
        finish = null,
        ledRequired = false,
        socketRequired = false,
        budgetMin = null,
        budgetMax = null,
        budgetRange = null,
        purchaseTimeline = null,
        deliveryTimeline = null,
        leadStage = 'NEW',
        leadScore = null,
        leadTemperature = null,
        leadStatus = '🟡 Warm Lead',
        assignedTo = null,
        nextFollowupAt = null,
        missingInformation = []
    }) => {
        logger.info('Tool Executed: classifyAndSaveLead', { customerPhone, customerName, leadStatus, productInterest, productCategory });

        const cleanPhone = (customerPhone || '').replace(/[^0-9+]/g, '');
        const leadId = `LEAD-${Date.now().toString().slice(-6)}`;

        // Normalize lead temperature: 'HOT' | 'WARM' | 'COLD'
        let temperature = 'WARM';
        if (leadTemperature) {
            temperature = leadTemperature.toUpperCase();
        } else if (typeof leadStatus === 'string') {
            if (leadStatus.includes('Hot') || leadStatus.includes('🔥')) temperature = 'HOT';
            else if (leadStatus.includes('Cold') || leadStatus.includes('⚪')) temperature = 'COLD';
            else temperature = 'WARM';
        }

        // Determine dynamic lead score based on temperature and information collected
        let score = leadScore;
        if (score === null || score === undefined) {
            if (temperature === 'HOT') score = 85;
            else if (temperature === 'WARM') score = 55;
            else score = 25;
        }

        const category = productCategory || productInterest || 'Beds';
        const timeline = purchaseTimeline || deliveryTimeline || 'Within 2-4 weeks';

        // Auto-resolve customer_id and conversation_id from memory store if not passed
        const resolvedCustomer = memoryStore.customers.get(cleanPhone);
        const resolvedConv = memoryStore.conversations.get(cleanPhone);

        const leadRecord = {
            id: leadId,
            customer_id: customerId || (resolvedCustomer ? resolvedCustomer.id : null),
            conversation_id: conversationId || (resolvedConv ? resolvedConv.id : null),
            product_id: productId,
            product_category: category,
            quantity: Number(quantity) || 1,
            size,
            color,
            fabric,
            finish,
            led_required: Boolean(ledRequired),
            socket_required: Boolean(socketRequired),
            budget_min: budgetMin,
            budget_max: budgetMax,
            purchase_timeline: timeline,
            lead_stage: leadStage || (temperature === 'HOT' ? 'QUALIFIED' : 'NEW'),
            lead_score: score,
            lead_temperature: temperature, // 'HOT', 'WARM', 'COLD'
            assigned_to: assignedTo,
            next_followup_at: nextFollowupAt || new Date(Date.now() + 24 * 3600 * 1000).toISOString(),
            last_followup_at: null,
            lost_reason: null,
            // Convenience legacy fields for API responses
            customerPhone: cleanPhone,
            customerName: customerName || 'Prospective Client',
            leadStatus: temperature === 'HOT' ? '🔥 Hot Lead' : (temperature === 'WARM' ? '🟡 Warm Lead' : '⚪ Cold Lead'),
            missingInformation,
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryStore.leads.set(cleanPhone, leadRecord);

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO leads (
                        customer_id, conversation_id, product_id, product_category,
                        quantity, size, color, fabric, finish,
                        led_required, socket_required, budget_min, budget_max,
                        purchase_timeline, lead_stage, lead_score, lead_temperature,
                        assigned_to, next_followup_at, created_at, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, NOW(), NOW())
                `, [
                    leadRecord.customer_id, leadRecord.conversation_id, leadRecord.product_id, leadRecord.product_category,
                    leadRecord.quantity, leadRecord.size, leadRecord.color, leadRecord.fabric, leadRecord.finish,
                    leadRecord.led_required, leadRecord.socket_required, leadRecord.budget_min, leadRecord.budget_max,
                    leadRecord.purchase_timeline, leadRecord.lead_stage, leadRecord.lead_score, leadRecord.lead_temperature,
                    leadRecord.assigned_to, leadRecord.next_followup_at
                ]);
            } catch (dbErr) {
                logger.warn('Could not persist lead to Postgres, stored in memory', { error: dbErr.message });
            }
        }

        await crmToolsService.logAiToolExecution({
            toolName: 'classifyAndSaveLead',
            input: { customerPhone: cleanPhone, customerName, productCategory: category, leadTemperature: temperature, leadStage },
            output: { leadId, leadTemperature: temperature, leadScore: score }
        });

        return { success: true, lead: leadRecord };
    },

    /**
     * Tool 5: Human Handoff
     * Escalates complex, custom luxury architectural design or high-value inquiries
     * directly to a human showroom sales executive in Addis Ababa.
     */
    requestHumanHandoff: async ({
        customerPhone,
        customerName,
        reason = 'Customer requested human consultant or complex custom architectural quote',
        summary = '',
        urgency = 'HIGH'
    }) => {
        logger.info('Tool Executed: requestHumanHandoff', { customerPhone, customerName, reason, urgency });

        const cleanPhone = customerPhone.replace(/[^0-9+]/g, '');
        const handoffTicket = {
            ticketId: `HANDOFF-${Date.now().toString().slice(-5)}`,
            customerPhone: cleanPhone,
            customerName: customerName || 'Valued Client',
            reason,
            summary,
            urgency,
            assignedShowroom: 'Bekansi Bole Showroom, Addis Ababa',
            assignedConsultantPhone: '+251 91 100 2233',
            status: 'PENDING_SHOWROOM_AGENT',
            timestamp: new Date().toISOString()
        };

        memoryStore.handoffs.set(cleanPhone, handoffTicket);

        await crmToolsService.logAiToolExecution({
            toolName: 'requestHumanHandoff',
            input: { customerPhone: cleanPhone, customerName, reason, urgency },
            output: { ticketId: handoffTicket.ticketId, assignedShowroom: handoffTicket.assignedShowroom }
        });

        return {
            success: true,
            message: 'Human sales agent has been notified and ticket dispatched to Bekansi Bole Showroom.',
            ticket: handoffTicket
        };
    },

    isHandoffActive: (customerPhone) => {
        const cleanPhone = (customerPhone || '').replace(/[^0-9+]/g, '');
        const ticket = memoryStore.handoffs.get(cleanPhone);
        return ticket && ticket.status === 'PENDING_SHOWROOM_AGENT';
    },

    resolveHandoff: (customerPhone) => {
        const cleanPhone = (customerPhone || '').replace(/[^0-9+]/g, '');
        if (memoryStore.handoffs.has(cleanPhone)) {
            const ticket = memoryStore.handoffs.get(cleanPhone);
            ticket.status = 'RESOLVED';
            memoryStore.handoffs.set(cleanPhone, ticket);
            return true;
        }
        return false;
    },

    logAiToolExecution: async ({ conversationId = null, messageId = null, toolName, input, output, executionTimeMs = 15, status = 'SUCCESS', errorMessage = null }) => {
        const logEntry = {
            id: `TOOL-${Date.now()}-${Math.floor(100 + Math.random() * 900)}`,
            conversationId,
            messageId,
            toolName,
            toolInput: input,
            toolOutput: output,
            executionTimeMs,
            status,
            errorMessage,
            createdAt: new Date().toISOString()
        };
        memoryStore.aiToolLogs.unshift(logEntry);
        if (memoryStore.aiToolLogs.length > 200) memoryStore.aiToolLogs.pop();

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO ai_tool_logs (id, conversation_id, message_id, tool_name, tool_input, tool_output, execution_time_ms, status, error_message, created_at)
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, NOW())
                `, [logEntry.id, conversationId, messageId, toolName, JSON.stringify(input), JSON.stringify(output), executionTimeMs, status, errorMessage]);
            } catch (err) {
                // Non-blocking telemetry
            }
        }
        return logEntry;
    },

    /**
     * Tool 6: Conversation Management
     * Manages conversation lifecycle matching conversations table schema
     */
    getOrCreateConversation: async ({
        customerId,
        whatsappPhone,
        channel = 'whatsapp',
        language = 'en',
        leadStage = 'NEW'
    }) => {
        const cleanPhone = (whatsappPhone || '').replace(/[^0-9+]/g, '');
        const existing = memoryStore.conversations.get(cleanPhone);

        if (existing) {
            existing.last_message_at = new Date().toISOString();
            existing.last_customer_message_at = new Date().toISOString();
            existing.updated_at = new Date().toISOString();
            memoryStore.conversations.set(cleanPhone, existing);
            return existing;
        }

        const newConv = {
            id: `CONV-${Date.now()}-${Math.floor(100 + Math.random() * 900)}`,
            customer_id: customerId,
            channel,
            whatsapp_phone: cleanPhone,
            status: 'open',
            language,
            ai_enabled: true,
            human_assigned: false,
            assigned_agent: null,
            lead_stage: leadStage,
            last_message_at: new Date().toISOString(),
            last_customer_message_at: new Date().toISOString(),
            last_ai_message_at: null,
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryStore.conversations.set(cleanPhone, newConv);

        if (pool) {
            try {
                const res = await pool.query(`
                    INSERT INTO conversations (
                        customer_id, channel, whatsapp_phone, status, language,
                        ai_enabled, human_assigned, assigned_agent, lead_stage,
                        last_message_at, last_customer_message_at, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, NOW(), NOW(), NOW())
                    RETURNING *
                `, [customerId, channel, cleanPhone, 'open', language, true, false, null, leadStage]);
                if (res.rows && res.rows[0]) {
                    newConv.id = res.rows[0].id;
                }
            } catch (err) {
                logger.warn('Could not insert into conversations table, kept in memory', { error: err.message });
            }
        }

        return newConv;
    },

    updateConversationAiMessage: async (whatsappPhone, leadStage = null) => {
        const cleanPhone = (whatsappPhone || '').replace(/[^0-9+]/g, '');
        const conv = memoryStore.conversations.get(cleanPhone);
        if (conv) {
            conv.last_message_at = new Date().toISOString();
            conv.last_ai_message_at = new Date().toISOString();
            conv.updated_at = new Date().toISOString();
            if (leadStage) conv.lead_stage = leadStage;
            memoryStore.conversations.set(cleanPhone, conv);
        }

        if (pool) {
            try {
                await pool.query(`
                    UPDATE conversations
                    SET last_message_at = NOW(),
                        last_ai_message_at = NOW(),
                        lead_stage = COALESCE($1, lead_stage),
                        updated_at = NOW()
                    WHERE whatsapp_phone = $2
                `, [leadStage, cleanPhone]);
            } catch (err) {
                // Non-blocking
            }
        }
    },

    /**
     * Tool 7: Message Recording
     * Records inbound customer and outbound AI/human messages matching messages table schema
     */
    recordMessage: async ({
        conversationId,
        whatsappMessageId = null,
        direction, // 'inbound' | 'outbound'
        senderType = 'customer', // 'customer' | 'ai' | 'agent'
        messageType = 'text',
        text,
        mediaId = null,
        mediaUrl = null,
        mimeType = null,
        intent = null,
        aiGenerated = false,
        aiModel = null,
        aiConfidence = 0.95,
        toolCalled = null,
        toolArguments = null,
        toolResult = null,
        deliveryStatus = 'received'
    }) => {
        const msgEntry = {
            id: `MSG-${Date.now()}-${Math.floor(100 + Math.random() * 900)}`,
            conversation_id: conversationId,
            whatsapp_message_id: whatsappMessageId || `wa_${Date.now()}`,
            direction,
            sender_type: senderType,
            message_type: messageType,
            text,
            media_id: mediaId,
            media_url: mediaUrl,
            mime_type: mimeType,
            intent,
            ai_generated: aiGenerated,
            ai_model: aiModel,
            ai_confidence: aiConfidence,
            tool_called: toolCalled,
            tool_arguments: toolArguments,
            tool_result: toolResult,
            delivery_status: deliveryStatus,
            created_at: new Date().toISOString()
        };

        memoryStore.messages.unshift(msgEntry);
        if (memoryStore.messages.length > 500) memoryStore.messages.pop();

        if (pool) {
            try {
                const res = await pool.query(`
                    INSERT INTO messages (
                        conversation_id, whatsapp_message_id, direction, sender_type,
                        message_type, text, media_id, media_url, mime_type,
                        intent, ai_generated, ai_model, ai_confidence,
                        tool_called, tool_arguments, tool_result, delivery_status, created_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, NOW())
                    RETURNING *
                `, [
                    conversationId, msgEntry.whatsapp_message_id, direction, senderType,
                    messageType, text, mediaId, mediaUrl, mimeType,
                    intent, aiGenerated, aiModel, aiConfidence,
                    toolCalled,
                    toolArguments ? JSON.stringify(toolArguments) : null,
                    toolResult ? JSON.stringify(toolResult) : null,
                    deliveryStatus
                ]);
                if (res.rows && res.rows[0]) {
                    msgEntry.id = res.rows[0].id;
                }
            } catch (err) {
                logger.warn('Could not insert into messages table, kept in memory store', { error: err.message });
            }
        }

        return msgEntry;
    },

    /**
     * Tool 8: Product Variants
     * Retrieves customizable variants (size, color, fabric, finish, led, socket) for a product
     */
    getProductVariants: async ({ productId = null, sku = null }) => {
        if (pool) {
            try {
                let sql = 'SELECT * FROM product_variants WHERE active = true';
                const params = [];
                if (productId) {
                    params.push(productId);
                    sql += ` AND product_id = $${params.length}`;
                }
                const res = await pool.query(sql, params);
                if (res.rows && res.rows.length > 0) {
                    return res.rows;
                }
            } catch (err) {
                logger.warn('Could not query product_variants from DB, using memory fallback', { error: err.message });
            }
        }

        return memoryStore.variants.filter(v => {
            if (productId && v.product_id === productId) return true;
            if (sku && v.sku === sku) return true;
            return !productId && !sku;
        });
    },

    /**
     * Tool 9: Price Lookup
     * Look up active ETB price for a product or specific variant
     */
    getPriceRecord: async ({ productId = null, variantId = null }) => {
        if (pool) {
            try {
                let sql = 'SELECT * FROM prices WHERE active = true';
                const params = [];
                if (variantId) {
                    params.push(variantId);
                    sql += ` AND variant_id = $${params.length}`;
                } else if (productId) {
                    params.push(productId);
                    sql += ` AND product_id = $${params.length} AND variant_id IS NULL`;
                }
                sql += ' ORDER BY effective_from DESC LIMIT 1';
                const res = await pool.query(sql, params);
                if (res.rows && res.rows.length > 0) {
                    return res.rows[0];
                }
            } catch (err) {
                logger.warn('Could not query prices from DB, using memory fallback', { error: err.message });
            }
        }

        const found = memoryStore.prices.find(p => {
            if (!p.active) return false;
            if (variantId && p.variant_id === variantId) return true;
            if (!variantId && productId && p.product_id === productId && !p.variant_id) return true;
            return false;
        });

        return found || memoryStore.prices[0];
    },

    getAllHandoffs: () => Array.from(memoryStore.handoffs.values()),
    getAllLeads: () => Array.from(memoryStore.leads.values()),
    getAllCustomers: () => Array.from(memoryStore.customers.values()),
    getAllConversations: () => Array.from(memoryStore.conversations.values()),
    getAllMessages: () => memoryStore.messages,
    getAllProducts: () => BEKANSI_VERIFIED_PRODUCTS,
    getAllProductVariants: () => memoryStore.variants,
    getAllPrices: () => memoryStore.prices,
    getAllAiToolLogs: () => memoryStore.aiToolLogs
};

module.exports = crmToolsService;
