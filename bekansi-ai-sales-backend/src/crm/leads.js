/**
 * BEKANSI AI SALES PLATFORM - CRM/LEADS.JS
 * Manages the leads table, lead scoring, and pipeline stages
 */

const { pool } = require('../config/database');
const logger = require('../config/logger');

const memoryLeads = new Map();

const leadCrm = {
    classifyAndSaveLead: async ({
        customerId,
        conversationId,
        customerPhone,
        customerName,
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
        purchaseTimeline = null,
        leadStage = 'NEW',
        leadScore = null,
        leadTemperature = null,
        leadStatus = '🟡 Warm Lead',
        assignedTo = null,
        nextFollowupAt = null,
        missingInformation = []
    }) => {
        const cleanPhone = (customerPhone || '').replace(/[^0-9+]/g, '');
        const leadId = `LEAD-${Date.now().toString().slice(-6)}`;

        // Normalize temperature: 'HOT' | 'WARM' | 'COLD'
        let temperature = 'WARM';
        if (leadTemperature) {
            temperature = leadTemperature.toUpperCase();
        } else if (typeof leadStatus === 'string') {
            if (leadStatus.includes('Hot') || leadStatus.includes('🔥')) temperature = 'HOT';
            else if (leadStatus.includes('Cold') || leadStatus.includes('⚪')) temperature = 'COLD';
            else temperature = 'WARM';
        }

        // Determine dynamic lead score
        let score = leadScore;
        if (score === null || score === undefined) {
            if (temperature === 'HOT') score = 85;
            else if (temperature === 'WARM') score = 55;
            else score = 25;
        }

        const category = productCategory || productInterest || 'Beds';
        const timeline = purchaseTimeline || 'Within 2-4 weeks';

        const leadRecord = {
            id: leadId,
            customer_id: customerId,
            conversation_id: conversationId,
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
            lead_temperature: temperature,
            assigned_to: assignedTo,
            next_followup_at: nextFollowupAt || new Date(Date.now() + 24 * 3600 * 1000).toISOString(),
            last_followup_at: null,
            lost_reason: null,
            customerPhone: cleanPhone,
            customerName: customerName || 'Prospective Client',
            leadStatus: temperature === 'HOT' ? '🔥 Hot Lead' : (temperature === 'WARM' ? '🟡 Warm Lead' : '⚪ Cold Lead'),
            missingInformation,
            created_at: new Date().toISOString(),
            updated_at: new Date().toISOString()
        };

        memoryLeads.set(cleanPhone, leadRecord);

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
                logger.warn('Could not persist lead to Postgres, kept in memory', { error: dbErr.message });
            }
        }

        return leadRecord;
    },

    getAllLeads: () => Array.from(memoryLeads.values())
};

module.exports = leadCrm;
