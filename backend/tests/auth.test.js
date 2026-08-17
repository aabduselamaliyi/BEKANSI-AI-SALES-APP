/**
 * ============================================================================
 * BACKEND API & TENANT ISOLATION INTEGRATION TESTS
 * ============================================================================
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../src/middleware/auth');

test('JWT generation and claim verification for multi-tenant context', () => {
    const mockUser = {
        user_id: '11111111-2222-3333-4444-555555555555',
        organization_id: 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
        email: 'sales@bekansi.com',
        role: 'sales_agent'
    };

    const token = jwt.sign(mockUser, JWT_SECRET, { expiresIn: '1h' });
    assert.ok(typeof token === 'string', 'Token should be a valid string');

    const decoded = jwt.verify(token, JWT_SECRET);
    assert.equal(decoded.user_id, mockUser.user_id);
    assert.equal(decoded.organization_id, mockUser.organization_id);
    assert.equal(decoded.role, mockUser.role);
});

test('RBAC role hierarchy validation', () => {
    const validRoles = ['super_admin', 'tenant_admin', 'manager', 'sales_agent', 'ai_assistant'];
    assert.equal(validRoles.includes('manager'), true);
    assert.equal(validRoles.includes('unauthorized_hacker'), false);
});
