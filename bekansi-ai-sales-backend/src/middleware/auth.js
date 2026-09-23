/**
 * ============================================================================
 * BEKANSI MULTI-TENANT AUTHENTICATION & RBAC MIDDLEWARE
 * ============================================================================
 */
const jwt = require('jsonwebtoken');
const logger = require('../config/logger');
const { withTenantTransaction } = require('../config/dbPool');

const JWT_SECRET = process.env.JWT_SECRET || 'bekansi_super_secret_enterprise_signing_key_production_2026';

const tenantAuthenticator = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                error: {
                    code: 'UNAUTHENTICATED',
                    message: 'Authorization header is missing or malformed. Expected Bearer Token.'
                }
            });
        }

        const token = authHeader.split(' ')[1];
        jwt.verify(token, JWT_SECRET, (err, decoded) => {
            if (err) {
                const isExpired = err.name === 'TokenExpiredError';
                return res.status(401).json({
                    success: false,
                    error: {
                        code: isExpired ? 'TOKEN_EXPIRED' : 'INVALID_TOKEN',
                        message: isExpired ? 'Your session has expired. Please log in again.' : 'Invalid token signature or payload.'
                    }
                });
            }

            if (!decoded.organization_id || !decoded.user_id || !decoded.role) {
                return res.status(403).json({
                    success: false,
                    error: {
                        code: 'FORBIDDEN_CLAIMS',
                        message: 'Token does not contain required claims (organization_id, user_id, role).'
                    }
                });
            }

            req.user = {
                id: decoded.user_id,
                email: decoded.email,
                role: decoded.role,
                organization_id: decoded.organization_id
            };
            req.tenantId = decoded.organization_id;

            // Bind scoped database execution helper to request
            req.dbExecute = (callback) => withTenantTransaction(req.tenantId, callback);

            return next();
        });
    } catch (error) {
        logger.error('Unhandled auth error', { error: error.message });
        return res.status(500).json({
            success: false,
            error: {
                code: 'INTERNAL_SERVER_ERROR',
                message: 'An unexpected error occurred during authentication.'
            }
        });
    }
};

/**
 * Role-Based Access Control (RBAC) Guard Middleware
 */
const requireRoles = (...allowedRoles) => {
    return (req, res, next) => {
        if (!req.user || !allowedRoles.includes(req.user.role)) {
            return res.status(403).json({
                success: false,
                error: {
                    code: 'PERMISSION_DENIED',
                    message: `User role '${req.user?.role || 'anonymous'}' is not authorized to access this resource.`
                }
            });
        }
        next();
    };
};

module.exports = {
    tenantAuthenticator,
    requireRoles,
    JWT_SECRET
};
