package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LanguageConfigDao {
    @Query("SELECT * FROM language_configs")
    fun getAllLanguages(): Flow<List<LanguageConfig>>

    @Query("SELECT * FROM language_configs WHERE isEnabled = 1")
    fun getEnabledLanguages(): Flow<List<LanguageConfig>>

    @Query("SELECT * FROM language_configs WHERE code = :code")
    suspend fun getLanguageByCode(code: String): LanguageConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguage(config: LanguageConfig): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanguages(configs: List<LanguageConfig>)
}


@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY createdAt DESC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :id")
    suspend fun getLeadById(id: Int): Lead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Query("DELETE FROM leads WHERE id = :id")
    suspend fun deleteLeadById(id: Int)

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}

@Dao
interface QuotationDao {
    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    fun getAllQuotations(): Flow<List<Quotation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: Quotation): Long

    @Query("DELETE FROM quotations WHERE id = :id")
    suspend fun deleteQuotationById(id: Int)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE channel = :channel ORDER BY timestamp ASC")
    fun getMessagesByChannel(channel: String): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Conversation): Long

    @Query("DELETE FROM conversations WHERE channel = :channel")
    suspend fun clearChannelMessages(channel: String)
}

@Dao
interface AlbumCategoryDao {
    @Query("SELECT * FROM album_categories")
    fun getAllCategories(): Flow<List<AlbumCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<AlbumCategory>)
}

@Dao
interface ProductAlbumDao {
    @Query("SELECT * FROM product_albums ORDER BY id ASC")
    fun getAllAlbums(): Flow<List<ProductAlbum>>

    @Query("SELECT * FROM product_albums WHERE category = :category ORDER BY id ASC")
    fun getAlbumsByCategory(category: String): Flow<List<ProductAlbum>>

    @Query("SELECT * FROM product_albums WHERE id = :id")
    suspend fun getAlbumById(id: String): ProductAlbum?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<ProductAlbum>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: ProductAlbum)

    @Query("DELETE FROM product_albums WHERE id = :id")
    suspend fun deleteAlbumById(id: String)
}

@Dao
interface CustomerFavoriteDao {
    @Query("SELECT * FROM customer_favorites WHERE customerPhone = :customerPhone")
    fun getFavoritesByCustomer(customerPhone: String): Flow<List<CustomerFavorite>>

    @Query("SELECT * FROM customer_favorites")
    fun getAllFavorites(): Flow<List<CustomerFavorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: CustomerFavorite): Long

    @Query("DELETE FROM customer_favorites WHERE customerPhone = :customerPhone AND albumId = :albumId")
    suspend fun removeFavorite(customerPhone: String, albumId: String)
}

@Dao
interface CustomerSelectionDao {
    @Query("SELECT * FROM customer_selections ORDER BY createdAt DESC")
    fun getAllSelections(): Flow<List<CustomerSelection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelection(selection: CustomerSelection): Long
}

@Dao
interface DesignComparisonDao {
    @Query("SELECT * FROM design_comparisons WHERE customerPhone = :customerPhone")
    fun getComparisonsByCustomer(customerPhone: String): Flow<List<DesignComparison>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComparison(comparison: DesignComparison): Long

    @Query("DELETE FROM design_comparisons WHERE id = :id")
    suspend fun deleteComparisonById(id: Int)
}

@Dao
interface AlbumAnalyticsDao {
    @Query("SELECT * FROM album_analytics")
    fun getAllAnalytics(): Flow<List<AlbumAnalytics>>

    @Query("SELECT * FROM album_analytics WHERE albumId = :albumId")
    suspend fun getAnalyticsForAlbum(albumId: String): AlbumAnalytics?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AlbumAnalytics)

    @Query("UPDATE album_analytics SET viewCount = viewCount + 1 WHERE albumId = :albumId")
    suspend fun incrementViewCount(albumId: String)

    @Query("UPDATE album_analytics SET selectionCount = selectionCount + 1 WHERE albumId = :albumId")
    suspend fun incrementSelectionCount(albumId: String)

    @Query("UPDATE album_analytics SET favoriteCount = favoriteCount + 1 WHERE albumId = :albumId")
    suspend fun incrementFavoriteCount(albumId: String)
}

