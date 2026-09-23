/**
 * BEKANSI AI SALES PLATFORM - PRODUCTS/PRODUCTS.JS
 * Manages the products table and verified furniture catalog
 */

const { pool } = require('../config/database');
const logger = require('../config/logger');

// Verified In-Memory Product Master Data matching the products table
const MASTER_PRODUCTS = [
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

// Product Variants
const MASTER_VARIANTS = [
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

const productService = {
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
    }
};

module.exports = productService;
