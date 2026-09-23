/**
 * BEKANSI AI SALES PLATFORM - PRODUCTS/PRICING.JS
 * Strict pricing repository and structured quotation generation
 */

const { pool } = require('../config/database');
const logger = require('../config/logger');

// Verified Base and Variant Pricing Matrix
const MASTER_PRICES = [
    { sku: 'BK-BED-ENTOTO', base_price: 85000, variant_prices: { 'Royal Blue': 88000, 'Emerald Green': 88000, 'Warm Beige': 78000 } },
    { sku: 'BK-SOF-BOLE', base_price: 145000, variant_prices: { 'Charcoal Grey': 145000, 'Navy Blue': 152000 } },
    { sku: 'BK-KIT-MESKEL', base_price: 38000, unit: 'ETB per linear meter' },
    { sku: 'BK-WRD-SEMEN', base_price: 92000 },
    { sku: 'BK-DIN-ENTOTO', base_price: 110000 },
    { sku: 'BK-OFF-EXECUTIVE', base_price: 125000 },
    { sku: 'BK-TVS-FINFINE', base_price: 48000 }
];

const ADDIS_ABABA_DELIVERY_RATES = {
    'Bole': 1500,
    'CMC': 2000,
    'Kazanchis': 1500,
    'Sarbet': 1800,
    'Old Airport': 1800,
    'Piazza': 2000,
    'Default_Addis': 2000,
    'Outside_Addis': 5000
};

const pricingService = {
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
        const priceInfo = await pricingService.getPriceForProduct(sku, color);
        if (!priceInfo) {
            return {
                success: false,
                message: `Pricing for ${productName || sku} requires custom measurement or verification with showroom staff.`
            };
        }

        const unitPrice = priceInfo.price;
        const totalItemsPrice = unitPrice * quantity;
        const deliveryCost = pricingService.calculateDelivery(deliveryLocation);
        const installationCost = includeInstallation ? (totalItemsPrice > 100000 ? 0 : 2500) : 0; // Free installation for high orders
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

module.exports = pricingService;
