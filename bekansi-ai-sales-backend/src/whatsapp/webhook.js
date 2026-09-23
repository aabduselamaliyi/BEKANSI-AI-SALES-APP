import express from "express";

import {
  getOrCreateCustomer,
  getOrCreateConversation,
  saveInboundMessage,
  saveOutboundMessage,
  updateConversation
} from "../crm/crm.js";

import {
  generateBekansiReply
} from "../ai/agent.js";

import {
  sendWhatsAppText,
  markWhatsAppMessageRead
} from "./whatsapp.service.js";


const router =
  express.Router();


// ============================================================
// META WEBHOOK VERIFICATION
// ============================================================

router.get(
  "/",

  (req, res) => {

    const mode =
      req.query["hub.mode"];

    const token =
      req.query["hub.verify_token"];

    const challenge =
      req.query["hub.challenge"];


    if (
      mode === "subscribe" &&
      token === (process.env.WHATSAPP_VERIFY_TOKEN || "bekansi_sales_verify_token_2025")
    ) {

      console.log(
        "WhatsApp webhook verified."
      );

      return res
        .status(200)
        .send(challenge);
    }


    return res
      .sendStatus(403);
  }
);


// ============================================================
// WHATSAPP EVENTS
// ============================================================

router.post(
  "/",

  async (req, res) => {

    // Acknowledge Meta quickly.
    res.sendStatus(200);


    try {

      const body =
        req.body;


      if (
        body.object !==
        "whatsapp_business_account"
      ) {
        return;
      }


      const entries =
        body.entry || [];


      for (const entry of entries) {

        const changes =
          entry.changes || [];


        for (const change of changes) {

          const value =
            change.value;


          const messages =
            value?.messages || [];


          for (
            const message
            of messages
          ) {

            await processIncomingMessage(
              message,
              value
            );

          }
        }
      }

    } catch (error) {

      console.error(
        "Webhook processing error:",
        error
      );

    }
  }
);


// ============================================================
// PROCESS INCOMING MESSAGE
// ============================================================

async function processIncomingMessage(
  message,
  webhookValue
) {

  if (
    !message?.from
  ) {
    return;
  }


  const phone =
    message.from;


  const profileName =
    webhookValue?.contacts?.[0]?.profile?.name ||
    null;


  const messageType =
    message.type;


  // ----------------------------------------------------------
  // PHASE 1: TEXT ONLY
  // ----------------------------------------------------------

  if (
    messageType !== "text"
  ) {

    await sendWhatsAppText(
      phone,
      "Thanks for contacting BEKANSI Furniture 😊 Please send your message as text for now. Image and voice-message automation will be added in the next phase."
    );

    return;
  }


  const text =
    message.text?.body?.trim();


  if (!text) {
    return;
  }


  // ----------------------------------------------------------
  // CUSTOMER
  // ----------------------------------------------------------

  const customer =
    await getOrCreateCustomer(
      phone,
      profileName
    );


  // ----------------------------------------------------------
  // CONVERSATION
  // ----------------------------------------------------------

  const conversation =
    await getOrCreateConversation(
      customer
    );


  // ----------------------------------------------------------
  // SAVE MESSAGE
  // ----------------------------------------------------------

  const savedMessage =
    await saveInboundMessage({

      conversationId:
        conversation.id,

      whatsappMessageId:
        message.id,

      messageType,

      text
    });


  // Duplicate webhook.
  if (!savedMessage) {
    return;
  }


  await updateConversation(
    conversation.id,
    {
      last_message_at:
        new Date().toISOString(),

      last_customer_message_at:
        new Date().toISOString()
    }
  );


  // ----------------------------------------------------------
  // MARK AS READ
  // ----------------------------------------------------------

  try {

    await markWhatsAppMessageRead(
      message.id
    );

  } catch (error) {

    console.error(
      "Could not mark WhatsApp message read:",
      error.message
    );

  }


  // ----------------------------------------------------------
  // HUMAN TAKEOVER
  // ----------------------------------------------------------

  if (
    conversation.ai_enabled === false
  ) {

    console.log(
      "AI disabled; human agent owns conversation:",
      conversation.id
    );

    return;
  }


  // ----------------------------------------------------------
  // GEMINI
  // ----------------------------------------------------------

  const aiResponse =
    await generateBekansiReply({

      conversation,

      customer,

      message: text

    });


  // ----------------------------------------------------------
  // SEND AI RESPONSE
  // ----------------------------------------------------------

  await sendWhatsAppText(
    phone,
    aiResponse.text
  );


  // ----------------------------------------------------------
  // SAVE AI MESSAGE
  // ----------------------------------------------------------

  await saveOutboundMessage({

    conversationId:
      conversation.id,

    text:
      aiResponse.text,

    aiGenerated:
      true,

    aiModel:
      process.env.GEMINI_MODEL

  });


  console.log(
    "BEKANSI AI response sent:",
    aiResponse.text
  );
}

// ============================================================
// SIMULATION ENDPOINT FOR TESTING
// ============================================================
router.post("/simulate", async (req, res) => {
  const { customerPhone = "+251911223344", customerName = "Sara Tesfaye", message = "Hi, I need a luxury king bed" } = req.body;

  try {
    const customer = await getOrCreateCustomer(customerPhone, customerName);
    const conversation = await getOrCreateConversation(customer);

    await saveInboundMessage({
      conversationId: conversation.id,
      whatsappMessageId: `sim_${Date.now()}`,
      messageType: "text",
      text: message
    });

    const aiResponse = await generateBekansiReply({
      conversation,
      customer,
      message
    });

    await saveOutboundMessage({
      conversationId: conversation.id,
      text: aiResponse.text,
      aiGenerated: true,
      aiModel: process.env.GEMINI_MODEL
    });

    return res.status(200).json({
      success: true,
      reply: aiResponse.text,
      conversationId: conversation.id
    });
  } catch (err) {
    console.error("Simulation error:", err);
    return res.status(500).json({ success: false, error: err.message });
  }
});

export default router;
