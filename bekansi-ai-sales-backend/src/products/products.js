/**
 * BEKANSI AI SALES PLATFORM - PRODUCTS/PRODUCTS.JS (ESM)
 * Master Catalog, Product Variants, and Pricing & Quotation Engine
 */

import { pool } from '../config/database.js';
import logger from '../config/logger.js';

// Verified In-Memory Product Master Data matching the products table
export const MASTER_PRODUCTS = [
    {
        id: '11111111-1111-4111-8111-111111111111',
        sku: 'BK-BED-ENTOTO',
        name: 'Entoto Luxury Smart Bed',
        category: 'Beds',
        description: 'King size 180x200cm bed with imported Turkish velvet, acoustic fluted headboard, smart LED ambient lighting, and solid eucalyptus/mahogany frame.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '22222222-2222-4222-8222-222222222222',
        sku: 'BK-SOF-BOLE',
        name: 'Bole Luxury Sectional L-Shape Sofa',
        category: 'Sofas',
        description: 'High-density foam sectional with stain-resistant imported fabric and kiln-dried hardwood framing, 320cm x 240cm.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '33333333-3333-4333-8333-333333333333',
        sku: 'BK-KIT-MESKEL',
        name: 'Meskel Luxury Custom Kitchen Cabinet',
        category: 'Kitchen Cabinets',
        description: 'German UV Acrylic cabinets with Calacatta quartz stone countertop and soft-close Blum hardware.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '44444444-4444-4444-8444-444444444444',
        sku: 'BK-WRD-SEMEN',
        name: 'Semen 6-Door Walk-In Wardrobe',
        category: 'Wardrobes',
        description: 'Scratch-resistant melamine finish, internal motion-sensor LED lighting rails, and soft-closing German sliding mechanism.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '55555555-5555-4555-8555-555555555555',
        sku: 'BK-DIN-ENTOTO',
        name: 'Entoto Marble Dining Set (8-Seater)',
        category: 'Dining Tables',
        description: 'Solid Oak & sintered marble accent dining table with 8 padded ergonomic dining chairs.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '66666666-6666-4666-8666-666666666666',
        sku: 'BK-OFF-EXECUTIVE',
        name: 'Addis Executive Office Desk & Credenza',
        category: 'Office Furniture',
        description: 'Walnut veneer with genuine leather blotter, integrated wire management, and lockable security credenza.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    },
    {
        id: '77777777-7777-4777-8777-777777777777',
        sku: 'BK-TVS-FINFINE',
        name: 'Finfine Floating Wall Media Unit',
        category: 'TV Stands',
        description: 'Sintered stone backdrop with acoustic fluted panels and smart warm ambient LED lighting.',
        active: true,
        created_at: new Date().toISOString(),
        updated_at: new Date().toISOString()
    }
];

export const MASTER_VARIANTS = [
    {
        id: 'var-bed-01',
        product_id: '11111111-1111-4111-8111-111111111111',
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
        id: 'var-bed-02',
        product_id: '11111111-1111-4111-8111-111111111111',
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
        id: 'var-sof-01',
        product_id: '22222222-2222-4222-8222-222222222222',
        sku: 'BK-SOF-BOLE',
        size: '5-Seater L-Shape (320x240cm)',
        color: 'Charcoal Grey',
        fabric: 'Stain-Resistant Turkish Fabric',
        finish: 'Solid Mahogany Feet',
        led_available: true,
        socket_available: false,
        active: true
    }
];

export const MASTER_PRICES = [
    { sku: 'BK-BED-ENTOTO', base_price: 85000, variant_prices: { 'Royal Blue': 88000, 'Emerald Green': 88000, 'Warm Beige': 78000 } },
    { sku: 'BK-SOF-BOLE', base_price: 145000, variant_prices: { 'Charcoal Grey': 145000, 'Navy Blue': 152000 } },
    { sku: 'BK-KIT-MESKEL', base_price: 38000, unit: 'ETB per linear meter' },
    { sku: 'BK-WRD-SEMEN', base_price: 92000 },
    { sku: 'BK-DIN-ENTOTO', base_price: 110000 },
    { sku: 'BK-OFF-EXECUTIVE', base_price: 125000 },
    { sku: 'BK-TVS-FINFINE', base_price: 48000 }
];

export const ADDIS_ABABA_DELIVERY_RATES = {
    'Bole': 1500,
    'CMC': 2000,
    'Kazanchis': 1500,
    'Sarbet': 1800,
    'Old Airport': 1800,
    'Piazza': 2000,
    'Default_Addis': 2000,
    'Outside_Addis': 5000
};

export const productService = {
    getAllProducts: async () => {
        if (pool) {
            try {
                const res = await pool.query('SELECT * FROM products WHERE active = true ORDER BY created_at ASC');
                if (res.rows && res.rows.length > 0) return res.rows;
            } catch (err) {
                logger.warn('Failed to query products from Postgres, returning master fallback', { error: err.message });
            }
        }
        return MASTER_PRODUCTS;
    },

    getProductBySku: async (sku) => {
        if (pool) {
            try {
                const res = await pool.query('SELECT * FROM products WHERE sku = $1 AND active = true', [sku]);
                if (res.rows && res.rows.length > 0) return res.rows[0];
            } catch (err) {
                logger.warn('Failed to query product by SKU from Postgres', { sku, error: err.message });
            }
        }
        return MASTER_PRODUCTS.find(p => p.sku.toLowerCase() === sku.toLowerCase()) || null;
    },

    searchProducts: async ({ query = '', category = '' }) => {
        if (pool) {
            try {
                let sql = 'SELECT * FROM products WHERE active = true';
                const params = [];
                if (category) {
                    params.push(`%${category}%`);
                    sql += ` AND category ILIKE $${params.length}`;
                }
                if (query) {
                    params.push(`%${query}%`);
                    sql += ` AND (name ILIKE $${params.length} OR description ILIKE $${params.length})`;
                }
                const res = await pool.query(sql, params);
                if (res.rows && res.rows.length > 0) return res.rows;
            } catch (err) {
                logger.warn('Failed to search products in Postgres, using memory search', { error: err.message });
            }
        }

        return MASTER_PRODUCTS.filter(p => {
            const matchCat = !category || p.category.toLowerCase().includes(category.toLowerCase());
            const matchQ = !query || p.name.toLowerCase().includes(query.toLowerCase()) || p.description.toLowerCase().includes(query.toLowerCase());
            return matchCat && matchQ;
        });
    },

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
                if (res.rows && res.rows.length > 0) return res.rows;
            } catch (err) {
                logger.warn('Failed to query product variants from DB', { error: err.message });
            }
        }
        return MASTER_VARIANTS.filter(v => {
            if (productId && v.product_id === productId) return true;
            if (sku && v.sku === sku) return true;
            return !productId && !sku;
        });
    },

    getPriceForProduct: async (sku, color = null) => {
        const productPricing = MASTER_PRICES.find(p => p.sku.toLowerCase() === sku.toLowerCase());
        if (!productPricing) return null;

        if (color && productPricing.variant_prices && productPricing.variant_prices[color]) {
            return {
                price: productPricing.variant_prices[color],
                currency: 'ETB',
                isVariant: true,
                color
            };
        }

        return {
            price: productPricing.base_price,
            currency: 'ETB',
            isVariant: false,
            unit: productPricing.unit || 'ETB'
        };
    },

    calculateDelivery: (location = 'Bole') => {
        if (!location) return ADDIS_ABABA_DELIVERY_RATES.Default_Addis;
        const matched = Object.keys(ADDIS_ABABA_DELIVERY_RATES).find(loc => 
            location.toLowerCase().includes(loc.toLowerCase())
        );
        return matched ? ADDIS_ABABA_DELIVERY_RATES[matched] : ADDIS_ABABA_DELIVERY_RATES.Default_Addis;
    },

    generateQuotation: async ({
        customerName,
        customerPhone,
        productName,
        sku,
        quantity = 1,
        materialType = 'Imported Turkish Velvet / Solid Hardwood Frame',
        color = null,
        deliveryLocation = 'Addis Ababa',
        includeInstallation = true
    }) => {
        const priceInfo = await productService.getPriceForProduct(sku, color);
        if (!priceInfo) {
            return {
                success: false,
                message: `Pricing for ${productName || sku} requires custom measurement or verification with showroom staff.`
            };
        }

        const unitPrice = priceInfo.price;
        const totalItemsPrice = unitPrice * quantity;
        const deliveryCost = productService.calculateDelivery(deliveryLocation);
        const installationCost = includeInstallation ? (totalItemsPrice > 100000 ? 0 : 2500) : 0;
        const grandTotal = totalItemsPrice + deliveryCost + installationCost;

        const quoteNumber = `QT-${Date.now().toString().slice(-6)}`;
        const validUntil = new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toLocaleDateString('en-GB');

        const quotation = {
            quoteNumber,
            customerName: customerName || 'Valued Customer',
            customerPhone,
            productName,
            sku,
            quantity,
            materialType,
            unitPrice: `${unitPrice.toLocaleString()} ETB`,
            itemsTotal: `${totalItemsPrice.toLocaleString()} ETB`,
            deliveryCost: `${deliveryCost.toLocaleString()} ETB (${deliveryLocation})`,
            installationCost: installationCost === 0 ? 'FREE (Promotional Offer)' : `${installationCost.toLocaleString()} ETB`,
            totalEstimate: `${grandTotal.toLocaleString()} ETB`,
            validityPeriod: `10 Days (Valid until ${validUntil})`,
            paymentTerms: '50% advance upon order confirmation, 50% upon final delivery & inspection in Addis Ababa'
        };

        return {
            success: true,
            quotation
        };
    },

    getAllPrices: () => MASTER_PRICES
};

export default productService;
