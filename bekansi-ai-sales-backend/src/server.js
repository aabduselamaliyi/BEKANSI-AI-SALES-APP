/**
 * ============================================================================
 * BEKANSI AI SALES PLATFORM - HTTP SERVER ENTRYPOINT
 * ============================================================================
 */
require('dotenv').config();
const http = require('http');
const app = require('./app');
const logger = require('./config/logger');
const { pool } = require('./config/dbPool');

const PORT = process.env.PORT || 8080;

const server = http.createServer(app);

server.listen(PORT, () => {
    logger.info(`Bekansi AI Sales Platform backend listening on port ${PORT}`, {
        environment: process.env.NODE_ENV || 'development',
        port: PORT
    });
});

// Graceful Shutdown
const shutdown = () => {
    logger.info('Shutting down Bekansi API server gracefully...');
    server.close(async () => {
        logger.info('HTTP server closed.');
        try {
            await pool.end();
            logger.info('PostgreSQL pool disconnected.');
            process.exit(0);
        } catch (e) {
            logger.error('Error closing PostgreSQL pool', { error: e.message });
            process.exit(1);
        }
    });
};

process.on('SIGTERM', shutdown);
process.on('SIGINT', shutdown);
