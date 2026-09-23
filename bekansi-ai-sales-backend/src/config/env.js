import "dotenv/config";

const required = [
  "SUPABASE_URL",
  "SUPABASE_SERVICE_ROLE_KEY",
  "GEMINI_API_KEY",
  "WHATSAPP_ACCESS_TOKEN",
  "WHATSAPP_PHONE_NUMBER_ID",
  "WHATSAPP_VERIFY_TOKEN"
];

for (const key of required) {
  if (!process.env[key]) {
    console.warn(`WARNING: Missing environment variable: ${key}`);
  }
}

export const env = {
  port: parseInt(process.env.PORT || "3000", 10),
  nodeEnv: process.env.NODE_ENV || "development",
  logLevel: process.env.LOG_LEVEL || "info",
  corsOrigin: process.env.CORS_ORIGIN || "*",

  brand: {
    name: process.env.BEKANSI_NAME || "BEKANSI FURNITURE & INTERIOR DESIGN",
    whatsapp: process.env.BEKANSI_WHATSAPP || "+251988828861",
    location: process.env.BEKANSI_LOCATION || "Dukem, in front of Daroni Hotel, beside Oromia Bank, next to Dibora Restaurant",
    freeDelivery: process.env.BEKANSI_FREE_DELIVERY || process.env["BEKANSI_FREE DELIVERY"] || "Across Ethiopia"
  },

  whatsapp: {
    verifyToken: process.env.WHATSAPP_VERIFY_TOKEN || "",
    accessToken: process.env.WHATSAPP_ACCESS_TOKEN || "",
    phoneNumberId: process.env.WHATSAPP_PHONE_NUMBER_ID || "",
    businessAccountId: process.env.WHATSAPP_BUSINESS_ACCOUNT_ID || "",
    appSecret: process.env.WHATSAPP_APP_SECRET || "",
    apiVersion: process.env.WHATSAPP_API_VERSION || "v23.0",
    baseUrl: "https://graph.facebook.com"
  },

  ai: {
    apiKey: process.env.GEMINI_API_KEY || process.env.API_KEY || "",
    model: process.env.GEMINI_MODEL || "gemini-3.8-flash"
  },

  supabase: {
    url: process.env.SUPABASE_URL || "",
    serviceRoleKey: process.env.SUPABASE_SERVICE_ROLE_KEY || ""
  }
};

export default env;
