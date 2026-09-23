-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 007
-- EXACT PRODUCTS TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS products (
  id uuid primary key default gen_random_uuid(),

  sku text unique not null,
  name text not null,

  category text not null,

  description text,

  active boolean default true,

  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

-- Optimization Indexes for Instant Catalog Lookups and Category Filtering
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);

-- Initial Bekansi Master Catalog Seed
INSERT INTO products (sku, name, category, description, active)
VALUES 
  ('BK-BED-ENTOTO', 'Entoto Luxury Smart Bed', 'Beds', 'King size 180x200cm, integrated warm LED ambient lighting, acoustic fluted headboard, USB-C smart charging sockets, Turkish velvet upholstery.', true),
  ('BK-SOF-BOLE', 'Bole Luxury Sectional L-Shape Sofa', 'Sofas', 'High-density foam, stain-resistant imported Turkish fabric, solid eucalyptus and mahogany hardwood framing.', true),
  ('BK-DIN-ENTOTO', 'Entoto Marble Dining Set (8-Seater)', 'Dining Tables', 'Sintered marble top, heat and scratch resistant, solid oak legs with brushed brass accents.', true),
  ('BK-WRD-SEMEN', 'Semen 6-Door Walk-In Wardrobe', 'Wardrobes', 'German soft-close hinges, internal sensor LED rails, built-in jewelry drawers, anti-scratch matte melamine finish.', true),
  ('BK-KIT-MESKEL', 'Meskel Luxury Custom Kitchen Cabinet', 'Kitchen Cabinets', 'High-gloss acrylic shutters, Calacatta quartz countertops, soft-close Blum hardware, waterproof marine plywood carcass.', true),
  ('BK-TVS-FINFINE', 'Finfine Floating Wall Media Unit', 'TV Stands', 'Sintered stone backdrop, acoustic fluted panels, concealed cable management, smart warm ambient backlight.', true),
  ('BK-OFF-EXECUTIVE', 'Addis Executive Office Desk & Credenza', 'Office Furniture', 'Ergonomic wire management, genuine leather desk pad insert, lockable privacy drawers, contemporary walnut finish.', true)
ON CONFLICT (sku) 
DO UPDATE SET 
  name = EXCLUDED.name,
  category = EXCLUDED.category,
  description = EXCLUDED.description,
  active = EXCLUDED.active,
  updated_at = NOW();
