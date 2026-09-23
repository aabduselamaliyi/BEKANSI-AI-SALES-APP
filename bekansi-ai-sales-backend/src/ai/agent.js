import { GoogleGenAI } from "@google/genai";
import env from "../config/env.js";
import logger from "../config/logger.js";

import {
  BEKANSI_SYSTEM_PROMPT
} from "./prompts.js";

import {
  BEKANSI_TOOLS,
  executeTool
} from "./tools.js";

import {
  updateConversation
} from "../crm/crm.js";


let ai = null;
if (env.ai.apiKey && !env.ai.apiKey.includes("your_gemini")) {
  try {
    ai = new GoogleGenAI({
      apiKey: env.ai.apiKey
    });
  } catch (e) {
    logger.warn("Failed to initialize GoogleGenAI client with provided key", { error: e.message });
  }
}

const MODEL =
  process.env.GEMINI_MODEL ||
  env.ai.model ||
  "gemini-3.8-flash";


export async function generateBekansiReply({
  conversation,
  customer,
  message
}) {

  const context = {
    customerId: customer?.id,
    conversationId: conversation?.id,
    customerPhone: customer?.phone
  };

  // If GoogleGenAI client is unavailable or Interactions API throws, fallback gracefully
  if (!ai || !ai.interactions) {
    return generateFallbackReply({ conversation, customer, message, context });
  }

  let interaction;

  try {
    // ==========================================================
    // FIRST MESSAGE
    // ==========================================================

    if (!conversation?.gemini_interaction_id) {

      interaction =
        await ai.interactions.create({

          model: MODEL,

          system_instruction:
            BEKANSI_SYSTEM_PROMPT,

          input: `
Customer phone: ${customer?.phone}

Customer name:
${customer?.full_name || "Unknown"}

Customer type:
${customer?.customer_type || "UNKNOWN"}

Latest customer message:
${message}
`,

          tools: BEKANSI_TOOLS,

          generation_config: {
            temperature: 0.4,
            max_output_tokens: 500
          }

        });

    }

    // ==========================================================
    // CONTINUE EXISTING CONVERSATION
    // ==========================================================

    else {

      interaction =
        await ai.interactions.create({

          model: MODEL,

          system_instruction:
            BEKANSI_SYSTEM_PROMPT,

          previous_interaction_id:
            conversation.gemini_interaction_id,

          input: message,

          tools: BEKANSI_TOOLS,

          generation_config: {
            temperature: 0.4,
            max_output_tokens: 500
          }

        });

    }


    // ==========================================================
    // TOOL LOOP
    // ==========================================================

    while (true) {

      const functionCalls =
        (interaction?.steps || [])
          .filter(
            step =>
              step.type === "function_call"
          );


      if (functionCalls.length === 0) {

        const output =
          interaction?.output_text?.trim();

        if (conversation?.id) {
          await updateConversation(
            conversation.id,
            {
              gemini_interaction_id:
                interaction?.id,
              last_ai_message_at:
                new Date().toISOString()
            }
          );
        }

        return {
          text:
            output ||
            "Thank you for contacting BEKANSI Furniture. How can we help you?",
          interactionId:
            interaction?.id
        };
      }


      // Execute each function call.
      // The Interactions API expects function_result
      // steps to be returned to Gemini.

      const results = [];

      for (const call of functionCalls) {

        let result;

        try {

          result =
            await executeTool(
              call.name,
              call.arguments || {},
              context
            );

        } catch (error) {

          result = {
            success: false,
            error: error.message
          };

        }

        results.push({
          type: "function_result",

          name: call.name,

          call_id: call.id,

          result: [
            {
              type: "text",
              text: JSON.stringify(result)
            }
          ]
        });
      }


      interaction =
        await ai.interactions.create({

          model: MODEL,

          system_instruction:
            BEKANSI_SYSTEM_PROMPT,

          previous_interaction_id:
            interaction.id,

          input: results,

          tools: BEKANSI_TOOLS,

          generation_config: {
            temperature: 0.4,
            max_output_tokens: 500
          }

        });

    }
  } catch (err) {
    logger.warn("Interactions API call encountered an error, falling back to standard sales engine", { error: err.message });
    return generateFallbackReply({ conversation, customer, message, context });
  }
}

/**
 * Intelligent sales engine fallback conforming strictly to the 6-point Bekansi format
 */
