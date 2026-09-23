import { supabase } from "../config/database.js";


export async function searchProducts(query) {
  const normalized = query?.trim() || "";

  let request = supabase
    .from("products")
    .select(`
      id,
      sku,
      name,
      category,
      description,
      customizable
    `)
    .eq("active", true)
    .limit(10);

  if (normalized) {
    request = request.or(
      `name.ilike.%${normalized}%,category.ilike.%${normalized}%,description.ilike.%${normalized}%`
    );
  }

  const { data, error } = await request;

  if (error) throw error;

  return {
    products: data || []
  };
}


export async function getProduct(productSku) {
  const { data, error } = await supabase
    .from("products")
    .select("*")
    .eq("sku", productSku)
    .eq("active", true)
    .maybeSingle();

  if (error) throw error;

  if (!data) {
    return {
      found: false,
      message: "Product not found."
    };
  }

  const { data: variants } = await supabase
    .from("product_variants")
    .select("*")
    .eq("product_id", data.id)
    .eq("active", true);

  return {
    found: true,
    product: data,
    variants: variants || []
  };
}


export async function getProductPrice({
  product_sku,
  size = null
}) {
  const product = await getProduct(product_sku);

  if (!product.found) {
    return product;
  }

  let query = supabase
    .from("prices")
    .select(`
      *,
      product:products!inner(
        sku,
        name
      )
    `)
    .eq("product_id", product.product.id)
    .eq("active", true)
    .order("created_at", {
      ascending: false
    })
    .limit(1);

  const { data, error } = await query;

  if (error) throw error;

  if (!data || data.length === 0) {
    return {
      found: false,
      message: "No active price is currently configured for this product."
    };
  }

  const price = data[0];

  return {
    found: true,
    product_sku,
    product_name: product.product.name,
    size,
    price: Number(price.price),
    currency: price.currency,
    delivery_included: price.delivery_included
  };
}

// Service adapter for backward compatibility with existing AI tools & quotation generator
export const productService = {
  searchProducts: async ({ query = '', category = '' }) => {
    try {
      const res = await searchProducts(query || category);
      return res.products;
    } catch {
      return [];
    }
  },

  getProductBySku: async (sku) => {
    try {
      const res = await getProduct(sku);
      return res.found ? res.product : null;
    } catch {
      return null;
    }
  },

  getProductVariants: async ({ sku }) => {
    try {
      const res = await getProduct(sku);
      return res.found ? res.variants : [];
    } catch {
      return [];
    }
  },

  getPriceForProduct: async (sku, size = null) => {
    try {
      const res = await getProductPrice({ product_sku: sku, size });
      if (res.found) {
        return {
          price: res.price,
          currency: res.currency || 'ETB',
          delivery_included: res.delivery_included
        };
      }
      return null;
    } catch {
      return null;
    }
  },

  generateQuotation: async ({
    customerName,
    customerPhone,
    productName,
    sku,
    quantity = 1,
    deliveryLocation = 'Addis Ababa'
  }) => {
    const priceRes = await getProductPrice({ product_sku: sku });
    if (!priceRes.found) {
      return {
        success: false,
        message: `Pricing for ${productName || sku} is not configured or requires custom measurement.`
      };
    }

    const unitPrice = priceRes.price;
    const totalItemsPrice = unitPrice * quantity;
    const deliveryCost = priceRes.delivery_included ? 0 : 2000;
    const grandTotal = totalItemsPrice + deliveryCost;

    const quoteNumber = `QT-${Date.now().toString().slice(-6)}`;
    const validUntil = new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toLocaleDateString('en-GB');

    return {
      success: true,
      quotation: {
        quoteNumber,
        customerName: customerName || 'Valued Customer',
        customerPhone,
        productName: productName || priceRes.product_name,
        sku,
        quantity,
        unitPrice: `${unitPrice.toLocaleString()} ${priceRes.currency || 'ETB'}`,
        itemsTotal: `${totalItemsPrice.toLocaleString()} ${priceRes.currency || 'ETB'}`,
        deliveryCost: deliveryCost === 0 ? 'FREE (Delivery Included)' : `${deliveryCost.toLocaleString()} ETB (${deliveryLocation})`,
        totalEstimate: `${grandTotal.toLocaleString()} ${priceRes.currency || 'ETB'}`,
        validityPeriod: `14 Days (Valid until ${validUntil})`,
        paymentTerms: '50% advance upon order confirmation, 50% upon final delivery & inspection in Addis Ababa'
      }
    };
  }
};

export default productService;