@Dao
interface CustomerProfileDao {
    @Query("SELECT * FROM customer_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<CustomerProfile>>

    @Query("SELECT * FROM customer_profiles WHERE phone = :phone")
    suspend fun getProfileByPhone(phone: String): CustomerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: CustomerProfile): Long
}

@Dao
interface OrderRecordDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderRecord>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): OrderRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderRecord): Long

    @Query("UPDATE orders SET orderStage = :newStage WHERE id = :id")
    suspend fun updateOrderStage(id: Int, newStage: String)

    @Query("UPDATE orders SET depositPaid = :deposit, remainingBalance = :remaining, paymentStatus = :status WHERE id = :id")
    suspend fun updateOrderPayment(id: Int, deposit: Double, remaining: Double, status: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrderById(id: Int)
}

@Dao
interface WarehouseItemDao {
    @Query("SELECT * FROM warehouse_items ORDER BY id ASC")
    fun getAllWarehouseItems(): Flow<List<WarehouseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseItem(item: WarehouseItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouseItems(items: List<WarehouseItem>)

    @Query("UPDATE warehouse_items SET quantity = :newQty WHERE id = :id")
    suspend fun updateQuantity(id: Int, newQty: Int)

    @Query("DELETE FROM warehouse_items WHERE id = :id")
    suspend fun deleteWarehouseItem(id: Int)
}

@Dao
interface DeliveryRecordDao {
    @Query("SELECT * FROM delivery_records ORDER BY timestamp DESC")
    fun getAllDeliveries(): Flow<List<DeliveryRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: DeliveryRecord): Long

    @Query("UPDATE delivery_records SET status = :status, isSigned = :isSigned WHERE id = :id")
    suspend fun updateDeliveryStatus(id: Int, status: String, isSigned: Boolean)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLog): Long
}

@Dao
interface MediaAssetDao {
    @Query("SELECT * FROM media_assets ORDER BY timestamp DESC")
    fun getAllMediaAssets(): Flow<List<MediaAsset>>

    @Query("SELECT * FROM media_assets WHERE id = :id")
    suspend fun getMediaAssetById(id: String): MediaAsset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaAsset(asset: MediaAsset)

    @Query("UPDATE media_assets SET progress = :progress, status = :status, downloadUrl = :downloadUrl WHERE id = :id")
    suspend fun updateUploadProgress(id: String, progress: Int, status: String, downloadUrl: String)

    @Query("DELETE FROM media_assets WHERE id = :id")
    suspend fun deleteMediaAssetById(id: String)
}

@Database(
    entities = [
        Lead::class, 
        Product::class, 
        Quotation::class, 
        Conversation::class, 
        LanguageConfig::class,
        AlbumCategory::class,
        ProductAlbum::class,
        CustomerFavorite::class,
        CustomerSelection::class,
        DesignComparison::class,
        AlbumAnalytics::class,
        CustomerProfile::class,
        OrderRecord::class,
        WarehouseItem::class,
        DeliveryRecord::class,
        AuditLog::class,
        MediaAsset::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun leadDao(): LeadDao
    abstract fun productDao(): ProductDao
    abstract fun quotationDao(): QuotationDao
    abstract fun conversationDao(): ConversationDao
    abstract fun languageConfigDao(): LanguageConfigDao
    abstract fun albumCategoryDao(): AlbumCategoryDao
    abstract fun productAlbumDao(): ProductAlbumDao
    abstract fun customerFavoriteDao(): CustomerFavoriteDao
    abstract fun customerSelectionDao(): CustomerSelectionDao
    abstract fun designComparisonDao(): DesignComparisonDao
    abstract fun albumAnalyticsDao(): AlbumAnalyticsDao
    abstract fun customerProfileDao(): CustomerProfileDao
    abstract fun orderRecordDao(): OrderRecordDao
    abstract fun warehouseItemDao(): WarehouseItemDao
    abstract fun deliveryRecordDao(): DeliveryRecordDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun mediaAssetDao(): MediaAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bekansi_sales_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

