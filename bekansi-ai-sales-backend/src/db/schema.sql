-- ============================================================
-- BEKANSI AI SALES - PHASE 1 DATABASE
-- ============================================================

create extension if not exists pgcrypto;


-- ============================================================
-- CUSTOMERS
-- ============================================================

create table if not exists customers (
    id uuid primary key default gen_random_uuid(),

    full_name text,
    phone text not null unique,
    whatsapp_user_id text unique,

    preferred_language text default 'en',

    customer_type text default 'B2C'
        check (customer_type in ('B2C', 'B2B', 'UNKNOWN')),

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


-- ============================================================
-- CONVERSATIONS
-- ============================================================

create table if not exists conversations (
    id uuid primary key default gen_random_uuid(),

    customer_id uuid references customers(id) on delete cascade,

    channel text default 'whatsapp',

    whatsapp_phone text,

    status text default 'open'
        check (status in ('open', 'closed', 'human')),

    language text default 'en',

    ai_enabled boolean default true,

    human_assigned boolean default false,

    assigned_agent text,

    lead_stage text default 'NEW',

    gemini_interaction_id text,

    last_message_at timestamptz,
    last_customer_message_at timestamptz,
    last_ai_message_at timestamptz,
    last_human_message_at timestamptz,

    created_at timestamptz default now(),
    updated_at timestamptz default now()
);


-- ============================================================
-- MESSAGES
-- ============================================================

create table if not exists messages (
    id uuid primary key default gen_random_uuid(),

    conversation_id uuid
        references conversations(id)
        on delete cascade,

    whatsapp_message_id text unique,

    direction text not null
        check (direction in ('inbound', 'outbound')),

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


-- ============================================================
-- LEADS
-- ============================================================

create table if not exists leads (
    id uuid primary key default gen_random_uuid(),

    customer_id uuid
        references customers(id)
        on delete cascade,

    conversation_id uuid
        references conversations(id)
        on delete cascade,

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

    assigned_to text,

    next_followup_at timestamptz,
    last_followup_at timestamptz,

    lost_reason text,

    created_at timestamptz default now(),
    updated_at timestamptz default now()
);


-- ============================================================
-- PRODUCTS
-- ============================================================

create table if not exists products (
    id uuid primary key default gen_random_uuid(),

    sku text unique not null,

    name text not null,

    category text not null,

    description text,

    customizable boolean default true,

    active boolean default true,

    created_at timestamptz default now(),
    updated_at timestamptz default now()
);


-- ============================================================
-- PRODUCT VARIANTS
-- ============================================================

create table if not exists product_variants (
    id uuid primary key default gen_random_uuid(),

    product_id uuid
        references products(id)
        on delete cascade,

    size text,

    color text,

    fabric text,

    finish text,

    led_available boolean default false,

    socket_available boolean default false,

    active boolean default true,

    created_at timestamptz default now()
);


-- ============================================================
-- PRICES
-- ============================================================

create table if not exists prices (
    id uuid primary key default gen_random_uuid(),

    product_id uuid
        references products(id)
        on delete cascade,

    variant_id uuid
        references product_variants(id)
        on delete set null,

    price numeric not null,

    currency text default 'ETB',

    delivery_included boolean default false,

    effective_from timestamptz default now(),

    effective_until timestamptz,

    active boolean default true,

    created_at timestamptz default now()
);


-- ============================================================
-- AI TOOL LOGS
-- ============================================================

create table if not exists ai_tool_logs (
    id uuid primary key default gen_random_uuid(),

    conversation_id uuid
        references conversations(id)
        on delete set null,

    customer_id uuid
        references customers(id)
        on delete set null,

    model text,

    tool_name text,

    arguments jsonb,

    result jsonb,

    success boolean default true,

    error text,

    created_at timestamptz default now()
);


-- ============================================================
-- INDEXES
-- ============================================================

create index if not exists idx_customers_phone
on customers(phone);

create index if not exists idx_messages_conversation
on messages(conversation_id);

create index if not exists idx_messages_whatsapp_id
on messages(whatsapp_message_id);

create index if not exists idx_leads_customer
on leads(customer_id);

create index if not exists idx_leads_stage
on leads(lead_stage);

create index if not exists idx_products_category
on products(category);

create index if not exists idx_prices_product
on prices(product_id);


-- ============================================================
-- BEKANSI INITIAL PRODUCTS
-- ============================================================

insert into products
    (sku, name, category, description, customizable)
values
(
    'BED-150',
    'Custom Luxury Bed 1.50 × 2.00m',
    'bed',
    'Modern customizable bed with multiple color, fabric and finishing options.',
    true
),
(
    'BED-180',
    'Custom Luxury Bed 1.80 × 2.00m',
    'bed',
    'Premium king-size customizable bed with multiple color, fabric and finishing options.',
    true
)
on conflict (sku) do nothing;


-- ============================================================
-- INITIAL PRICES
-- ============================================================

insert into prices
    (product_id, price, currency, delivery_included)
select
    id,
    80000,
    'ETB',
    true
from products
where sku = 'BED-150'
and not exists (
    select 1
    from prices p
    where p.product_id = products.id
);


insert into prices
    (product_id, price, currency, delivery_included)
select
    id,
    90000,
    'ETB',
    true
from products
where sku = 'BED-180'
and not exists (
    select 1
    from prices p
    where p.product_id = products.id
);


-- ============================================================
-- VERIFY
-- ============================================================

select
    p.sku,
    p.name,
    pr.price,
    pr.currency,
    pr.delivery_included
from products p
join prices pr
    on pr.product_id = p.id
where p.active = true
and pr.active = true;
