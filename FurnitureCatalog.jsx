import React, { useState, useMemo, useEffect } from 'react';
import {
  Search,
  Filter,
  Plus,
  Share2,
  Grid,
  List,
  CheckCircle,
  AlertTriangle,
  XCircle,
  DollarSign,
  Package,
  Layers,
  ChevronRight,
  Edit2,
  Trash2,
  Eye,
  SlidersHorizontal,
  X,
  Sparkles,
  ShieldCheck,
  Check,
  Building,
  RefreshCw
} from 'lucide-react';

/**
 * BEKANSI AI SALES — DIRECT ILILI ERP PRODUCTS THEME IMPLEMENTATION
 * Reference: https://ililierp.base44.app/products
 * 
 * Branded strictly with BEKANSI Design Identity:
 * - Deep Navy: #0A2E5D
 * - Gold Accent: #D4AF37
 * - Pure White: #FFFFFF
 * - Light Gray: #F5F5F5
 */

// Initial Seed Products grounded strictly in Bekansi Wood Species & Furniture Catalog
const INITIAL_PRODUCTS = [
  {
    id: "prod-bk-101",
    name: "Modern King Bed (1.80m × 2.00m)",
    sku: "BK-BED-180-001",
    barcode: "600180200001",
    category: "Beds",
    productType: "Finished Product",
    brand: "BEKANSI Luxury",
    unit: "Unit",
    costPrice: 48000,
    wholesalePrice: 72000,
    resellerPrice: 78000,
    retailPrice: 90000,
    minSellingPrice: 85000,
    stock: 14,
    minStock: 3,
    maxStock: 25,
    warehouse: "Central Factory (Dukem)",
    status: "Active",
    material: "Wanza (Cordia Africana)",
    dimensions: "1.80m × 2.00m × 1.10m",
    warranty: "5 Years Structural",
    description: "Handcrafted master bed with integrated warm LED backlighting and Turkish velvet upholstered headboard. Delivery included in Addis Ababa and surrounding regions.",
    image: "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "1.80 × 2.00m", color: "Royal Blue", fabric: "Velvet", led: true, price: 90000 },
      { size: "1.80 × 2.00m", color: "Warm Beige", fabric: "Linen", led: false, price: 85000 },
      { size: "1.50 × 2.00m", color: "Charcoal Grey", fabric: "Velvet", led: true, price: 80000 }
    ],
    updatedAt: "2026-06-20"
  },
  {
    id: "prod-bk-102",
    name: "Queen Floating Bed (1.50m × 2.00m)",
    sku: "BK-BED-150-002",
    barcode: "600150200002",
    category: "Beds",
    productType: "Finished Product",
    brand: "BEKANSI Luxury",
    unit: "Unit",
    costPrice: 42000,
    wholesalePrice: 65000,
    resellerPrice: 70000,
    retailPrice: 80000,
    minSellingPrice: 75000,
    stock: 8,
    minStock: 2,
    maxStock: 20,
    warehouse: "Central Factory (Dukem)",
    status: "Active",
    material: "Grar Acacia & Mahogany",
    dimensions: "1.50m × 2.00m × 1.05m",
    warranty: "5 Years Structural",
    description: "Compact floating silhouette queen bed featuring kiln-dried Ethiopian Acacia hardwood framework with seamless floating side pedestals.",
    image: "https://images.unsplash.com/photo-1540518614846-7ede433c4ef3?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "1.50 × 2.00m", color: "Natural Honey", fabric: "Textured Linen", led: true, price: 80000 },
      { size: "1.50 × 2.00m", color: "Dark Walnut", fabric: "Linen", led: false, price: 76000 }
    ],
    updatedAt: "2026-06-18"
  },
  {
    id: "prod-bk-103",
    name: "Bole Sectional L-Shape Curved Sofa",
    sku: "BK-SOF-001",
    barcode: "600320240003",
    category: "Sofas",
    productType: "Finished Product",
    brand: "BEKANSI Home",
    unit: "Set",
    costPrice: 75000,
    wholesalePrice: 115000,
    resellerPrice: 125000,
    retailPrice: 145000,
    minSellingPrice: 135000,
    stock: 5,
    minStock: 2,
    maxStock: 15,
    warehouse: "Bole Showroom Annex",
    status: "Active",
    material: "Solid Wanza Timber & High-Density Foam",
    dimensions: "3.20m × 2.40m × 0.85m",
    warranty: "5 Years Comprehensive",
    description: "Ultra-luxury modular curved sectional sofa with water-repellent Turkish velvet fabric and high-density orthopedic comfort foam cushions.",
    image: "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "5-Seater L-Shape", color: "Charcoal Grey", fabric: "Turkish Velvet", led: false, price: 145000 },
      { size: "5-Seater L-Shape", color: "Navy Blue", fabric: "Turkish Velvet", led: false, price: 145000 }
    ],
    updatedAt: "2026-06-22"
  },
  {
    id: "prod-bk-104",
    name: "Zid Mahogany 8-Seater Dining Table",
    sku: "BK-DIN-001",
    barcode: "600240100004",
    category: "Dining Tables",
    productType: "Finished Product",
    brand: "BEKANSI Legacy",
    unit: "Set",
    costPrice: 50000,
    wholesalePrice: 75000,
    resellerPrice: 82000,
    retailPrice: 90000,
    minSellingPrice: 85000,
    stock: 3,
    minStock: 2,
    maxStock: 10,
    warehouse: "Central Factory (Dukem)",
    status: "Active",
    material: "Solid Zid Mahogany & Brushed Brass",
    dimensions: "2.40m × 1.00m × 0.76m",
    warranty: "10 Years Heirloom",
    description: "Grand architectural 8-seater dining table pairing deep-hued solid Ethiopian Mahogany with brushed brass inlays and hand-rubbed organic protective lacquer.",
    image: "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "8-Seater (2.40m)", color: "Deep Mahogany", fabric: "N/A", led: false, price: 90000 },
      { size: "6-Seater (1.80m)", color: "Deep Mahogany", fabric: "N/A", led: false, price: 78000 }
    ],
    updatedAt: "2026-06-15"
  },
  {
    id: "prod-bk-105",
    name: "Semen 6-Door Walk-In Wardrobe",
    sku: "BK-WRD-001",
    barcode: "600270240005",
    category: "Wardrobes",
    productType: "Custom Product",
    brand: "BEKANSI Bespoke",
    unit: "Unit",
    costPrice: 48000,
    wholesalePrice: 74000,
    resellerPrice: 80000,
    retailPrice: 90000,
    minSellingPrice: 85000,
    stock: 2,
    minStock: 2,
    maxStock: 8,
    warehouse: "Central Factory (Dukem)",
    status: "Active",
    material: "Matte Melamine & Glass Doors",
    dimensions: "2.70m × 2.40m × 0.65m",
    warranty: "5 Years Hardware",
    description: "Modular floor-to-ceiling wardrobe with German soft-close Blum hinges, sensor activated LED lighting rails, and velvet-lined jewelry organizational trays.",
    image: "https://images.unsplash.com/photo-1558997519-83ea9252def8?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "6-Door (2.70m)", color: "Matte White & Grey Glass", fabric: "N/A", led: true, price: 90000 },
      { size: "4-Door (1.80m)", color: "Smoked Walnut", fabric: "N/A", led: true, price: 72000 }
    ],
    updatedAt: "2026-06-19"
  },
  {
    id: "prod-bk-106",
    name: "Finfine Floating Acoustic TV Wall Unit",
    sku: "BK-TVS-001",
    barcode: "600000007500",
    category: "TV Stands",
    productType: "Custom Product",
    brand: "BEKANSI Bespoke",
    unit: "sqm",
    costPrice: 4200,
    wholesalePrice: 6200,
    resellerPrice: 6800,
    retailPrice: 7500,
    minSellingPrice: 7000,
    stock: 20,
    minStock: 5,
    maxStock: 50,
    warehouse: "Central Factory (Dukem)",
    status: "Active",
    material: "Sintered Stone & Fluted Oak Panels",
    dimensions: "Per Square Meter (7,500 ETB/sqm)",
    warranty: "3 Years",
    description: "Bespoke media backdrop wall featuring sintered stone slab center, acoustic fluted oak slat panels, concealed cable conduits, and warm ambient backlighting.",
    image: "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "Custom Sqm", color: "Calacatta White & Oak", fabric: "N/A", led: true, price: 7500 },
      { size: "Custom Sqm", color: "Nero Marquina Black", fabric: "N/A", led: true, price: 8200 }
    ],
    updatedAt: "2026-06-21"
  },
  {
    id: "prod-bk-107",
    name: "Abay Executive Office Command Desk",
    sku: "BK-OFF-001",
    barcode: "600180090007",
    category: "Office Furniture",
    productType: "Finished Product",
    brand: "BEKANSI Executive",
    unit: "Unit",
    costPrice: 55000,
    wholesalePrice: 78000,
    resellerPrice: 85000,
    retailPrice: 95000,
    minSellingPrice: 90000,
    stock: 1,
    minStock: 2,
    maxStock: 6,
    warehouse: "Bole Showroom Annex",
    status: "Active",
    material: "Mahogany Hardwood & Leather Inlay",
    dimensions: "1.80m × 0.90m × 0.76m",
    warranty: "5 Years",
    description: "Prestige boardroom executive workstation built from solid Mahogany panels, equipped with hidden desktop wireless charging and lockable security credenza.",
    image: "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "1.80m Standard", color: "Warm Mahogany", fabric: "Leather Inlay", led: false, price: 95000 }
    ],
    updatedAt: "2026-06-11"
  },
  {
    id: "prod-bk-108",
    name: "Meskel Acrylic Waterproof Kitchen Cabinet",
    sku: "BK-KIT-001",
    barcode: "600300250008",
    category: "Kitchen Cabinets",
    productType: "Custom Product",
    brand: "BEKANSI Bespoke",
    unit: "Running Meter",
    costPrice: 22000,
    wholesalePrice: 32000,
    resellerPrice: 35000,
    retailPrice: 38000,
    minSellingPrice: 35000,
    stock: 0,
    minStock: 5,
    maxStock: 40,
    warehouse: "Central Factory (Dukem)",
    status: "Out of Stock",
    material: "Marine Waterproof Plywood & Acrylic",
    dimensions: "Per Running Meter",
    warranty: "10 Years Waterproof",
    description: "High-gloss German acrylic fronts with Calacatta quartz worktops, marine waterproof structural core, and motorized Blum lift-up wall cabinet dampers.",
    image: "https://images.unsplash.com/photo-1556911220-e15b29be8c8f?auto=format&fit=crop&w=600&q=80",
    variants: [
      { size: "Per Meter", color: "Gloss White & Graphite", fabric: "N/A", led: true, price: 38000 }
    ],
    updatedAt: "2026-06-08"
  }
];

