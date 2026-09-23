/**
 * Winston Logger Configuration with Console Fallback for Bekansi Backend
 */
let logger;

try {
    const winston = require('winston');
    logger = winston.createLogger({
        level: process.env.LOG_LEVEL || 'info',
        format: winston.format.combine(
            winston.format.timestamp(),
            winston.format.errors({ stack: true }),
            winston.format.json()
        ),
        defaultMeta: { service: 'bekansi-sales-api' },
        transports: [
            new winston.transports.Console({
                format: winston.format.combine(
                    winston.format.colorize(),
                    winston.format.simple()
                )
            })
        ]
    });
} catch (e) {
    logger = {
        info: (msg, meta) => console.log(`[INFO] [bekansi-sales-api] ${msg}`, meta || ''),
        warn: (msg, meta) => console.warn(`[WARN] [bekansi-sales-api] ${msg}`, meta || ''),
        error: (msg, meta) => console.error(`[ERROR] [bekansi-sales-api] ${msg}`, meta || ''),
        debug: (msg, meta) => console.debug(`[DEBUG] [bekansi-sales-api] ${msg}`, meta || '')
    };
}

module.exports = logger;
