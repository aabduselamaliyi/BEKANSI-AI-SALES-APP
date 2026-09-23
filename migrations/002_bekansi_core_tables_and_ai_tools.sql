-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 002
-- 8 CORE SYSTEM TABLES FOR WHATSAPP, CRM, CATALOG & GEMINI AI AGENT
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. CUSTOMERS
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone_number VARCHAR(100) NOT NULL, -- Omnichannel unique key (WhatsApp, Telegram)
    email VARCHAR(255),
    city VARCHAR(100) DEFAULT 'Addis Ababa',
    subcity VARCHAR(100) DEFAULT 'Bole',
    preferred_language VARCHAR(10) DEFAULT 'am', -- 'en', 'am', 'om'
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_customers_org_phone UNIQUE (organization_id, phone_number)
);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone_number);
CREATE INDEX IF NOT EXISTS idx_customers_city_subcity ON customers(city, subcity);

-- 2. CONVERSATIONS
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    channel VARCHAR(50) DEFAULT 'whatsapp' NOT NULL, -- whatsapp, telegram, instagram, tiktok, showroom_app
    external_chat_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'bot_active' NOT NULL, -- bot_active, human_required, human_active, archived
    assigned_agent_id UUID,
    last_message_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_conv_org_channel_chat UNIQUE (organization_id, channel, external_chat_id)
);
CREATE INDEX IF NOT EXISTS idx_conversations_customer ON conversations(customer_id);
CREATE INDEX IF NOT EXISTS idx_conversations_status ON conversations(status);

-- 3. MESSAGES
CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE NOT NULL,
    sender_type VARCHAR(50) NOT NULL, -- 'customer', 'ai_assistant', 'human_agent'
    sender_id UUID,
    raw_content TEXT NOT NULL,
    translated_content JSONB DEFAULT '{}'::jsonb,
    media_url TEXT,
    media_type VARCHAR(50), -- 'image', 'audio', 'document'
    meta_message_id VARCHAR(255),
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at);

-- 4. LEADS
CREATE TABLE IF NOT EXISTS leads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    customer_id UUID REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    conversation_id UUID REFERENCES conversations(id) ON DELETE SET NULL,
    assigned_agent_id UUID,
    status VARCHAR(50) DEFAULT 'warm' NOT NULL, -- hot (🔥), warm (🟡), cold (⚪), won, lost
    product_interest VARCHAR(255),
    estimated_budget DECIMAL(12, 2),
    delivery_timeline VARCHAR(100),
    missing_info TEXT[] DEFAULT '{}',
    lead_score INT DEFAULT 50,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_leads_status ON leads(status);
CREATE INDEX IF NOT EXISTS idx_leads_customer ON leads(customer_id);

-- 5. PRODUCTS
CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    category_id UUID,
    sku VARCHAR(100) NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    name_am VARCHAR(255),
    name_om VARCHAR(255),
    names JSONB DEFAULT '{}'::jsonb,
    descriptions JSONB DEFAULT '{}'::jsonb,
    category VARCHAR(100) NOT NULL, -- Beds, Sofas, Kitchen Cabinets, Wardrobes, Dining Tables, TV Stands, Office Furniture
    dimensions VARCHAR(100),
    base_price_etb DECIMAL(12, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'ETB' NOT NULL,
    image_urls TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_products_org_sku UNIQUE (organization_id, sku)
);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

-- 6. PRODUCT_VARIANTS
CREATE TABLE IF NOT EXISTS product_variants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE NOT NULL,
    sku VARCHAR(100) NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    color VARCHAR(100), -- Turkish Velvet Royal Blue, Emerald Green, Walnut, Oak
    material VARCHAR(255), -- Kiln-dried hardwood, Turkish velvet, Sintered stone
    fabric_type VARCHAR(100),
    dimensions VARCHAR(100),
    stock_quantity INT DEFAULT 0 NOT NULL,
    is_in_stock BOOLEAN DEFAULT TRUE NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uq_variants_org_sku UNIQUE (organization_id, sku)
);
CREATE INDEX IF NOT EXISTS idx_variants_product ON product_variants(product_id);

-- 7. PRICES
CREATE TABLE IF NOT EXISTS prices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE NOT NULL,
    variant_id UUID REFERENCES product_variants(id) ON DELETE CASCADE,
    currency VARCHAR(10) DEFAULT 'ETB' NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    min_price DECIMAL(12, 2), -- Floor negotiated price
    promo_price DECIMAL(12, 2),
    delivery_cost_addis DECIMAL(12, 2) DEFAULT 2500.00 NOT NULL,
    delivery_cost_regional DECIMAL(12, 2) DEFAULT 9000.00 NOT NULL,
    installation_cost DECIMAL(12, 2) DEFAULT 3500.00 NOT NULL,
    effective_date DATE DEFAULT CURRENT_DATE NOT NULL,
    valid_until DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_prices_product ON prices(product_id);
CREATE INDEX IF NOT EXISTS idx_prices_variant ON prices(variant_id);

-- 8. AI_TOOL_LOGS
CREATE TABLE IF NOT EXISTS ai_tool_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL,
    conversation_id UUID REFERENCES conversations(id) ON DELETE SET NULL,
    message_id UUID REFERENCES messages(id) ON DELETE SET NULL,
    tool_name VARCHAR(100) NOT NULL, -- searchProductDatabase, checkPriceAndQuotation, upsertCustomerCRM, classifyAndSaveLead, requestHumanHandoff
    tool_input JSONB NOT NULL,
    tool_output JSONB NOT NULL,
    execution_time_ms INT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'SUCCESS' NOT NULL, -- SUCCESS, ERROR
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_ai_tool_logs_tool ON ai_tool_logs(tool_name);
CREATE INDEX IF NOT EXISTS idx_ai_tool_logs_conv ON ai_tool_logs(conversation_id);

-- ENABLE ROW-LEVEL SECURITY (RLS) FOR MULTI-TENANCY
ALTER TABLE customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE leads ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_variants ENABLE ROW LEVEL SECURITY;
ALTER TABLE prices ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_tool_logs ENABLE ROW LEVEL SECURITY;
