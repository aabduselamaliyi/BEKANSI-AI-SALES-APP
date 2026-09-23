/**
 * BEKANSI AI SALES PLATFORM - CRM/CUSTOMERS.JS
 * Manages the customers table and CRM contact data
 */

const { pool } = require('../config/database');
const logger = require('../config/logger');

const memoryCustomers = new Map();

const customerCrm = {
    upsertCustomer: async ({
        fullName,
        phone,
        whatsappUserId = null,
        preferredLanguage = 'en',
        customerType = 'INDIVIDUAL',
        city = 'Addis Ababa',
        subCity = null,
        woreda = null,
        deliveryLocation = null,
        source = 'WHATSAPP',
        campaign = null,
        adId = null,
        status = 'ACTIVE'
    }) => {
        const cleanPhone = (phone || '').replace(/[^0-9+]/g, '');
        if (!cleanPhone) throw new Error('Phone number is required for customer record');

        let customer = memoryCustomers.get(cleanPhone);
        const customerId = customer ? customer.id : `CUST-${Date.now().toString().slice(-5)}`;

        const updated = {
            id: customerId,
            full_name: fullName || (customer ? customer.full_name : 'Valued Client'),
            phone: cleanPhone,
            whatsapp_user_id: whatsappUserId || cleanPhone,
            preferred_language: preferredLanguage || (customer ? customer.preferred_language : 'en'),
            customer_type: customerType,
            city: city || 'Addis Ababa',
            sub_city: subCity || (customer ? customer.sub_city : null),
            woreda: woreda || (customer ? customer.woreda : null),
            delivery_location: deliveryLocation || subCity || city,
            source,
            campaign,
            ad_id: adId,
            status,
            created_at: customer ? customer.created_at : new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryCustomers.set(cleanPhone, updated);

        if (pool) {
            try {
                await pool.query(`
                    INSERT INTO customers (
                        full_name, phone, whatsapp_user_id, preferred_language,
                        customer_type, city, sub_city, woreda, delivery_location,
                        source, campaign, ad_id, status, created_at, updated_at
                    )
                    VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, NOW(), NOW())
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
                logger.warn('Could not persist customer to Postgres, kept in memory', { error: dbErr.message });
            }
        }

        return updated;
    },

    getCustomerByPhone: async (phone) => {
        const cleanPhone = (phone || '').replace(/[^0-9+]/g, '');
        if (pool) {
            try {
                const res = await pool.query('SELECT * FROM customers WHERE phone = $1', [cleanPhone]);
                if (res.rows && res.rows.length > 0) return res.rows[0];
            } catch (err) {
                logger.warn('Failed to query customer by phone from Postgres', { error: err.message });
            }
        }
        return memoryCustomers.get(cleanPhone) || null;
    },

    getAllCustomers: () => Array.from(memoryCustomers.values())
};

module.exports = customerCrm;
