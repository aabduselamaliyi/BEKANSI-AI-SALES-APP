import { supabase } from "../config/database.js";

export async function getOrCreateCustomer(phone, profileName = null) {
  const { data: existing, error: findError } = await supabase
    .from("customers")
    .select("*")
    .eq("phone", phone)
    .maybeSingle();

  if (findError) throw findError;

  if (existing) {
    return existing;
  }

  const { data, error } = await supabase
    .from("customers")
    .insert({
      phone,
      whatsapp_user_id: phone,
      full_name: profileName
    })
    .select()
    .single();

  if (error) throw error;

  return data;
}


export async function getOrCreateConversation(customer) {
  const { data: existing, error: findError } = await supabase
    .from("conversations")
    .select("*")
    .eq("customer_id", customer.id)
    .eq("channel", "whatsapp")
    .eq("status", "open")
    .maybeSingle();

  if (findError) throw findError;

  if (existing) {
    return existing;
  }

  const { data, error } = await supabase
    .from("conversations")
    .insert({
      customer_id: customer.id,
      channel: "whatsapp",
      whatsapp_phone: customer.phone,
      language: customer.preferred_language || "en"
    })
    .select()
    .single();

  if (error) throw error;

  return data;
}


export async function saveInboundMessage({
  conversationId,
  whatsappMessageId,
  messageType,
  text
}) {
  const { data, error } = await supabase
    .from("messages")
    .insert({
      conversation_id: conversationId,
      whatsapp_message_id: whatsappMessageId,
      direction: "inbound",
      sender_type: "customer",
      message_type: messageType,
      text
    })
    .select()
    .single();

  if (error) {
    // Duplicate webhook messages should not crash the webhook.
    if (error.code === "23505") {
      return null;
    }

    throw error;
  }

  return data;
}


export async function saveOutboundMessage({
  conversationId,
  text,
  aiGenerated = true,
  aiModel = null
}) {
  const { data, error } = await supabase
    .from("messages")
    .insert({
      conversation_id: conversationId,
      direction: "outbound",
      sender_type: aiGenerated ? "ai" : "human",
      message_type: "text",
      text,
      ai_generated: aiGenerated,
      ai_model: aiModel
    })
    .select()
    .single();

  if (error) throw error;

  return data;
}


export async function updateConversation(conversationId, updates) {
  const { data, error } = await supabase
    .from("conversations")
    .update({
      ...updates,
      updated_at: new Date().toISOString()
    })
    .eq("id", conversationId)
    .select()
    .single();

  if (error) throw error;

  return data;
}


export async function createOrUpdateLead(context, leadData) {
  const {
    customerId,
    conversationId
  } = context;

  const { data: existing } = await supabase
    .from("leads")
    .select("*")
    .eq("customer_id", customerId)
    .eq("conversation_id", conversationId)
    .maybeSingle();

  if (existing) {
    const { data, error } = await supabase
      .from("leads")
      .update({
        ...leadData,
        updated_at: new Date().toISOString()
      })
      .eq("id", existing.id)
      .select()
      .single();

    if (error) throw error;

    return data;
  }

  const { data, error } = await supabase
    .from("leads")
    .insert({
      customer_id: customerId,
      conversation_id: conversationId,
      ...leadData
    })
    .select()
    .single();

  if (error) throw error;

  return data;
}

export const crm = {
  getOrCreateCustomer,
  getOrCreateConversation,
  saveInboundMessage,
  saveOutboundMessage,
  updateConversation,
  createOrUpdateLead,
  upsertCustomer: (args) => getOrCreateCustomer(args.phone, args.fullName),
  classifyAndSaveLead: (args) => createOrUpdateLead({ customerId: args.customerId, conversationId: args.conversationId }, args),
  getAllCustomers: () => [],
  getAllConversations: () => [],
  getAllMessages: () => [],
  getAllLeads: () => []
};

export default crm;

