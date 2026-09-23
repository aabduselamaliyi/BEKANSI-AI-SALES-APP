-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 006
-- EXACT LEADS TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS leads (
  id uuid primary key default gen_random_uuid(),

  customer_id uuid references customers(id),
  conversation_id uuid references conversations(id),

  product_id uuid,

  product_category text,

  quantity integer default 1,

  size text,
  color text,
  fabric text,
  finish text,

  led_required boolean,
  socket_required boolean,

  budget_min numeric,
  budget_max numeric,

  purchase_timeline text,

  lead_stage text default 'NEW',

  lead_score integer default 0,

  lead_temperature text default 'COLD',

  assigned_to uuid,

  next_followup_at timestamptz,
  last_followup_at timestamptz,

  lost_reason text,

  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Performance & CRM Pipeline Optimization Indexes
CREATE INDEX IF NOT EXISTS idx_leads_customer_id ON leads(customer_id);
CREATE INDEX IF NOT EXISTS idx_leads_conversation_id ON leads(conversation_id);
CREATE INDEX IF NOT EXISTS idx_leads_product_id ON leads(product_id);
CREATE INDEX IF NOT EXISTS idx_leads_product_category ON leads(product_category);
CREATE INDEX IF NOT EXISTS idx_leads_lead_stage ON leads(lead_stage);
CREATE INDEX IF NOT EXISTS idx_leads_lead_temperature ON leads(lead_temperature);
CREATE INDEX IF NOT EXISTS idx_leads_assigned_to ON leads(assigned_to);
CREATE INDEX IF NOT EXISTS idx_leads_next_followup ON leads(next_followup_at);
