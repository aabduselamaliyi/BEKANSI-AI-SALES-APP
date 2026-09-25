import {
  searchProducts,
  getProduct,
  getProductPrice
} from "../products/products.js";

import {
  createOrUpdateLead,
  updateConversation
} from "../crm/crm.js";

import { env } from "../config/env.js";


// ============================================================
// TOOL DECLARATIONS
// ============================================================

export const BEKANSI_TOOLS = [

  {
    type: "function",

    name: "search_products",

    description:
      "Search the Bekansi product catalog for products matching the customer's request.",

    parameters: {
      type: "object",

      properties: {
        query: {
          type: "string",
          description:
            "Product search query, such as bed, sofa, wardrobe, dining table."
        }
      },

      required: ["query"]
    }
  },


  {
    type: "function",

    name: "get_product",

    description:
      "Retrieve detailed information about a Bekansi product using its SKU.",

    parameters: {
      type: "object",

      properties: {
        product_sku: {
          type: "string",
          description:
            "Bekansi product SKU such as BED-150 or BED-180."
        }
      },

      required: ["product_sku"]
    }
  },


  {
    type: "function",

    name: "get_product_price",

    description:
      "Retrieve the current official Bekansi price from the database. Never guess a price.",

    parameters: {
      type: "object",

      properties: {
        product_sku: {
          type: "string",
          description:
            "Bekansi product SKU."
        },

        size: {
          type: "string",
          description:
            "Requested product size if known."
        }
      },

      required: ["product_sku"]
    }
  },


  {
    type: "function",

    name: "get_delivery_policy",

    description:
      "Retrieve Bekansi's current official delivery policy across Ethiopia (Addis Ababa, Dukem, Oromia, Jigjiga, Hawassa, Bahir Dar, Dire Dawa).",

    parameters: {
      type: "object",
      properties: {
        destination_city: {
          type: "string",
          description: "Optional destination city/town in Ethiopia (e.g., Addis Ababa, Jigjiga, Hawassa)."
        }
      }
    }
  },


  {
    type: "function",

    name: "create_or_update_lead",

    description:
      "Create or update the customer's CRM lead information.",

    parameters: {
      type: "object",

      properties: {

        customer_type: {
          type: "string",
          enum: [
            "B2C",
            "B2B",
            "UNKNOWN"
          ]
        },

        product_category: {
          type: "string"
        },

        quantity: {
          type: "integer"
        },

        size: {
          type: "string"
        },

        color: {
          type: "string"
        },

        fabric: {
          type: "string"
        },

        finish: {
          type: "string"
        },

        led_required: {
          type: "boolean"
        },

        socket_required: {
          type: "boolean"
        },

        delivery_location: {
          type: "string"
        },

        purchase_timeline: {
          type: "string"
        },

        lead_stage: {
          type: "string"
        },

        lead_score: {
          type: "integer"
        }
      }
    }
  },


  {
    type: "function",

    name: "handoff_to_human",

    description:
      "Transfer the WhatsApp conversation from AI to a human Bekansi salesperson.",

    parameters: {
      type: "object",

      properties: {
        reason: {
          type: "string",
          description:
            "Reason the human salesperson should take over."
        },

        summary: {
          type: "string",
          description:
            "Concise summary for the salesperson."
        }
      },

      required: [
        "reason",
        "summary"
      ]
    }
  }

];


// ============================================================
// TOOL EXECUTION
// ============================================================

export async function executeTool(
  toolName,
  args,
  context = {}
) {

  switch (toolName) {

    case "search_products": {

      return await searchProducts(
        args.query
      );

    }


    case "get_product": {

      return await getProduct(
        args.product_sku
      );

    }


    case "get_product_price": {

      return await getProductPrice({
        product_sku: args.product_sku,
        size: args.size || null
      });

    }


    case "get_delivery_policy": {

      const city = args.destination_city?.trim();
      const isRegional = city && !city.toLowerCase().includes("addis") && !city.toLowerCase().includes("dukem");

      return {
        delivery_available: true,
        coverage: env.brand.freeDelivery || "Across Ethiopia",
        destination_requested: city || "All Regional & Addis Destinations",
        delivery_notes: isRegional 
          ? `Delivery to ${city} is supported via dedicated freight carriers dispatched from Dukem Central Workshop. Transit timeframe is typically 3-5 days.`
          : "Delivery within Addis Ababa and Dukem showroom perimeter is scheduled directly upon production completion.",
        business_location:
          env.brand.location || "Dukem, in front of Daroni Hotel, beside Oromia Bank, next to Dibora Restaurant",
        whatsapp:
          env.brand.whatsapp || "+251988828861"
      };

    }


    case "create_or_update_lead": {

      const {
        customer_type,
        product_category,
        quantity,
        size,
        color,
        fabric,
        finish,
        led_required,
        socket_required,
        purchase_timeline,
        lead_stage,
        lead_score
      } = args;

      const lead = await createOrUpdateLead(
        context,
        {
          product_category,
          quantity,
          size,
          color,
          fabric,
          finish,
          led_required,
          socket_required,
          purchase_timeline,
          lead_stage,
          lead_score,
          lead_temperature:
            Number(lead_score || 0) >= 80
              ? "HOT"
              : Number(lead_score || 0) >= 60
                ? "WARM"
                : Number(lead_score || 0) >= 40
                  ? "DEVELOPING"
                  : "COLD"
        }
      );

      return {
        success: true,
        lead
      };

    }


    case "handoff_to_human": {

      const updated =
        await updateConversation(
          context.conversationId,
          {
            ai_enabled: false,
            human_assigned: true,
            status: "human"
          }
        );

      return {
        success: true,
        handed_off: true,
        reason: args.reason,
        summary: args.summary,
        conversation_id: updated?.id || context.conversationId
      };

    }


    default:

      throw new Error(
        `Unknown tool: ${toolName}`
      );
  }
}

// Compatibility exports
export const AI_TOOLS_SCHEMA = BEKANSI_TOOLS;
export const tools = {
  executeTool,
  getToolLogs: () => [],
  AI_TOOLS_SCHEMA: BEKANSI_TOOLS,
  BEKANSI_TOOLS
};

export default tools;
