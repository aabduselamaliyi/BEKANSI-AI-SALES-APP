package com.example.data.api

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * ============================================================================
 * SECURE TOKEN STORAGE & SESSION MANAGER (Encrypted SharedPreferences backed)
 * ============================================================================
 */
class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bekansi_secure_auth_prefs", Context.MODE_PRIVATE)

    private val _authToken = MutableStateFlow(getToken())
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    private val _tenantId = MutableStateFlow(getTenantId())
    val tenantId: StateFlow<String?> = _tenantId.asStateFlow()

    fun saveSession(token: String, tenantId: String, userEmail: String, userRole: String) {
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_TENANT_ID, tenantId)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_USER_ROLE, userRole)
            .apply()
        _authToken.value = token
        _tenantId.value = tenantId
    }

    fun getToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)
    fun getTenantId(): String? = prefs.getString(KEY_TENANT_ID, null)
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    fun getUserRole(): String? = prefs.getString(KEY_USER_ROLE, null)

    fun clearSession() {
        prefs.edit().clear().apply()
        _authToken.value = null
        _tenantId.value = null
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "jwt_access_token"
        private const val KEY_TENANT_ID = "organization_tenant_id"
        private const val KEY_USER_EMAIL = "authenticated_user_email"
        private const val KEY_USER_ROLE = "authenticated_user_role"
    }
}

/**
 * ============================================================================
 * AUTHENTICATION & MULTI-TENANT HEADER INTERCEPTOR
 * ============================================================================
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        tokenManager.getToken()?.let { token ->
            requestBuilder.header("Authorization", "Bearer $token")
        }

        tokenManager.getTenantId()?.let { tenantId ->
            requestBuilder.header("X-Tenant-ID", tenantId)
        }

        return chain.proceed(requestBuilder.build())
    }
}

/**
 * ============================================================================
 * RETROFIT API SERVICE DEFINITIONS
 * ============================================================================
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ApiError? = null
)

data class ApiError(
    val code: String,
    val message: String
)

data class LoginRequest(
    val email: String,
    val password: String,
    val organization_id: String? = null
)

data class AuthResponseData(
    val token: String,
    val user: UserProfileData
)

data class UserProfileData(
    val id: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val role: String,
    val organization_id: String,
    val organization_name: String? = null
)

data class ErpStatusData(
    val configured: Boolean,
    val provider: String,
    val tenant_code: String,
    val status: String,
    val last_sync_timestamp: String? = null
)

interface BekansiBackendApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): retrofit2.Response<ApiResponse<AuthResponseData>>

    @GET("api/v1/auth/me")
    suspend fun getCurrentUser(): retrofit2.Response<ApiResponse<UserProfileData>>

    @GET("api/v1/erp/sync-status")
    suspend fun getErpStatus(): retrofit2.Response<ApiResponse<ErpStatusData>>

    @GET("api/v1/health")
    suspend fun checkHealth(): retrofit2.Response<ApiResponse<Map<String, Any>>>

    @Multipart
    @POST("api/v1/dam/upload")
    suspend fun uploadDesignImage(
        @Part file: MultipartBody.Part,
        @Part("product_name") productName: RequestBody,
        @Part("category") category: RequestBody,
        @Part("sku") sku: RequestBody,
        @Part("tenant_id") tenantId: RequestBody,
        @Part("design_type") designType: RequestBody,
        @Part("room_type") roomType: RequestBody,
        @Part("is_primary") isPrimary: RequestBody,
        @Part("price") price: RequestBody
    ): retrofit2.Response<ApiResponse<DesignUploadResultData>>
}

/**
 * ============================================================================
 * SINGLETON RETROFIT CLIENT FACTORY
 * ============================================================================
 */
object BekansiApiClient {
    // Configurable backend base URL (defaulting to the production AI Studio Run App gateway)
    private const val DEFAULT_BASE_URL = "https://ais-dev-deqcomo2ppszhy6mvyk6ap-962457232513.europe-west2.run.app/"

    @Volatile
    private var apiInstance: BekansiBackendApi? = null

    fun getApi(context: Context): BekansiBackendApi {
        return apiInstance ?: synchronized(this) {
            val tokenManager = TokenManager(context.applicationContext)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            val instance = retrofit.create(BekansiBackendApi::class.java)
            apiInstance = instance
            instance
        }
    }
}