export default function BekansiErpProductsPage() {
  const [products, setProducts] = useState(INITIAL_PRODUCTS);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [selectedStatus, setSelectedStatus] = useState('All');
  const [selectedStock, setSelectedStock] = useState('All');
  const [viewMode, setViewMode] = useState('table'); // 'table' | 'grid'
  const [isFilterExpanded, setIsFilterExpanded] = useState(false);

  // Dialog & Modal States
  const [selectedProductForDetail, setSelectedProductForDetail] = useState(null);
  const [isAddProductOpen, setIsAddProductOpen] = useState(false);
  const [productToEdit, setProductToEdit] = useState(null);

  // Form State for Add/Edit
  const [formData, setFormData] = useState({
    name: '',
    sku: '',
    barcode: '',
    category: 'Beds',
    productType: 'Finished Product',
    costPrice: '',
    wholesalePrice: '',
    resellerPrice: '',
    retailPrice: '',
    minSellingPrice: '',
    stock: '',
    minStock: '3',
    maxStock: '20',
    warehouse: 'Central Factory (Dukem)',
    material: 'Wanza (Cordia Africana)',
    dimensions: '1.80m × 2.00m',
    warranty: '5 Years Structural',
    description: '',
    image: ''
  });

  // Calculate ERP KPI Metrics
  const metrics = useMemo(() => {
    const total = products.length;
    const active = products.filter(p => p.status === 'Active').length;
    const lowStock = products.filter(p => p.stock > 0 && p.stock <= p.minStock).length;
    const outOfStock = products.filter(p => p.stock === 0).length;
    const totalCostVal = products.reduce((acc, p) => acc + (p.stock * p.costPrice), 0);
    const totalRetailVal = products.reduce((acc, p) => acc + (p.stock * p.retailPrice), 0);

    return {
      total,
      active,
      lowStock,
      outOfStock,
      totalCostVal: totalCostVal.toLocaleString('en-US'),
      totalRetailVal: totalRetailVal.toLocaleString('en-US')
    };
  }, [products]);

  // Categories list
  const CATEGORIES = [
    'All',
    'Beds',
    'Sofas',
    'Wardrobes',
    'Kitchen Cabinets',
    'TV Stands',
    'Dining Tables',
    'Office Furniture'
  ];

  // Filter products
  const filteredProducts = useMemo(() => {
    return products.filter(p => {
      const q = searchQuery.toLowerCase();
      const matchesSearch =
        p.name.toLowerCase().includes(q) ||
        p.sku.toLowerCase().includes(q) ||
        p.category.toLowerCase().includes(q) ||
        p.barcode.includes(q) ||
        p.material.toLowerCase().includes(q);

      const matchesCat = selectedCategory === 'All' || p.category === selectedCategory;
      const matchesStat = selectedStatus === 'All' || p.status === selectedStatus;

      let matchesStock = true;
      if (selectedStock === 'In Stock') matchesStock = p.stock > p.minStock;
      else if (selectedStock === 'Low Stock') matchesStock = p.stock > 0 && p.stock <= p.minStock;
      else if (selectedStock === 'Out of Stock') matchesStock = p.stock === 0;

      return matchesSearch && matchesCat && matchesStat && matchesStock;
    });
  }, [products, searchQuery, selectedCategory, selectedStatus, selectedStock]);

  const handleOpenAdd = () => {
    setFormData({
      name: '',
      sku: `BK-NEW-${Math.floor(100 + Math.random() * 900)}`,
      barcode: `600${Date.now().toString().slice(-9)}`,
      category: 'Beds',
      productType: 'Finished Product',
      costPrice: '45000',
      wholesalePrice: '68000',
      resellerPrice: '75000',
      retailPrice: '90000',
      minSellingPrice: '82000',
      stock: '5',
      minStock: '2',
      maxStock: '20',
      warehouse: 'Central Factory (Dukem)',
      material: 'Wanza (Cordia Africana)',
      dimensions: '1.80m × 2.00m',
      warranty: '5 Years Structural',
      description: 'Manufactured at BEKANSI Dukem workshop with kiln-dried local timber and premium joinery.',
      image: 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80'
    });
    setProductToEdit(null);
    setIsAddProductOpen(true);
  };

  const handleOpenEdit = (p, e) => {
    if (e) e.stopPropagation();
    setProductToEdit(p);
    setFormData({
      name: p.name,
      sku: p.sku,
      barcode: p.barcode,
      category: p.category,
      productType: p.productType,
      costPrice: p.costPrice.toString(),
      wholesalePrice: p.wholesalePrice.toString(),
      resellerPrice: p.resellerPrice.toString(),
      retailPrice: p.retailPrice.toString(),
      minSellingPrice: p.minSellingPrice.toString(),
      stock: p.stock.toString(),
      minStock: p.minStock.toString(),
      maxStock: p.maxStock.toString(),
      warehouse: p.warehouse,
      material: p.material,
      dimensions: p.dimensions,
      warranty: p.warranty,
      description: p.description,
      image: p.image
    });
    setIsAddProductOpen(true);
  };

  const handleSaveProduct = (e) => {
    e.preventDefault();
    if (!formData.name.trim()) return;

    if (productToEdit) {
      setProducts(products.map(item => {
        if (item.id === productToEdit.id) {
          return {
            ...item,
            name: formData.name,
            sku: formData.sku,
            barcode: formData.barcode,
            category: formData.category,
            productType: formData.productType,
            costPrice: parseFloat(formData.costPrice) || item.costPrice,
            wholesalePrice: parseFloat(formData.wholesalePrice) || item.wholesalePrice,
            resellerPrice: parseFloat(formData.resellerPrice) || item.resellerPrice,
            retailPrice: parseFloat(formData.retailPrice) || item.retailPrice,
            minSellingPrice: parseFloat(formData.minSellingPrice) || item.minSellingPrice,
            stock: parseInt(formData.stock, 10) || 0,
            material: formData.material,
            dimensions: formData.dimensions,
            warranty: formData.warranty,
            description: formData.description,
            image: formData.image,
            updatedAt: new Date().toISOString().split('T')[0]
          };
        }
        return item;
      }));
    } else {
      const newProd = {
        id: `prod-bk-${Date.now()}`,
        name: formData.name,
        sku: formData.sku,
        barcode: formData.barcode,
        category: formData.category,
        productType: formData.productType,
        brand: 'BEKANSI Luxury',
        unit: 'Unit',
        costPrice: parseFloat(formData.costPrice) || 45000,
        wholesalePrice: parseFloat(formData.wholesalePrice) || 70000,
        resellerPrice: parseFloat(formData.resellerPrice) || 75000,
        retailPrice: parseFloat(formData.retailPrice) || 90000,
        minSellingPrice: parseFloat(formData.minSellingPrice) || 82000,
        stock: parseInt(formData.stock, 10) || 1,
        minStock: parseInt(formData.minStock, 10) || 2,
        maxStock: parseInt(formData.maxStock, 10) || 20,
        warehouse: formData.warehouse,
        status: parseInt(formData.stock, 10) > 0 ? 'Active' : 'Out of Stock',
        material: formData.material,
        dimensions: formData.dimensions,
        warranty: formData.warranty,
        description: formData.description,
        image: formData.image || 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80',
        variants: [
          { size: formData.dimensions, color: 'Standard Showroom', fabric: 'Natural Wood', led: false, price: parseFloat(formData.retailPrice) || 90000 }
        ],
        updatedAt: new Date().toISOString().split('T')[0]
      };
      setProducts([newProd, ...products]);
    }
    setIsAddProductOpen(false);
  };

  const handleDeleteProduct = (id, e) => {
    if (e) e.stopPropagation();
    if (window.confirm("Are you sure you want to remove this product from the ERP catalog?")) {
      setProducts(products.filter(p => p.id !== id));
      if (selectedProductForDetail?.id === id) {
        setSelectedProductForDetail(null);
      }
    }
  };

  return (
    <div className="w-full min-h-screen bg-[#F5F5F5] text-slate-800 font-sans antialiased p-4 md:p-6 lg:p-8">
      {/* ======================================================== */}
      {/* 1. HEADER: Title, Subtitle, and ERP Action Bar           */}
      {/* ======================================================== */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 mb-6 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2 mb-1.5">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-bold bg-[#0A2E5D]/10 text-[#0A2E5D] border border-[#0A2E5D]/20">
              <Building className="w-3 h-3 mr-1 text-[#D4AF37]" />
              BEKANSI AI SALES ERP
            </span>
            <span className="text-xs text-slate-500 font-medium">
              Source of Truth: Postgres / Supabase Isolated
            </span>
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold text-[#0A2E5D] tracking-tight">
            Products
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Manage products, pricing, inventory, variants and sales information.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2.5">
          <button 
            onClick={() => alert("Product catalog synchronized with BEKANSI AI Agent tools & Supabase!")}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white text-slate-700 text-xs font-semibold rounded-lg border border-slate-300 shadow-sm hover:bg-slate-50 transition"
          >
            <RefreshCw className="w-3.5 h-3.5 text-slate-500" />
            Sync AI Tools
          </button>
          <button 
            onClick={() => alert("Exporting product inventory matrix (CSV/Excel)...")}
            className="inline-flex items-center gap-1.5 px-3 py-2 bg-white text-slate-700 text-xs font-semibold rounded-lg border border-slate-300 shadow-sm hover:bg-slate-50 transition"
          >
            <Share2 className="w-3.5 h-3.5 text-slate-500" />
            Export
          </button>
          <button
            onClick={handleOpenAdd}
            className="inline-flex items-center gap-1.5 px-4 py-2 bg-[#0A2E5D] text-white text-xs font-bold rounded-lg shadow-sm hover:bg-[#071f3f] transition active:scale-95"
          >
            <Plus className="w-4 h-4 text-[#D4AF37]" />
            + Add Product
          </button>
        </div>
      </div>

      {/* ======================================================== */}
      {/* 2. PRODUCT SUMMARY KPI CARDS (ILILI ERP Structured)      */}
      {/* ======================================================== */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3.5 mb-6">
        {/* Total Products */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Total Products</span>
            <span className="p-1.5 rounded-lg bg-[#0A2E5D]/5 text-[#0A2E5D]">
              <Package className="w-4 h-4" />
            </span>
          </div>
          <div className="text-2xl font-black text-[#0A2E5D]">{metrics.total}</div>
          <div className="text-[11px] text-slate-400 mt-1">Catalog master count</div>
        </div>

        {/* Active Products */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Active</span>
            <span className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600">
              <CheckCircle className="w-4 h-4" />
            </span>
          </div>
          <div className="text-2xl font-black text-emerald-600">{metrics.active}</div>
          <div className="text-[11px] text-slate-400 mt-1">Available in showroom</div>
        </div>

        {/* Low Stock */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Low Stock</span>
            <span className="p-1.5 rounded-lg bg-amber-50 text-amber-600">
              <AlertTriangle className="w-4 h-4" />
            </span>
          </div>
          <div className="text-2xl font-black text-amber-600">{metrics.lowStock}</div>
          <div className="text-[11px] text-slate-400 mt-1">Below reorder point</div>
        </div>

        {/* Out of Stock */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Out of Stock</span>
            <span className="p-1.5 rounded-lg bg-rose-50 text-rose-600">
              <XCircle className="w-4 h-4" />
            </span>
          </div>
          <div className="text-2xl font-black text-rose-600">{metrics.outOfStock}</div>
          <div className="text-[11px] text-slate-400 mt-1">Made to order only</div>
        </div>

        {/* Inventory Cost Value */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Inventory Cost</span>
            <span className="p-1.5 rounded-lg bg-[#D4AF37]/15 text-[#0A2E5D]">
              <Layers className="w-4 h-4 text-[#D4AF37]" />
            </span>
          </div>
          <div className="text-xl font-black text-[#0A2E5D] truncate">{metrics.totalCostVal}</div>
          <div className="text-[11px] text-slate-400 mt-1">ETB (Manufacturing)</div>
        </div>

        {/* Retail Value */}
        <div className="bg-white rounded-xl p-4 border border-slate-200/80 shadow-sm">
          <div className="flex items-center justify-between text-slate-400 mb-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">Retail Value</span>
            <span className="p-1.5 rounded-lg bg-emerald-50 text-emerald-600">
              <DollarSign className="w-4 h-4" />
            </span>
          </div>
          <div className="text-xl font-black text-emerald-700 truncate">{metrics.totalRetailVal}</div>
          <div className="text-[11px] text-slate-400 mt-1">ETB (Sales Potential)</div>
        </div>
      </div>

      {/* ======================================================== */}
      {/* 3. PRODUCT SEARCH & COMBINABLE FILTERS (ILILI UX)        */}
      {/* ======================================================== */}
      <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm p-3.5 mb-6">
        <div className="flex flex-col md:flex-row items-stretch md:items-center justify-between gap-3">
          {/* Search bar */}
          <div className="relative flex-1">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search products by name, SKU, category or barcode..."
              className="w-full pl-9 pr-8 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D] focus:border-[#0A2E5D]"
            />
            {searchQuery && (
              <button 
                onClick={() => setSearchQuery('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* Quick Category Tabs */}
          <div className="hidden xl:flex items-center gap-1 overflow-x-auto py-1">
            {CATEGORIES.slice(0, 5).map(cat => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
                  selectedCategory === cat
                    ? 'bg-[#0A2E5D] text-white'
                    : 'text-slate-600 hover:bg-slate-100'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Filter Toggle & View Switcher */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsFilterExpanded(!isFilterExpanded)}
              className={`inline-flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs font-semibold border transition ${
                isFilterExpanded || selectedCategory !== 'All' || selectedStatus !== 'All' || selectedStock !== 'All'
                  ? 'bg-[#0A2E5D] text-white border-[#0A2E5D]'
                  : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
              }`}
            >
              <SlidersHorizontal className="w-3.5 h-3.5" />
              <span>Filters</span>
              {(selectedCategory !== 'All' || selectedStatus !== 'All' || selectedStock !== 'All') && (
                <span className="w-2 h-2 rounded-full bg-[#D4AF37]"></span>
              )}
            </button>

            {/* View Mode Switcher (Table vs Grid) */}
            <div className="inline-flex rounded-lg border border-slate-300 bg-slate-100 p-0.5">
              <button
                onClick={() => setViewMode('table')}
                className={`p-1.5 rounded-md transition ${viewMode === 'table' ? 'bg-white text-[#0A2E5D] shadow-xs' : 'text-slate-500 hover:text-slate-700'}`}
                title="Table View"
              >
                <List className="w-4 h-4" />
              </button>
              <button
                onClick={() => setViewMode('grid')}
                className={`p-1.5 rounded-md transition ${viewMode === 'grid' ? 'bg-white text-[#0A2E5D] shadow-xs' : 'text-slate-500 hover:text-slate-700'}`}
                title="Grid View"
              >
                <Grid className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>

        {/* Collapsible Advanced Filters Drawer */}
        {isFilterExpanded && (
          <div className="mt-3 pt-3 border-t border-slate-200 grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-bold text-slate-600 uppercase mb-1">Category</label>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full text-xs bg-slate-50 border border-slate-200 rounded-lg p-2 text-slate-700 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
              >
                {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-600 uppercase mb-1">Status</label>
              <select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                className="w-full text-xs bg-slate-50 border border-slate-200 rounded-lg p-2 text-slate-700 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
              >
                <option value="All">All Statuses</option>
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
                <option value="Out of Stock">Out of Stock</option>
              </select>
            </div>

            <div>
              <label className="block text-[11px] font-bold text-slate-600 uppercase mb-1">Stock Level</label>
              <div className="flex items-center gap-2">
                <select
                  value={selectedStock}
                  onChange={(e) => setSelectedStock(e.target.value)}
                  className="flex-1 text-xs bg-slate-50 border border-slate-200 rounded-lg p-2 text-slate-700 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                >
                  <option value="All">All Stock Levels</option>
                  <option value="In Stock">In Stock (&gt;3)</option>
                  <option value="Low Stock">Low Stock (&le;3)</option>
                  <option value="Out of Stock">Out of Stock (0)</option>
                </select>
                <button
                  onClick={() => {
                    setSelectedCategory('All');
                    setSelectedStatus('All');
                    setSelectedStock('All');
                    setSearchQuery('');
                  }}
                  className="px-2.5 py-2 text-xs font-semibold text-rose-600 hover:bg-rose-50 rounded-lg transition"
                >
                  Reset
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ======================================================== */}
      {/* 4. PRODUCTS DATA DISPLAY (TABLE VIEW)                    */}
      {/* ======================================================== */}
      {viewMode === 'table' ? (
        <div className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-600 border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase text-[10px] tracking-wider">
                  <th className="py-3 px-4">Product</th>
                  <th className="py-3 px-3">SKU</th>
                  <th className="py-3 px-3">Category</th>
                  <th className="py-3 px-3">Cost (ETB)</th>
                  <th className="py-3 px-3">Selling Price</th>
                  <th className="py-3 px-3">Stock</th>
                  <th className="py-3 px-3">Status</th>
                  <th className="py-3 px-3">Updated</th>
                  <th className="py-3 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {filteredProducts.length === 0 ? (
                  <tr>
                    <td colSpan="9" className="text-center py-12 text-slate-400">
                      <Package className="w-8 h-8 mx-auto mb-2 text-slate-300" />
                      <p className="font-semibold text-slate-600 text-sm">No products match current filters</p>
                      <p className="text-xs text-slate-400 mt-1">Try modifying search keyword or reset applied filters.</p>
                      <button
                        onClick={() => { setSearchQuery(''); setSelectedCategory('All'); setSelectedStatus('All'); setSelectedStock('All'); }}
                        className="mt-3 px-3 py-1.5 text-xs bg-[#0A2E5D] text-white rounded-lg font-bold"
                      >
                        Clear Filters
                      </button>
                    </td>
                  </tr>
                ) : (
                  filteredProducts.map(p => (
                    <tr
                      key={p.id}
                      onClick={() => setSelectedProductForDetail(p)}
                      className="hover:bg-slate-50/80 transition-colors cursor-pointer group"
                    >
                      {/* Product Thumbnail + Title */}
                      <td className="py-3 px-4">
                        <div className="flex items-center gap-3">
                          <img
                            src={p.image}
                            alt={p.name}
                            className="w-10 h-10 rounded-lg object-cover border border-slate-200 flex-shrink-0"
                            onError={(e) => { e.target.src = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=150&q=80"; }}
                          />
                          <div>
                            <div className="font-bold text-[#0A2E5D] text-xs group-hover:text-blue-700 transition">
                              {p.name}
                            </div>
                            <div className="text-[11px] text-slate-400 truncate max-w-xs">
                              {p.material} • {p.dimensions}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* SKU */}
                      <td className="py-3 px-3 font-mono font-semibold text-[11px] text-slate-700">
                        {p.sku}
                      </td>

                      {/* Category */}
                      <td className="py-3 px-3">
                        <span className="inline-block px-2 py-0.5 rounded text-[10px] font-bold bg-slate-100 text-slate-700 border border-slate-200">
                          {p.category}
                        </span>
                      </td>

                      {/* Cost Price */}
                      <td className="py-3 px-3 font-medium text-slate-500">
                        {p.costPrice.toLocaleString('en-US')}
                      </td>

                      {/* Selling Price */}
                      <td className="py-3 px-3">
                        <div className="font-bold text-slate-900 text-xs">
                          {p.retailPrice.toLocaleString('en-US')} ETB
                        </div>
                        <div className="text-[10px] text-emerald-600 font-medium">
                          Min: {p.minSellingPrice.toLocaleString('en-US')}
                        </div>
                      </td>

                      {/* Stock with Warning Indicators */}
                      <td className="py-3 px-3">
                        <div className="flex items-center gap-1.5 font-bold">
                          {p.stock === 0 ? (
                            <span className="text-rose-600 font-extrabold flex items-center gap-1">
                              <XCircle className="w-3.5 h-3.5" /> 0 Units
                            </span>
                          ) : p.stock <= p.minStock ? (
                            <span className="text-amber-600 font-extrabold flex items-center gap-1">
                              <AlertTriangle className="w-3.5 h-3.5" /> {p.stock} Units
                            </span>
                          ) : (
                            <span className="text-emerald-700 flex items-center gap-1">
                              <CheckCircle className="w-3.5 h-3.5" /> {p.stock} Units
                            </span>
                          )}
                        </div>
                        <div className="text-[10px] text-slate-400">
                          {p.warehouse.split(' ')[0]}
                        </div>
                      </td>

                      {/* Status Badge */}
                      <td className="py-3 px-3">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          p.status === 'Active'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}>
                          {p.status}
                        </span>
                      </td>

                      {/* Last Updated */}
                      <td className="py-3 px-3 text-slate-400 text-[11px]">
                        {p.updatedAt}
                      </td>

                      {/* Actions */}
                      <td className="py-3 px-4 text-right">
                        <div className="inline-flex items-center gap-1" onClick={(e) => e.stopPropagation()}>
                          <button
                            onClick={() => setSelectedProductForDetail(p)}
                            className="p-1.5 rounded-md hover:bg-slate-100 text-slate-500 hover:text-[#0A2E5D] transition"
                            title="View Details"
                          >
                            <Eye className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={(e) => handleOpenEdit(p, e)}
                            className="p-1.5 rounded-md hover:bg-slate-100 text-slate-500 hover:text-amber-600 transition"
                            title="Edit Product"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={(e) => handleDeleteProduct(p.id, e)}
                            className="p-1.5 rounded-md hover:bg-slate-100 text-slate-500 hover:text-rose-600 transition"
                            title="Delete"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* ======================================================== */
        /* 5. PRODUCTS DATA DISPLAY (GRID VIEW)                     */
        /* ======================================================== */
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {filteredProducts.map(p => (
            <div
              key={p.id}
              onClick={() => setSelectedProductForDetail(p)}
              className="bg-white rounded-xl border border-slate-200/80 shadow-sm overflow-hidden hover:shadow-md transition cursor-pointer flex flex-col group"
            >
              {/* Product Image Cover */}
              <div className="relative aspect-video bg-slate-100 overflow-hidden">
                <img
                  src={p.image}
                  alt={p.name}
                  className="w-full h-full object-cover group-hover:scale-105 transition duration-300"
                />
                <div className="absolute top-2 right-2">
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold shadow-sm ${
                    p.status === 'Active'
                      ? 'bg-white/90 text-emerald-700'
                      : 'bg-white/90 text-rose-700'
                  }`}>
                    {p.status}
                  </span>
                </div>
                <div className="absolute bottom-2 left-2">
                  <span className="px-2 py-0.5 rounded bg-[#0A2E5D]/80 backdrop-blur-xs text-[#D4AF37] text-[10px] font-mono font-bold">
                    {p.sku}
                  </span>
                </div>
              </div>

              {/* Card Body */}
              <div className="p-4 flex-1 flex flex-col justify-between">
                <div>
                  <div className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                    {p.category}
                  </div>
                  <h3 className="font-bold text-[#0A2E5D] text-sm mt-0.5 line-clamp-1 group-hover:text-blue-700 transition">
                    {p.name}
                  </h3>
                  <p className="text-xs text-slate-500 mt-1 line-clamp-2">
                    {p.description}
                  </p>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-100">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Retail Price</div>
                      <div className="text-base font-black text-[#0A2E5D]">
                        {p.retailPrice.toLocaleString('en-US')} <span className="text-xs font-semibold text-slate-500">ETB</span>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Stock</div>
                      <div className={`text-xs font-bold ${
                        p.stock === 0 ? 'text-rose-600' : p.stock <= p.minStock ? 'text-amber-600' : 'text-emerald-700'
                      }`}>
                        {p.stock} Units
                      </div>
                    </div>
                  </div>

                  {/* Card Actions */}
                  <div className="flex items-center justify-end gap-1 mt-3" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => setSelectedProductForDetail(p)}
                      className="px-2.5 py-1 text-xs bg-slate-100 hover:bg-[#0A2E5D] hover:text-white text-slate-700 font-semibold rounded-md transition"
                    >
                      View
                    </button>
                    <button
                      onClick={(e) => handleOpenEdit(p, e)}
                      className="px-2.5 py-1 text-xs bg-[#D4AF37]/20 text-[#0A2E5D] hover:bg-[#D4AF37] font-semibold rounded-md transition"
                    >
                      Edit
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ======================================================== */}
      {/* 6. PRODUCT DETAIL SLIDEOUT / MODAL (ILILI ERP Spec)     */}
      {/* ======================================================== */}
      {selectedProductForDetail && (
        <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-xs flex justify-end">
          <div className="w-full max-w-xl bg-white h-full shadow-2xl overflow-y-auto flex flex-col">
            {/* Drawer Header */}
            <div className="p-5 border-b border-slate-200 flex items-center justify-between sticky top-0 bg-white z-10">
              <div className="flex items-center gap-2">
                <span className="p-2 rounded-lg bg-[#0A2E5D]/10 text-[#0A2E5D]">
                  <Package className="w-5 h-5 text-[#D4AF37]" />
                </span>
                <div>
                  <h2 className="text-base font-extrabold text-[#0A2E5D]">
                    {selectedProductForDetail.name}
                  </h2>
                  <p className="text-xs text-slate-400 font-mono">
                    SKU: {selectedProductForDetail.sku} • Barcode: {selectedProductForDetail.barcode}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setSelectedProductForDetail(null)}
                className="p-2 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Drawer Content */}
            <div className="p-6 space-y-6 flex-1">
              {/* Product Hero Banner */}
              <div className="aspect-video w-full rounded-xl overflow-hidden bg-slate-100 border border-slate-200 relative">
                <img
                  src={selectedProductForDetail.image}
                  alt={selectedProductForDetail.name}
                  className="w-full h-full object-cover"
                />
                <div className="absolute top-3 left-3 bg-[#0A2E5D]/90 text-[#D4AF37] px-2.5 py-1 rounded text-xs font-bold">
                  {selectedProductForDetail.category}
                </div>
              </div>

              {/* 1. Multi-Tier Pricing Architecture */}
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5">
                  Multi-Tier Pricing Matrix (ETB)
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-200">
                    <div className="text-[10px] text-slate-400 font-bold uppercase">Retail (Customer)</div>
                    <div className="text-sm font-black text-emerald-700 mt-0.5">
                      {selectedProductForDetail.retailPrice.toLocaleString()} ETB
                    </div>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-200">
                    <div className="text-[10px] text-slate-400 font-bold uppercase">Reseller Rate</div>
                    <div className="text-sm font-black text-blue-700 mt-0.5">
                      {selectedProductForDetail.resellerPrice.toLocaleString()} ETB
                    </div>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-200">
                    <div className="text-[10px] text-slate-400 font-bold uppercase">Wholesale B2B</div>
                    <div className="text-sm font-black text-[#D4AF37] mt-0.5">
                      {selectedProductForDetail.wholesalePrice.toLocaleString()} ETB
                    </div>
                  </div>
                  <div className="bg-slate-50 p-3 rounded-lg border border-slate-200">
                    <div className="text-[10px] text-slate-400 font-bold uppercase">Factory Cost</div>
                    <div className="text-sm font-black text-slate-700 mt-0.5">
                      {selectedProductForDetail.costPrice.toLocaleString()} ETB
                    </div>
                  </div>
                </div>
              </div>

              {/* 2. Inventory & Stock Status */}
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5">
                  Inventory & Warehouse Control
                </h4>
                <div className="bg-white border border-slate-200 rounded-lg p-3.5 space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Available Stock:</span>
                    <span className="font-bold text-[#0A2E5D]">{selectedProductForDetail.stock} Units</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Reorder Level Threshold:</span>
                    <span className="font-semibold text-slate-700">{selectedProductForDetail.minStock} Units</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Assigned Facility:</span>
                    <span className="font-semibold text-slate-700">{selectedProductForDetail.warehouse}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Stock Status:</span>
                    <span className="font-bold text-emerald-600">{selectedProductForDetail.status}</span>
                  </div>
                </div>
              </div>

              {/* 3. Product Specifications & Customization */}
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5">
                  Specifications & Materials
                </h4>
                <div className="bg-white border border-slate-200 rounded-lg p-3.5 space-y-2 text-xs">
                  <div className="flex justify-between">
                    <span className="text-slate-500">Wood / Carcass Material:</span>
                    <span className="font-semibold text-slate-800">{selectedProductForDetail.material}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Standard Dimensions:</span>
                    <span className="font-semibold text-slate-800">{selectedProductForDetail.dimensions}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-slate-500">Warranty Coverage:</span>
                    <span className="font-semibold text-slate-800">{selectedProductForDetail.warranty}</span>
                  </div>
                </div>
              </div>

              {/* 4. Product Variants */}
              {selectedProductForDetail.variants && selectedProductForDetail.variants.length > 0 && (
                <div>
                  <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-2.5">
                    Available Customization Variants
                  </h4>
                  <div className="border border-slate-200 rounded-lg overflow-hidden">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-50 text-slate-500 text-[10px] uppercase font-bold border-b border-slate-200">
                        <tr>
                          <th className="p-2.5">Variant Size</th>
                          <th className="p-2.5">Color / Finish</th>
                          <th className="p-2.5">Fabric</th>
                          <th className="p-2.5 text-right">Price</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {selectedProductForDetail.variants.map((v, idx) => (
                          <tr key={idx} className="hover:bg-slate-50">
                            <td className="p-2.5 font-semibold text-slate-800">{v.size}</td>
                            <td className="p-2.5 text-slate-600">{v.color}</td>
                            <td className="p-2.5 text-slate-600">{v.fabric}</td>
                            <td className="p-2.5 text-right font-bold text-[#0A2E5D]">
                              {v.price.toLocaleString()} ETB
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {/* 5. Workshop Description */}
              <div>
                <h4 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  Showroom & Workshop Craft Notes
                </h4>
                <p className="text-xs text-slate-600 leading-relaxed bg-slate-50 p-3 rounded-lg border border-slate-200">
                  {selectedProductForDetail.description}
                </p>
              </div>
            </div>

            {/* Drawer Actions */}
            <div className="p-4 border-t border-slate-200 bg-slate-50 flex items-center justify-between">
              <button
                onClick={(e) => handleDeleteProduct(selectedProductForDetail.id, e)}
                className="px-3 py-2 text-xs font-bold text-rose-600 hover:bg-rose-100 rounded-lg transition"
              >
                Delete Product
              </button>
              <div className="flex items-center gap-2">
                <button
                  onClick={(e) => {
                    handleOpenEdit(selectedProductForDetail, e);
                    setSelectedProductForDetail(null);
                  }}
                  className="px-4 py-2 text-xs font-bold bg-[#D4AF37] text-[#0A2E5D] hover:bg-amber-400 rounded-lg transition"
                >
                  Edit Information
                </button>
                <button
                  onClick={() => setSelectedProductForDetail(null)}
                  className="px-4 py-2 text-xs font-bold bg-[#0A2E5D] text-white hover:bg-[#071f3f] rounded-lg transition"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ======================================================== */}
      {/* 7. ADD / EDIT PRODUCT MODAL (ILILI ERP Workflow)         */}
      {/* ======================================================== */}
      {isAddProductOpen && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-2xl border border-slate-200 w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden">
            {/* Modal Header */}
            <div className="p-5 border-b border-slate-200 flex items-center justify-between bg-white">
              <div className="flex items-center gap-2">
                <span className="p-2 rounded-lg bg-[#0A2E5D]/10 text-[#0A2E5D]">
                  <Plus className="w-5 h-5 text-[#D4AF37]" />
                </span>
                <div>
                  <h3 className="text-base font-extrabold text-[#0A2E5D]">
                    {productToEdit ? "Edit Product Details" : "+ Add New Product"}
                  </h3>
                  <p className="text-xs text-slate-400">
                    Registers item directly in BEKANSI database and updates AI Sales Tools.
                  </p>
                </div>
              </div>
              <button
                onClick={() => setIsAddProductOpen(false)}
                className="p-1.5 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleSaveProduct} className="p-6 overflow-y-auto space-y-5 flex-1 text-xs">
              {/* Section 1: Basic Information */}
              <div>
                <h4 className="font-bold text-[#0A2E5D] uppercase tracking-wider text-[11px] mb-3">
                  1. Basic Information
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div className="sm:col-span-2">
                    <label className="block text-slate-600 font-semibold mb-1">Product Name *</label>
                    <input
                      type="text"
                      required
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      placeholder="e.g. Modern King Bed 1.80m × 2.00m"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">SKU *</label>
                    <input
                      type="text"
                      required
                      value={formData.sku}
                      onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                      placeholder="BK-BED-180-001"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 font-mono focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Category *</label>
                    <select
                      value={formData.category}
                      onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    >
                      {CATEGORIES.filter(c => c !== 'All').map(c => (
                        <option key={c} value={c}>{c}</option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>

              {/* Section 2: Pricing Matrix */}
              <div>
                <h4 className="font-bold text-[#0A2E5D] uppercase tracking-wider text-[11px] mb-3">
                  2. Pricing Architecture (ETB)
                </h4>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Retail Price *</label>
                    <input
                      type="number"
                      required
                      value={formData.retailPrice}
                      onChange={(e) => setFormData({ ...formData, retailPrice: e.target.value })}
                      placeholder="90000"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-emerald-700 font-bold focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Reseller Price</label>
                    <input
                      type="number"
                      value={formData.resellerPrice}
                      onChange={(e) => setFormData({ ...formData, resellerPrice: e.target.value })}
                      placeholder="78000"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Wholesale (B2B)</label>
                    <input
                      type="number"
                      value={formData.wholesalePrice}
                      onChange={(e) => setFormData({ ...formData, wholesalePrice: e.target.value })}
                      placeholder="72000"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Factory Cost</label>
                    <input
                      type="number"
                      value={formData.costPrice}
                      onChange={(e) => setFormData({ ...formData, costPrice: e.target.value })}
                      placeholder="48000"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                </div>
              </div>

              {/* Section 3: Inventory & Warehouse */}
              <div>
                <h4 className="font-bold text-[#0A2E5D] uppercase tracking-wider text-[11px] mb-3">
                  3. Inventory & Specifications
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Current Stock Qty</label>
                    <input
                      type="number"
                      value={formData.stock}
                      onChange={(e) => setFormData({ ...formData, stock: e.target.value })}
                      placeholder="5"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Wood / Hardwood</label>
                    <input
                      type="text"
                      value={formData.material}
                      onChange={(e) => setFormData({ ...formData, material: e.target.value })}
                      placeholder="Wanza, Mahogany, Grar"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Dimensions</label>
                    <input
                      type="text"
                      value={formData.dimensions}
                      onChange={(e) => setFormData({ ...formData, dimensions: e.target.value })}
                      placeholder="1.80m × 2.00m"
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                </div>
              </div>

              {/* Section 4: Image & Description */}
              <div>
                <h4 className="font-bold text-[#0A2E5D] uppercase tracking-wider text-[11px] mb-3">
                  4. Media & Craftsmanship Notes
                </h4>
                <div className="space-y-3">
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Image URL / CDN</label>
                    <input
                      type="text"
                      value={formData.image}
                      onChange={(e) => setFormData({ ...formData, image: e.target.value })}
                      placeholder="https://..."
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    />
                  </div>
                  <div>
                    <label className="block text-slate-600 font-semibold mb-1">Product Description</label>
                    <textarea
                      rows="3"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                      placeholder="Handcrafted master bed with integrated warm LED backlighting and Turkish velvet upholstered headboard."
                      className="w-full p-2 bg-slate-50 border border-slate-200 rounded-lg text-slate-800 focus:outline-none focus:ring-1 focus:ring-[#0A2E5D]"
                    ></textarea>
                  </div>
                </div>
              </div>

              {/* Form Buttons */}
              <div className="pt-4 border-t border-slate-200 flex items-center justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => setIsAddProductOpen(false)}
                  className="px-4 py-2 rounded-lg border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-lg bg-[#0A2E5D] text-white font-bold hover:bg-[#071f3f] transition shadow-sm"
                >
                  {productToEdit ? "Save Changes" : "Save to Database"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
