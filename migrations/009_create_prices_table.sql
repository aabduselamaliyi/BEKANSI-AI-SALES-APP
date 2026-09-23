-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 009
-- EXACT PRICES TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS prices (
  id uuid primary key default gen_random_uuid(),

  product_id uuid references products(id),
  variant_id uuid references product_variants(id),

  price numeric not null,
  currency text default 'ETB',

  delivery_included boolean default false,

  effective_from timestamptz default now(),
  effective_until timestamptz,

  active boolean default true
);

-- Performance & Pricing Lookup Indexes
CREATE INDEX IF NOT EXISTS idx_prices_product_id ON prices(product_id);
CREATE INDEX IF NOT EXISTS idx_prices_variant_id ON prices(variant_id);
CREATE INDEX IF NOT EXISTS idx_prices_active ON prices(active);
CREATE INDEX IF NOT EXISTS idx_prices_effective ON prices(effective_from, effective_until);

-- Initial Bekansi Master ETB Price Matrix
-- Links base product prices and customization variant prices
INSERT INTO prices (product_id, variant_id, price, currency, delivery_included, active)
SELECT 
  p.id AS product_id,
  NULL AS variant_id,
  p_prices.base_price,
  'ETB',
  false,
  true
FROM products p
JOIN (
  VALUES 
    ('BK-BED-ENTOTO', 85000),
    ('BK-SOF-BOLE', 145000),
    ('BK-KIT-MESKEL', 38000),
    ('BK-WRD-SEMEN', 92000),
    ('BK-DIN-ENTOTO', 110000),
    ('BK-OFF-EXECUTIVE', 125000),
    ('BK-TVS-FINFINE', 48000)
) AS p_prices(sku, base_price)
ON p.sku = p_prices.sku;

-- Insert Variant-Specific Pricing
INSERT INTO prices (product_id, variant_id, price, currency, delivery_included, active)
SELECT 
  v.product_id,
  v.id AS variant_id,
  v_prices.price,
  'ETB',
  false,
  true
FROM product_variants v
JOIN products p ON v.product_id = p.id
JOIN (
  VALUES 
    ('BK-BED-ENTOTO', 'Royal Blue', 88000),
    ('BK-BED-ENTOTO', 'Emerald Green', 88000),
    ('BK-BED-ENTOTO', 'Warm Beige', 78000),
    ('BK-SOF-BOLE', 'Charcoal Grey', 145000),
    ('BK-SOF-BOLE', 'Navy Blue', 152000),
    ('BK-WRD-SEMEN', 'Matte White & Grey Glass', 92000),
    ('BK-DIN-ENTOTO', 'Calacatta Gold White Marble', 110000),
    ('BK-TVS-FINFINE', 'Grey Sintered Stone & Walnut', 48000)
) AS v_prices(sku, color, price)
ON p.sku = v_prices.sku AND v.color = v_prices.color;
