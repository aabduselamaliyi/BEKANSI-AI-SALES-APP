-- ============================================================================
-- BEKANSI AI SALES PLATFORM - MIGRATION 008
-- EXACT PRODUCT_VARIANTS TABLE DEFINITION
-- ============================================================================

CREATE TABLE IF NOT EXISTS product_variants (
  id uuid primary key default gen_random_uuid(),

  product_id uuid references products(id),

  size text,
  color text,
  fabric text,
  finish text,

  led_available boolean default false,
  socket_available boolean default false,

  active boolean default true
);

-- Performance & Catalog Discovery Indexes
CREATE INDEX IF NOT EXISTS idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_product_variants_active ON product_variants(active);
CREATE INDEX IF NOT EXISTS idx_product_variants_size ON product_variants(size);
CREATE INDEX IF NOT EXISTS idx_product_variants_color ON product_variants(color);

-- Initial Seed Data for Bekansi Luxury Product Customization Variants
INSERT INTO product_variants (product_id, size, color, fabric, finish, led_available, socket_available, active)
SELECT 
  p.id, 
  v.size, 
  v.color, 
  v.fabric, 
  v.finish, 
  v.led_available, 
  v.socket_available, 
  v.active
FROM products p
JOIN (
  VALUES 
    -- Bed Variants (Entoto Smart Bed)
    ('BK-BED-ENTOTO', 'King (180x200cm)', 'Royal Blue', 'Imported Turkish Velvet', 'High Gloss Walnut', true, true, true),
    ('BK-BED-ENTOTO', 'King (180x200cm)', 'Emerald Green', 'Imported Turkish Velvet', 'Matte Oak', true, true, true),
    ('BK-BED-ENTOTO', 'Queen (160x200cm)', 'Warm Beige', 'Textured Linen Blend', 'Natural Teak', true, false, true),
    
    -- Sofa Variants (Bole Sectional)
    ('BK-SOF-BOLE', '5-Seater L-Shape (320x240cm)', 'Charcoal Grey', 'Stain-Resistant Turkish Fabric', 'Solid Mahogany Feet', true, false, true),
    ('BK-SOF-BOLE', '5-Seater L-Shape (320x240cm)', 'Navy Blue', 'Water-Repellent Velvet', 'Brushed Brass Feet', true, false, true),
    ('BK-SOF-BOLE', '3-Seater Regular (240cm)', 'Camel Brown', 'Genuine Top-Grain Leather', 'Solid Eucalyptus Feet', false, false, true),

    -- Wardrobe Variants (Semen Wardrobe)
    ('BK-WRD-SEMEN', '6-Door (240x270x65cm)', 'Matte White & Grey Glass', 'Melamine Carcass', 'Scratch-Proof Matte', true, false, true),
    ('BK-WRD-SEMEN', '4-Door (240x180x65cm)', 'Smoked Walnut & Mirrored', 'Melamine Carcass', 'Textured Woodgrain', true, false, true),

    -- Dining Variants (Entoto Marble)
    ('BK-DIN-ENTOTO', '8-Seater (220x100cm)', 'Calacatta Gold White Marble', 'High-Density Velvet Chairs', 'Solid Oak & Brass', false, false, true),
    ('BK-DIN-ENTOTO', '6-Seater (180x90cm)', 'Nero Marquina Black Marble', 'Leather Padded Chairs', 'Walnut & Black Steel', false, false, true),

    -- TV Stand Variants (Finfine Console)
    ('BK-TVS-FINFINE', '240cm Wall Unit', 'Grey Sintered Stone & Walnut', 'Acoustic Felt Slats', 'Matte Lacquer', true, true, true),
    ('BK-TVS-FINFINE', '200cm Compact Unit', 'White Marble Texture & Oak', 'Acoustic Fluted MDF', 'Satin Finish', true, true, true)
) AS v(sku, size, color, fabric, finish, led_available, socket_available, active)
ON p.sku = v.sku;
