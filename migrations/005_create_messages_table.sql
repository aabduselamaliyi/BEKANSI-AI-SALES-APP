-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 005
-- EXACT MESSAGES TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS messages (
  id uuid primary key default gen_random_uuid(),

  conversation_id uuid references conversations(id),

  whatsapp_message_id text unique,

  direction text not null,
  sender_type text,

  message_type text default 'text',

  text text,

  media_id text,
  media_url text,
  mime_type text,

  intent text,

  ai_generated boolean default false,
  ai_model text,
  ai_confidence numeric,

  tool_called text,
  tool_arguments jsonb,
  tool_result jsonb,

  delivery_status text,

  created_at timestamptz default now()
);

-- Performance & Query Optimization Indexes
CREATE INDEX IF NOT EXISTS idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_whatsapp_message_id ON messages(whatsapp_message_id);
CREATE INDEX IF NOT EXISTS idx_messages_direction ON messages(direction);
CREATE INDEX IF NOT EXISTS idx_messages_sender_type ON messages(sender_type);
CREATE INDEX IF NOT EXISTS idx_messages_ai_generated ON messages(ai_generated);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages(created_at);
