-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 003
-- EXACT CUSTOMERS TABLE DEFINITION
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS customers (
  id uuid primary key default gen_random_uuid(),

  full_name text,
  phone text not null unique,
  whatsapp_user_id text unique,

  preferred_language text default 'en',
  customer_type text default 'B2C',

  city text,
  sub_city text,
  woreda text,
  delivery_location text,

  source text,
  campaign text,
  ad_id text,

  status text default 'active',

  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Optimization indexes for rapid WhatsApp lookup and geographic filtering
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone);
CREATE INDEX IF NOT EXISTS idx_customers_whatsapp_user_id ON customers(whatsapp_user_id);
CREATE INDEX IF NOT EXISTS idx_customers_location ON customers(city, sub_city);
CREATE INDEX IF NOT EXISTS idx_customers_status ON customers(status);
