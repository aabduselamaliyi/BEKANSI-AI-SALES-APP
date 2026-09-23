/**
 * Logger Configuration with Console Fallback for Bekansi Backend (ESM)
 */
const logger = {
    info: (msg, meta) => console.log(`[INFO] [bekansi-sales-api] ${msg}`, meta ? JSON.stringify(meta) : ''),
    warn: (msg, meta) => console.warn(`[WARN] [bekansi-sales-api] ${msg}`, meta ? JSON.stringify(meta) : ''),
    error: (msg, meta) => console.error(`[ERROR] [bekansi-sales-api] ${msg}`, meta ? JSON.stringify(meta) : ''),
    debug: (msg, meta) => console.debug(`[DEBUG] [bekansi-sales-api] ${msg}`, meta ? JSON.stringify(meta) : '')
};

export default logger;
export { logger };
