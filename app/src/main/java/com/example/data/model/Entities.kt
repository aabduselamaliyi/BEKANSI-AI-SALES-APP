package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String,
    val status: String, // New, Contacted, Quoted, Won, Lost
    val source: String, // WhatsApp, Facebook, Telegram, LiveChat
    val requirements: String,
    val notes: String,
    val language: String, // English, Amharic, Afaan Oromo
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Sofa, Dining Table, Bed, Coffee Table, Office
    val price: Double,
    val material: String, // Wanza, Mahogany, Grar, MDF
    val dimensions: String,
    val warranty: String,
    val description: String,
    val stockStatus: String, // In Stock, Made to Order, Out of Stock
    val imageUrl: String = ""
) : Serializable

@Entity(tableName = "quotations")
data class Quotation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int,
    val leadName: String,
    val productName: String,
    val dimensions: String,
    val material: String,
    val laborCost: Double,
    val transportCost: Double,
    val materialCost: Double,
    val subtotal: Double,
    val vat: Double, // 15% VAT
    val margin: Double, // Profit margin visualizer (e.g., 20%)
    val total: Double,
    val deliveryTimeEstimate: String,
    val terms: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int = 0,
    val channel: String, // WhatsApp, Facebook, Telegram, LiveChat
    val sender: String, // Customer, AI, Human Agent
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "language_configs")
data class LanguageConfig(
    @PrimaryKey val code: String, // "en", "am", "om"
    val name: String,             // "English", "Amharic", "Afaan Oromo"
    val isEnabled: Boolean = true,
    val systemPromptOverride: String = "", // Custom knowledge base instructions for this language
    val customGreeting: String = "",       // Custom greeting response
    val customFallback: String = ""        // Custom fallback when answer is unavailable
) : Serializable

@Entity(tableName = "album_categories")
data class AlbumCategory(
    @PrimaryKey val id: String, // e.g. "bedroom_sets"
    val name: String,
    val nameAm: String,
    val nameOm: String,
    val description: String
) : Serializable

@Entity(tableName = "product_albums")
data class ProductAlbum(
    @PrimaryKey val id: String, // e.g. "BS-001"
    val name: String,
    val category: String, // Bedroom Sets, Wardrobes, Kitchen Cabinets, TV Stands, Dining Sets, Office Furniture, Sofas, Coffee Tables, Reception Furniture, Custom Furniture Designs
    val designStyle: String, // e.g. "Modern", "Classic", "Contemporary", "Scandinavian"
    val descriptionAm: String,
    val descriptionOm: String,
    val descriptionEn: String,
    val dimensions: String,
    val materialOptions: String,
    val colorOptions: String,
    val estimatedProductionTime: String,
    val priceRangeLower: Double,
    val priceRangeUpper: Double,
    val popularityScore: Int,
    val tags: String,
    val imageUrls: String // Comma separated URLs
) : Serializable

@Entity(tableName = "customer_favorites")
data class CustomerFavorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val albumId: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "customer_selections")
data class CustomerSelection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leadId: Int,
    val albumId: String,
    val requirements: String = "",
    val budget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "design_comparisons")
data class DesignComparison(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerPhone: String,
    val albumId1: String,
    val albumId2: String,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "album_analytics")
data class AlbumAnalytics(
    @PrimaryKey val albumId: String,
    val viewCount: Int = 0,
    val selectionCount: Int = 0,
    val favoriteCount: Int = 0
) : Serializable

@Entity(tableName = "customer_profiles")
data class CustomerProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val location: String = "Addis Ababa",
    val loyaltyPoints: Int = 120,
    val csatScore: Float = 4.8f,
    val preferences: String = "Modern Wanza & Mahogany",
    val referralCode: String = "BEKANSI-2026",
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "orders")
data class OrderRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val quotationId: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val productName: String,
    val totalAmount: Double,
    val depositPaid: Double,
    val remainingBalance: Double,
    val paymentStatus: String = "Deposit Paid", // Unpaid, Deposit Paid, Fully Paid
    val orderStage: String = "Production", // Inquiry, Quotation, Negotiation, Approval, Payment, Production, Quality Check, Dispatch, Delivery, Installation, Warranty
    val warehouseLocation: String = "Addis Ababa Central",
    val deliveryAddress: String = "Bole, Addis Ababa",
    val driverName: String = "Driver Abebe",
    val estimatedDeliveryDate: String = "3-4 Weeks",
    val signatureCaptured: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "warehouse_items")
data class WarehouseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val barcode: String,
    val productName: String,
    val warehouseName: String, // Addis Ababa Central, Hawassa Branch, Kaliti Workshop
    val quantity: Int,
    val reorderPoint: Int = 5,
    val unitCost: Double,
    val supplierName: String = "Bekansi Wood Mill",
    val lastRestocked: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "delivery_records")
data class DeliveryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val driverName: String,
    val vehiclePlate: String = "ET-3-89123",
    val status: String = "In Transit", // Scheduled, In Transit, Delivered, Installed
    val currentGpsLat: Double = 9.0192,
    val currentGpsLng: Double = 38.7525,
    val deliveryNotes: String = "Handle Wanza furniture with velvet wraps.",
    val isSigned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userRole: String, // Super Admin, Sales Manager, Interior Designer, Logistics Manager
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "media_assets")
data class MediaAsset(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val mediaType: String,
    val fileUri: String,
    val downloadUrl: String = "",
    val status: String = "Uploading", // Uploading, Approved, Draft, Failed
    val progress: Int = 0,
    val fileSize: String = "14.2 MB",
    val price: String = "N/A",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable



