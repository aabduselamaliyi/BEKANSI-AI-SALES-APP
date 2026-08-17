/**
 * ============================================================================
 * AUTHENTICATION ROUTES & CONTROLLERS (JWT + Role Context)
 * ============================================================================
 */
const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { pool } = require('../config/dbPool');
const { JWT_SECRET, tenantAuthenticator } = require('../middleware/auth');
const logger = require('../config/logger');

const router = express.Router();

/**
 * POST /api/v1/auth/login
 */
router.post('/login', async (req, res) => {
    try {
        const { email, password, organization_id } = req.body;

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                error: {
                    code: 'VALIDATION_ERROR',
                    message: 'Email and password are required.'
                }
            });
        }

        let userQuery = 'SELECT u.*, o.name as org_name, o.is_active as org_active FROM users u JOIN organizations o ON u.organization_id = o.id WHERE u.email = $1';
        let queryParams = [email.toLowerCase().trim()];

        if (organization_id) {
            userQuery += ' AND u.organization_id = $2';
            queryParams.push(organization_id);
        }

        const result = await pool.query(userQuery, queryParams);

        if (result.rows.length === 0) {
            return res.status(401).json({
                success: false,
                error: {
                    code: 'INVALID_CREDENTIALS',
                    message: 'Invalid email or password.'
                }
            });
        }

        const user = result.rows[0];

        if (!user.is_active || !user.org_active) {
            return res.status(403).json({
                success: false,
                error: {
                    code: 'ACCOUNT_INACTIVE',
                    message: 'This user account or organization is deactivated.'
                }
            });
        }

        // Verify password hash
        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch) {
            return res.status(401).json({
                success: false,
                error: {
                    code: 'INVALID_CREDENTIALS',
                    message: 'Invalid email or password.'
                }
            });
        }

        // Create JWT payload
        const payload = {
            user_id: user.id,
            email: user.email,
            role: user.role,
            organization_id: user.organization_id,
            first_name: user.first_name,
            last_name: user.last_name
        };

        const token = jwt.sign(payload, JWT_SECRET, { expiresIn: '7d' });

        logger.info('User authenticated successfully', { userId: user.id, email: user.email, role: user.role });

        return res.status(200).json({
            success: true,
            data: {
                token,
                user: {
                    id: user.id,
                    email: user.email,
                    first_name: user.first_name,
                    last_name: user.last_name,
                    role: user.role,
                    organization_id: user.organization_id,
                    organization_name: user.org_name
                }
            }
        });

    } catch (error) {
        logger.error('Login processing error', { error: error.message });
        return res.status(500).json({
            success: false,
            error: {
                code: 'INTERNAL_SERVER_ERROR',
                message: 'Failed to process login.'
            }
        });
    }
});

/**
 * GET /api/v1/auth/me
 */
router.get('/me', tenantAuthenticator, (req, res) => {
    return res.status(200).json({
        success: true,
        data: {
            user: req.user,
            tenant_id: req.tenantId
        }
    });
});

module.exports = router;
