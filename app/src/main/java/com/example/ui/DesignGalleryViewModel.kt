package com.example.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.ImageUploadService
import com.example.data.api.UploadProgressState
import com.example.data.model.Product
import com.example.data.model.ProductDesignImage
import com.example.data.repository.SalesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Model representing an image selected from the device Photo Gallery (PickVisualMedia)
 * or local storage / internal storage documents.
 */
data class SelectedImageMetadata(
    val uri: Uri,
    val fileName: String,
    val mimeType: String,
    val fileSizeFormatted: String,
    val isPrimary: Boolean = false,
    val sortOrder: Int = 0
)

/**
 * UI State for Design Gallery Management
 */
data class DesignGalleryUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadStatusMessage: String = "",
    val uploadBytesTransferred: Long = 0L,
    val uploadTotalBytes: Long = 0L,
    val currentUploadingFile: String = "",
    val currentFileIndex: Int = 0,
    val totalFilesToUpload: Int = 0,
    val error: String? = null,
    val selectedImageUris: List<SelectedImageMetadata> = emptyList(),
    val primaryImageIndex: Int = 0,
    val activePreviewIndex: Int = 0,
    val showUploadDialog: Boolean = false,
    val showAddSourceDialog: Boolean = false,
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val filterOnlyFavorites: Boolean = false,
    val filterOnlyPrimary: Boolean = false,
    val sortOption: String = "Newest", // "Newest", "Price Low-High", "Price High-Low", "Category"
    val isGridView: Boolean = true
)

/**
 * ViewModel managing image selection via ActivityResultContracts.PickVisualMedia
 * and local storage document pickers, along with Retrofit-based DAM image upload monitoring.
 */
