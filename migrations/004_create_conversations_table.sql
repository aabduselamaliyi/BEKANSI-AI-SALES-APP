-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 004
-- EXACT CONVERSATIONS TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS conversations (
  id uuid primary key default gen_random_uuid(),

  customer_id uuid references customers(id),

  channel text default 'whatsapp',
  whatsapp_phone text,

  status text default 'open',

  language text default 'en',

  ai_enabled boolean default true,
  human_assigned boolean default false,

  assigned_agent uuid,

  lead_stage text default 'NEW',

  last_message_at timestamptz,
  last_customer_message_at timestamptz,
  last_ai_message_at timestamptz,

  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Performance Indexes for Fast WhatsApp Lookups and Agent Assignment
CREATE INDEX IF NOT EXISTS idx_conversations_customer_id ON conversations(customer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_whatsapp_phone ON conversations(whatsapp_phone);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);
CREATE INDEX IF NOT EXISTS idx_conversations_lead_stage ON conversations(lead_stage);
CREATE INDEX IF NOT EXISTS idx_conversations_human_assigned ON conversations(human_assigned);
