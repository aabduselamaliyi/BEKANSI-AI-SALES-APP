package com.example.data.repository

import android.content.Context
import androidx.work.WorkInfo
import com.example.data.database.MediaAssetDao
import com.example.data.model.MediaAsset
import com.example.service.MediaUploadService
import com.example.service.MediaUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Data state wrapper representing the live upload progress of an enqueued media asset.
 */
data class UploadProgress(
    val assetId: String,
    val progress: Int = 0,
    val status: String = "Initializing",
    val downloadUrl: String = "",
    val error: String = ""
)

/**
 * MediaRepository integrates MediaUploadService (WorkManager background uploads)
 * with StateFlow progress tracking and Room Database persistent storage.
 */
class MediaRepository(
    private val mediaAssetDao: MediaAssetDao,
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    /**
     * Flow of all saved DAM media assets from Room database.
     */
    val allMediaAssets: Flow<List<MediaAsset>> = mediaAssetDao.getAllMediaAssets()

    /**
     * Live StateFlow map tracking real-time upload progress by asset ID.
     */
    private val _uploadProgressMap = MutableStateFlow<Map<String, UploadProgress>>(emptyMap())
    val uploadProgressState: StateFlow<Map<String, UploadProgress>> = _uploadProgressMap.asStateFlow()

    /**
     * Triggers a background file upload via MediaUploadService (WorkManager),
     * tracks live progress in StateFlow, and persists initial and final status to Room DB.
     */
    fun uploadMedia(
        context: Context,
        fileUri: String,
        mediaType: String,
        title: String,
        category: String,
        price: String = "N/A",
        assetId: String = "DAM-${System.currentTimeMillis()}"
    ): UUID {
        val workId = MediaUploadService.enqueueUpload(
            context = context,
            fileUri = fileUri,
            mediaType = mediaType,
            title = title,
            category = category,
            assetId = assetId
        )

        val initialAsset = MediaAsset(
            id = assetId,
            title = title,
            category = category,
            mediaType = mediaType,
            fileUri = fileUri,
            downloadUrl = "",
            status = "Uploading",
            progress = 0,
            price = price
        )

        // Save initial asset to Room DB and StateFlow
        repositoryScope.launch {
            mediaAssetDao.insertMediaAsset(initialAsset)
            updateProgressState(assetId, UploadProgress(assetId, 0, "Enqueued in WorkManager"))
        }

        // Observe WorkManager progress and save updates to StateFlow & Room DB
        repositoryScope.launch {
            MediaUploadService.getWorkInfoFlow(context, workId).collect { workInfo ->
                if (workInfo != null) {
                    val progress = workInfo.progress.getInt(MediaUploadWorker.KEY_PROGRESS, 0)
                    val statusText = workInfo.progress.getString(MediaUploadWorker.KEY_STATUS) ?: workInfo.state.name
                    val downloadUrl = workInfo.progress.getString(MediaUploadWorker.KEY_DOWNLOAD_URL)
                        ?: workInfo.outputData.getString(MediaUploadWorker.KEY_DOWNLOAD_URL) ?: ""
                    val error = workInfo.outputData.getString(MediaUploadWorker.KEY_ERROR) ?: ""

                    val finalStatus = when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> "Approved"
                        WorkInfo.State.FAILED -> "Failed"
                        WorkInfo.State.CANCELLED -> "Cancelled"
                        else -> "Uploading"
                    }

                    val progressItem = UploadProgress(
                        assetId = assetId,
                        progress = if (workInfo.state == WorkInfo.State.SUCCEEDED) 100 else progress,
                        status = statusText,
                        downloadUrl = downloadUrl,
                        error = error
                    )

                    updateProgressState(assetId, progressItem)

                    // Update status in Room Database
                    mediaAssetDao.updateUploadProgress(
                        id = assetId,
                        progress = progressItem.progress,
                        status = finalStatus,
                        downloadUrl = downloadUrl.ifEmpty { "https://firebasestorage.googleapis.com/v0/b/bekansi-furniture.appspot.com/o/media%2F${category.lowercase()}%2F${title.replace(" ", "_").lowercase()}.jpg?alt=media" }
                    )
                }
            }
        }

        return workId
    }

    /**
     * Saves or updates a media asset entity directly in Room DB.
     */
    suspend fun saveMediaAsset(asset: MediaAsset) = withContext(Dispatchers.IO) {
        mediaAssetDao.insertMediaAsset(asset)
    }

    /**
     * Deletes a media asset from Room DB by ID.
     */
    suspend fun deleteMediaAsset(assetId: String) = withContext(Dispatchers.IO) {
        mediaAssetDao.deleteMediaAssetById(assetId)
        _uploadProgressMap.value = _uploadProgressMap.value - assetId
    }

    /**
     * Cancels an enqueued upload task.
     */
    fun cancelUpload(context: Context, workId: UUID) {
        MediaUploadService.cancelUpload(context, workId)
    }

    private fun updateProgressState(assetId: String, progress: UploadProgress) {
        val current = _uploadProgressMap.value.toMutableMap()
        current[assetId] = progress
        _uploadProgressMap.value = current
    }
}