class DesignGalleryViewModel(
    private val repository: SalesRepository,
    private val imageUploadService: ImageUploadService? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DesignGalleryUiState())
    val uiState: StateFlow<DesignGalleryUiState> = _uiState.asStateFlow()

    private val _uploadProgressState = MutableStateFlow<UploadProgressState>(UploadProgressState.Idle)
    val uploadProgressState: StateFlow<UploadProgressState> = _uploadProgressState.asStateFlow()

    val allDesigns: StateFlow<List<ProductDesignImage>> = repository.allDesignImages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Filtered list of furniture designs combining search, category, favorites, and sort
     */
    val filteredDesigns: StateFlow<List<ProductDesignImage>> = combine(
        allDesigns,
        _uiState
    ) { designs, state ->
        designs.filter { item ->
            val matchesCategory = (state.selectedCategory == "All") || 
                item.category.equals(state.selectedCategory, ignoreCase = true)
            val matchesSearch = state.searchQuery.isBlank() ||
                item.productName.contains(state.searchQuery, ignoreCase = true) ||
                item.sku.contains(state.searchQuery, ignoreCase = true) ||
                item.tags.contains(state.searchQuery, ignoreCase = true) ||
                item.material.contains(state.searchQuery, ignoreCase = true) ||
                item.color.contains(state.searchQuery, ignoreCase = true) ||
                item.description.contains(state.searchQuery, ignoreCase = true)
            val matchesFav = !state.filterOnlyFavorites || item.isFavorite
            val matchesPrimary = !state.filterOnlyPrimary || item.isPrimary

            matchesCategory && matchesSearch && matchesFav && matchesPrimary
        }.let { list ->
            when (state.sortOption) {
                "Price Low-High" -> list.sortedBy { it.price }
                "Price High-Low" -> list.sortedByDescending { it.price }
                "Category" -> list.sortedBy { it.category }
                else -> list.sortedByDescending { it.createdAt }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // =========================================================================
    // Image Selection Logic (Modern ActivityResultContracts.PickVisualMedia & Storage)
    // =========================================================================

    /**
     * Helper to create Visual Media Request for Android PhotoPicker
     */
    fun createVisualMediaRequest(
        mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
    ): PickVisualMediaRequest {
        return PickVisualMediaRequest(mediaType)
    }

    /**
     * Called when images are selected from the device Photo Gallery via PickMultipleVisualMedia
     * or from local storage via OpenMultipleDocuments.
     */
    fun onImagesSelected(uris: List<Uri>, context: Context? = null) {
        if (uris.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val metadataList = uris.mapIndexed { index, uri ->
                val (fileName, mimeType, sizeFormatted) = extractUriMetadata(uri, context)
                SelectedImageMetadata(
                    uri = uri,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSizeFormatted = sizeFormatted,
                    isPrimary = index == 0,
                    sortOrder = index + 1
                )
            }

            withContext(Dispatchers.Main) {
                _uiState.update { current ->
                    current.copy(
                        selectedImageUris = metadataList,
                        primaryImageIndex = 0,
                        activePreviewIndex = 0,
                        showUploadDialog = true,
                        showAddSourceDialog = false,
                        error = null
                    )
                }
            }
        }
    }

    /**
     * Called when a single image is selected via PickVisualMedia or camera
     */
    fun onSingleImageSelected(uri: Uri?, context: Context? = null) {
        if (uri == null) return
        onImagesSelected(listOf(uri), context)
    }

    /**
     * Append additional image(s) to current selection queue
     */
    fun appendImages(newUris: List<Uri>, context: Context? = null) {
        if (newUris.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentList = _uiState.value.selectedImageUris
            val currentSize = currentList.size

            val additionalMetadata = newUris.mapIndexed { index, uri ->
                val (fileName, mimeType, sizeFormatted) = extractUriMetadata(uri, context)
                SelectedImageMetadata(
                    uri = uri,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSizeFormatted = sizeFormatted,
                    isPrimary = false,
                    sortOrder = currentSize + index + 1
                )
            }

            withContext(Dispatchers.Main) {
                _uiState.update { current ->
                    current.copy(
                        selectedImageUris = current.selectedImageUris + additionalMetadata,
                        showUploadDialog = true,
                        showAddSourceDialog = false
                    )
                }
            }
        }
    }

    /**
     * Remove an image from the current upload queue
     */
    fun removeImageAt(index: Int) {
        _uiState.update { current ->
            val updated = current.selectedImageUris.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            val newPrimary = if (current.primaryImageIndex >= updated.size) {
                (updated.size - 1).coerceAtLeast(0)
            } else {
                current.primaryImageIndex
            }
            val newPreview = if (current.activePreviewIndex >= updated.size) {
                (updated.size - 1).coerceAtLeast(0)
            } else {
                current.activePreviewIndex
            }

            current.copy(
                selectedImageUris = updated,
                primaryImageIndex = newPrimary,
                activePreviewIndex = newPreview,
                showUploadDialog = updated.isNotEmpty()
            )
        }
    }

    /**
     * Designate a specific image in the selection as the primary showcase image
     */
    fun setPrimaryIndex(index: Int) {
        _uiState.update { current ->
            if (index in current.selectedImageUris.indices) {
                val updated = current.selectedImageUris.mapIndexed { idx, item ->
                    item.copy(isPrimary = idx == index)
                }
                current.copy(
                    selectedImageUris = updated,
                    primaryImageIndex = index,
                    activePreviewIndex = index
                )
            } else {
                current
            }
        }
    }

    /**
     * Set active image preview index
     */
    fun setActivePreviewIndex(index: Int) {
        _uiState.update { current ->
            if (index in current.selectedImageUris.indices) {
                current.copy(activePreviewIndex = index)
            } else {
                current
            }
        }
    }

    /**
     * Clear all selected images
     */
    fun clearSelection() {
        _uiState.update { current ->
            current.copy(
                selectedImageUris = emptyList(),
                primaryImageIndex = 0,
                activePreviewIndex = 0,
                showUploadDialog = false,
                isUploading = false,
                uploadProgress = 0f,
                uploadStatusMessage = "",
                uploadBytesTransferred = 0L,
                uploadTotalBytes = 0L,
                currentUploadingFile = ""
            )
        }
        _uploadProgressState.value = UploadProgressState.Idle
    }

    // =========================================================================
    // Filter, Search & View Controls
    // =========================================================================

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleFilterOnlyFavorites() {
        _uiState.update { it.copy(filterOnlyFavorites = !it.filterOnlyFavorites) }
    }

    fun toggleFilterOnlyPrimary() {
        _uiState.update { it.copy(filterOnlyPrimary = !it.filterOnlyPrimary) }
    }

    fun setSortOption(sort: String) {
        _uiState.update { it.copy(sortOption = sort) }
    }

    fun toggleViewMode(isGrid: Boolean) {
        _uiState.update { it.copy(isGridView = isGrid) }
    }

    fun setShowAddSourceDialog(show: Boolean) {
        _uiState.update { it.copy(showAddSourceDialog = show) }
    }

    fun setShowUploadDialog(show: Boolean) {
        _uiState.update { it.copy(showUploadDialog = show) }
    }

    // =========================================================================
    // Product Design Image Database Operations
    // =========================================================================

    fun toggleFavorite(design: ProductDesignImage) {
        viewModelScope.launch {
            repository.toggleDesignFavorite(design.id, !design.isFavorite)
        }
    }

    fun setDesignAsPrimary(design: ProductDesignImage) {
        viewModelScope.launch {
            repository.setDesignAsPrimary(design.id, design.category)
        }
    }

    fun deleteDesign(design: ProductDesignImage, hardDelete: Boolean = false) {
        viewModelScope.launch {
            if (hardDelete) {
                repository.hardDeleteDesignImage(design.id)
            } else {
                repository.softDeleteDesignImage(design.id)
            }
        }
    }

    // =========================================================================
    // Retrofit & Coroutines Progress-Monitored Upload Service Implementation
    // =========================================================================

    /**
     * Upload selected designs using the Retrofit ImageUploadService, streaming live
     * progress states to UIState and UploadProgressState.
     */
    fun uploadSelectedDesigns(
        context: Context,
        productName: String,
        category: String,
        sku: String,
        description: String,
        dimensions: String,
        color: String,
        material: String,
        price: Double,
        tags: String,
        designType: String,
        roomType: String,
        productId: String = "",
        uploadedBy: String = "Sales Agent (Bekansi)",
        customUris: List<Uri>? = null,
        onSuccess: ((List<ProductDesignImage>) -> Unit)? = null
    ) {
        val urisToUpload = customUris ?: _uiState.value.selectedImageUris.map { it.uri }
        if (urisToUpload.isEmpty()) return

        val primaryIdx = _uiState.value.primaryImageIndex
        val uploader = imageUploadService ?: ImageUploadService(context.applicationContext)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadProgress = 0f,
                    uploadStatusMessage = "Initializing Retrofit Multipart Upload...",
                    totalFilesToUpload = urisToUpload.size,
                    currentFileIndex = 1
                )
            }

            uploader.uploadImagesWithProgress(
                uris = urisToUpload,
                productName = productName,
                category = category,
                sku = sku,
                description = description,
                dimensions = dimensions,
                color = color,
                material = material,
                price = price,
                tags = tags,
                designType = designType,
                roomType = roomType,
                productId = productId,
                primaryIndex = primaryIdx,
                uploadedBy = uploadedBy
            ).collect { progressState ->
                _uploadProgressState.value = progressState

                when (progressState) {
                    is UploadProgressState.Idle -> {
                        _uiState.update { it.copy(isUploading = false) }
                    }
                    is UploadProgressState.Preparing -> {
                        _uiState.update {
                            it.copy(
                                isUploading = true,
                                currentFileIndex = progressState.fileIndex,
                                totalFilesToUpload = progressState.totalFiles,
                                currentUploadingFile = progressState.fileName,
                                uploadStatusMessage = "Preparing ${progressState.fileName} for upload..."
                            )
                        }
                    }
                    is UploadProgressState.Uploading -> {
                        _uiState.update {
                            it.copy(
                                isUploading = true,
                                currentFileIndex = progressState.fileIndex,
                                totalFilesToUpload = progressState.totalFiles,
                                currentUploadingFile = progressState.fileName,
                                uploadProgress = progressState.overallProgress,
                                uploadBytesTransferred = progressState.bytesTransferred,
                                uploadTotalBytes = progressState.totalBytes,
                                uploadStatusMessage = progressState.statusMessage
                            )
                        }
                    }
                    is UploadProgressState.FileUploaded -> {
                        _uiState.update {
                            it.copy(
                                uploadProgress = progressState.overallProgress,
                                uploadStatusMessage = "Uploaded ${progressState.fileIndex} of ${progressState.totalFiles}: ${progressState.design.productName}"
                            )
                        }
                    }
                    is UploadProgressState.Success -> {
                        // Persist all uploaded design images into Room Database
                        repository.insertDesignImages(progressState.uploadedDesigns)

                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 1.0f,
                                uploadStatusMessage = progressState.summaryMessage,
                                selectedImageUris = emptyList(),
                                showUploadDialog = false
                            )
                        }
                        onSuccess?.invoke(progressState.uploadedDesigns)
                    }
                    is UploadProgressState.Error -> {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                error = "Upload error (${progressState.fileName}): ${progressState.errorMessage}",
                                uploadStatusMessage = "Upload paused due to error."
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Content Resolver Helper
    // =========================================================================

    private fun extractUriMetadata(uri: Uri, context: Context?): Triple<String, String, String> {
        var name = "furniture_design_${System.currentTimeMillis()}"
        var mimeType = "image/jpeg"
        var sizeFormatted = "1.8 MB"

        context?.contentResolver?.let { resolver ->
            try {
                mimeType = resolver.getType(uri) ?: "image/jpeg"
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            name = cursor.getString(nameIndex) ?: name
                        }
                        if (sizeIndex != -1) {
                            val sizeBytes = cursor.getLong(sizeIndex)
                            sizeFormatted = when {
                                sizeBytes > 1024 * 1024 -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
                                sizeBytes > 1024 -> String.format("%.0f KB", sizeBytes / 1024.0)
                                else -> "$sizeBytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to defaults if provider access fails
            }
        }

        return Triple(name, mimeType, sizeFormatted)
    }
}

/**
 * Factory for DesignGalleryViewModel
 */
class DesignGalleryViewModelFactory(
    private val repository: SalesRepository,
    private val imageUploadService: ImageUploadService? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DesignGalleryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DesignGalleryViewModel(repository, imageUploadService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