async function generateFallbackReply({ conversation, customer, message, context }) {
  const customerPhone = customer?.phone || context?.customerPhone || "+251911223344";
  const lower = (message || "").toLowerCase();

  let detectedLang = "en";
  if (/[\u1200-\u137F]/.test(message)) {
    detectedLang = "am";
  } else if (/\b(akkam|nagaa|akkamitti|barbaada|maaloo|galatoomi|fayyisaa)\b/i.test(message)) {
    detectedLang = "om";
  }

  let leadStatus = "🟡 Warm Lead";
  if (lower.includes("order") || lower.includes("buy") || lower.includes("መግዛት") || lower.includes("bituu") || lower.includes("quote") || lower.includes("ዋጋ")) {
    leadStatus = "🔥 Hot Lead";
  } else if (lower.includes("hi") || lower.includes("hello") || lower.includes("ሰላም") || lower.includes("akkam")) {
    leadStatus = "⚪ Cold Lead";
  }

  // Auto trigger product discovery
  if (lower.includes("bed") || lower.includes("አልጋ") || lower.includes("siree")) {
    await executeTool("search_products", { query: "bed" }, context);
    if (lower.includes("price") || lower.includes("ዋጋ") || lower.includes("gatii")) {
      await executeTool("get_product_price", { product_sku: "BK-BED-ENTOTO" }, context);
    }
  } else if (lower.includes("sofa") || lower.includes("ሶፋ") || lower.includes("soofaa")) {
    await executeTool("search_products", { query: "sofa" }, context);
  }

  // Update lead
  await executeTool("create_or_update_lead", {
    customer_type: customer?.customer_type || "B2C",
    product_category: lower.includes("bed") ? "Beds" : (lower.includes("sofa") ? "Sofas" : "Beds"),
    purchase_timeline: leadStatus === "🔥 Hot Lead" ? "Immediate" : "Researching",
    lead_score: leadStatus === "🔥 Hot Lead" ? 85 : 50
  }, context);

  let replyText = "";
  if (detectedLang === "am") {
    replyText = `📌 Response  
እንኳን ወደ በካንሲ ፈርኒቸር እና ኢንቴሪየር ዲዛይን በደህና መጡ! 🇪🇹🛋️ የፈለጉትን የቤትና የቢሮ ፈርኒቸር በጥራትና በቅንጦት እናዘጋጃለን። የቦሌ ሮያል ኪንግ ቬልቬት አልጋችን (85,000 ብር) እና የእንጦጦ ቅንጡ L-ቅርጽ ሳሎን ሶፋችን በምርጥ ቱርክ ቬልቬት የተመረቱ ናቸው።

📌 Recommended Action  
የሚፈልጉትን የቀለም ምርጫ (ሮያል ብሉ ወይም ኤመራልድ ግሪን) እና የመኝታ ቤትዎን ስፋት ያሳውቁን።

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• ስልክ: ${customerPhone}
• ፍላጎት: ${lower.includes("sofa") ? "ሳሎን ሶፋ" : "አልጋ እና የቤት ፈርኒቸር"}

📌 Missing Information  
• ሙሉ ስም
• የመኖሪያ ሰፈር / አድራሻ (ቦሌ፣ ሲኤምሲ፣ ወዘተ)
• የመረጡት ቀለምና መጠን

📌 Suggested Follow-Up  
ትክክለኛውን ልኬት ለመውሰድ ባለሙያዎቻችን ወደ ቤትዎ እንዲመጡ ቀጠሮ እንያዝልዎ?`;
  } else if (detectedLang === "om") {
    replyText = `📌 Response  
Baga gara Mana Meeshaa Manaa fi Dizaayinii Keessaa Bekansi nagaan dhuftan! 🇪🇹🛋️ Siree mootii Bolee Velvet (Qarshii 85,000) fi Soofaa qananii Entoto L-shape qulqullina olaanaadhaan qopheessinee jirra.

📌 Recommended Action  
Halluu fi bal'ina iddoo keessanii filachuudhaan gatii fi dhiyeessa sirrii argadhaa.

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• Lakkoofsa Bilbilaa: ${customerPhone}
• Fedhii: Meeshaa Manaa / Siree fi Soofaa

📌 Missing Information  
• Maqaa Guutuu
• Iddoo Jireenyaa (Finfinnee: Bolee, CMC, kkf)
• Bajata fi Yeroo barbaaddan

📌 Suggested Follow-Up  
Ogeessi dizaayinii keenya iddoo keessan safaruuf yoom haa dhufu?`;
  } else {
    replyText = `📌 Response  
Welcome to Bekansi Furniture & Interior Design! 🇪🇹✨ We specialize in bespoke luxury furniture handcrafted with imported Turkish fabrics, German Blum soft-close fittings, and solid hardwood framing. Our signature Entoto Luxury Smart Bed (85,000 ETB) and Bole Sectional Sofa (145,000 ETB) are available for immediate custom fabrication.

📌 Recommended Action  
Review our custom fabric variants (Royal Blue or Emerald Green velvet with smart ambient LED and USB-C charging).

📌 Lead Status  
${leadStatus}

📌 Information Collected  
• Phone Number: ${customerPhone}
• Product Interest: ${lower.includes("sofa") ? "Luxury Sectional Sofa" : "King Smart Bed / Living Room Furniture"}

📌 Missing Information  
• Full Name
• Addis Ababa Sub-city (Bole, CMC, Sarbet, Kazanchis, etc.)
• Preferred Delivery Timeline

📌 Suggested Follow-Up  
Would you like an official itemized quotation including free delivery and installation within Addis Ababa?`;
  }

  return {
    text: replyText,
    interactionId: `local-${Date.now()}`
  };
}

/**
 * Compatibility processCustomerMessage for Webhook & Simulator callers
 */
export const agent = {
  processCustomerMessage: async ({
    customerPhone,
    customerName = null,
    message,
    customerId = null,
    conversationId = null
  }) => {
    const customer = {
      id: customerId,
      phone: customerPhone,
      full_name: customerName
    };
    const conversation = {
      id: conversationId
    };

    const reply = await generateBekansiReply({
      conversation,
      customer,
      message
    });

    return {
      agentReply: reply.text,
      leadStatus: "🔥 Hot Lead",
      toolsInvoked: ["generateBekansiReply"],
      interactionId: reply.interactionId
    };
  }
};

export default agent;
