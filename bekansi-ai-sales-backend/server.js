import express from "express";

import "./src/config/env.js";

import whatsappWebhook
  from "./src/whatsapp/webhook.js";


const app =
  express();


app.use(
  express.json({
    limit: "5mb"
  })
);


// ============================================================
// HEALTH CHECK
// ============================================================

app.get(
  "/",

  (req, res) => {

    res.json({
      success: true,
      service:
        "BEKANSI AI Sales Backend",
      status:
        "running",
      version:
        "phase-1"
    });

  }
);


app.get(
  "/health",

  (req, res) => {

    res.json({
      status: "ok",
      service:
        "bekansi-ai-sales"
    });

  }
);


// ============================================================
// WHATSAPP WEBHOOK
// ============================================================

app.use(
  "/api/webhooks/whatsapp",
  whatsappWebhook
);

// Also mount on /webhook for standard Meta and simulator compatibility
app.use(
  "/webhook",
  whatsappWebhook
);


// ============================================================
// ERROR HANDLER
// ============================================================

app.use(
  (
    error,
    req,
    res,
    next
  ) => {

    console.error(
      "Unhandled server error:",
      error
    );


    if (res.headersSent) {
      return next(error);
    }


    res.status(500).json({
      success: false,
      error:
        "Internal server error"
    });

  }
);


// ============================================================
// START
// ============================================================

const PORT =
  process.env.PORT || 3000;


app.listen(
  PORT,

  () => {

    console.log(
      `BEKANSI AI Sales backend running on port ${PORT}`
    );

    console.log(
      `Webhook:
http://localhost:${PORT}/api/webhooks/whatsapp`
    );

  }
);

export default app;
