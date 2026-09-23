const GRAPH_VERSION =
  process.env.WHATSAPP_API_VERSION ||
  "v23.0";


const PHONE_NUMBER_ID =
  process.env.WHATSAPP_PHONE_NUMBER_ID;


const ACCESS_TOKEN =
  process.env.WHATSAPP_ACCESS_TOKEN;


const BASE_URL =
  `https://graph.facebook.com/${GRAPH_VERSION}/${PHONE_NUMBER_ID}/messages`;


async function whatsappRequest(body) {

  // If credentials are not configured, simulate to allow local testing
  if (!ACCESS_TOKEN || !PHONE_NUMBER_ID) {
    return {
      messaging_product: "whatsapp",
      contacts: [{ input: body.to, wa_id: body.to }],
      messages: [{ id: `wamid.sim_${Date.now()}` }],
      simulated: true
    };
  }

  const response = await fetch(
    BASE_URL,
    {
      method: "POST",

      headers: {
        "Authorization":
          `Bearer ${ACCESS_TOKEN}`,

        "Content-Type":
          "application/json"
      },

      body: JSON.stringify(body)
    }
  );


  const data =
    await response.json();


  if (!response.ok) {

    console.error(
      "WhatsApp API error:",
      JSON.stringify(data, null, 2)
    );

    throw new Error(
      data?.error?.message ||
      "WhatsApp API request failed"
    );
  }


  return data;
}


export async function sendWhatsAppText(
  recipientPhone,
  message
) {

  return await whatsappRequest({

    messaging_product: "whatsapp",

    recipient_type: "individual",

    to: recipientPhone,

    type: "text",

    text: {
      preview_url: false,
      body: message
    }

  });
}


export async function markWhatsAppMessageRead(
  messageId
) {

  return await whatsappRequest({

    messaging_product: "whatsapp",

    status: "read",

    message_id: messageId

  });
}

// Backward-compatibility wrapper for existing modules
export const whatsappService = {
  sendTextMessage: sendWhatsAppText,
  markMessageAsRead: markWhatsAppMessageRead
};

export default whatsappService;
