/**
 * ============================================================================
 * DATABASE MIGRATION SCRIPT - POSTGRESQL MULTI-TENANT INITIALIZATION
 * ============================================================================
 */
require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { pool } = require('../config/dbPool');
const logger = require('../config/logger');

const runMigrations = async () => {
    logger.info('Starting PostgreSQL database migration runner...');

    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        // Execute primary schema.sql
        const schemaPath = path.resolve(__dirname, '../../../schema.sql');
        if (fs.existsSync(schemaPath)) {
            const schemaSql = fs.readFileSync(schemaPath, 'utf8');
            logger.info('Executing main schema.sql...');
            await client.query(schemaSql);
            logger.info('Main schema.sql executed successfully.');
        }

        // Execute incremental migrations in /migrations directory
        const migrationsDir = path.resolve(__dirname, '../../../migrations');
        if (fs.existsSync(migrationsDir)) {
            const migrationFiles = fs.readdirSync(migrationsDir).sort();
            for (const file of migrationFiles) {
                if (file.endsWith('.sql')) {
                    logger.info(`Executing migration: ${file}...`);
                    const migrationSql = fs.readFileSync(path.join(migrationsDir, file), 'utf8');
                    await client.query(migrationSql);
                    logger.info(`Migration ${file} executed successfully.`);
                }
            }
        }

        await client.query('COMMIT');
        logger.info('All PostgreSQL migrations completed successfully.');
    } catch (error) {
        await client.query('ROLLBACK');
        logger.error('Database migration failed and rolled back', { error: error.message, stack: error.stack });
        process.exit(1);
    } finally {
        client.release();
        await pool.end();
    }
};

if (require.main === module) {
    runMigrations();
}

module.exports = runMigrations;
