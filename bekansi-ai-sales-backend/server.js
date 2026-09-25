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
// PROCESS GUARDS & START
// ============================================================

process.on("unhandledRejection", (reason, promise) => {
  console.error("Unhandled Rejection at:", promise, "reason:", reason);
});

process.on("uncaughtException", (err) => {
  console.error("Uncaught Exception thrown:", err);
});

const PORT = 3000;

const server = app.listen(
  PORT,
  "0.0.0.0",
  () => {
    console.log(
      `BEKANSI AI Sales backend running on port ${PORT}`
    );
    console.log(
      `Webhook: http://localhost:${PORT}/api/webhooks/whatsapp`
    );
  }
);

server.on("error", (err) => {
  console.error("Server listen error:", err);
});

export default app;
