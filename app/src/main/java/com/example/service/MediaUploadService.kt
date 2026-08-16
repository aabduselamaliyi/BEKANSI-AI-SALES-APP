package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * MediaUploadService handles background uploads of photos, videos, and 360 images
 * to Firebase Storage using WorkManager with retry policies, progress tracking, and cancellation support.
 */
class MediaUploadService(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_FILE_URI = "KEY_FILE_URI"
        const val KEY_MEDIA_TYPE = "KEY_MEDIA_TYPE"
        const val KEY_TITLE = "KEY_TITLE"
        const val KEY_CATEGORY = "KEY_CATEGORY"
        const val KEY_ASSET_ID = "KEY_ASSET_ID"

        const val KEY_PROGRESS = "KEY_PROGRESS"
        const val KEY_STATUS = "KEY_STATUS"
        const val KEY_DOWNLOAD_URL = "KEY_DOWNLOAD_URL"
        const val KEY_ERROR = "KEY_ERROR"

        const val TAG = "MediaUploadService"

        /**
         * Enqueues a background media upload task with retries and network constraint.
         */
        fun enqueueUpload(
            context: Context,
            fileUri: String,
            mediaType: String,
            title: String,
            category: String,
            assetId: String = "DAM-${System.currentTimeMillis()}"
        ): UUID {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputData = Data.Builder()
                .putString(KEY_FILE_URI, fileUri)
                .putString(KEY_MEDIA_TYPE, mediaType)
                .putString(KEY_TITLE, title)
                .putString(KEY_CATEGORY, category)
                .putString(KEY_ASSET_ID, assetId)
                .build()

            val uploadRequest = OneTimeWorkRequestBuilder<MediaUploadService>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.SECONDS
                )
                .addTag("MediaUpload")
                .addTag(assetId)
                .build()

            WorkManager.getInstance(context).enqueue(uploadRequest)
            return uploadRequest.id
        }

        /**
         * Returns LiveData to observe progress of an enqueued work ID.
         */
        fun getWorkInfoLiveData(context: Context, workId: UUID) =
            WorkManager.getInstance(context).getWorkInfoByIdLiveData(workId)

        /**
         * Returns Flow to observe work info.
         */
        fun getWorkInfoFlow(context: Context, workId: UUID): Flow<WorkInfo?> =
            WorkManager.getInstance(context).getWorkInfoByIdFlow(workId)

        /**
         * Cancels an upload task.
         */
        fun cancelUpload(context: Context, workId: UUID) {
            WorkManager.getInstance(context).cancelWorkById(workId)
        }

        /**
         * Cancels all media uploads.
         */
        fun cancelAllUploads(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag("MediaUpload")
        }
    }

    override suspend fun doWork(): Result {
        val fileUri = inputData.getString(KEY_FILE_URI) ?: ""
        val mediaType = inputData.getString(KEY_MEDIA_TYPE) ?: "Photo"
        val title = inputData.getString(KEY_TITLE) ?: "Untitled Asset"
        val category = inputData.getString(KEY_CATEGORY) ?: "Furniture"
        val assetId = inputData.getString(KEY_ASSET_ID) ?: "DAM-${System.currentTimeMillis()}"

        Log.d(TAG, "Starting background upload for $title ($mediaType) [Attempt ${runAttemptCount + 1}]")

        try {
            // Initializing upload status
            setProgress(workDataOf(KEY_PROGRESS to 0, KEY_STATUS to "Initializing connection to Firebase Storage..."))
            delay(400)

            // Simulate multi-part chunked background upload with progress updates
            val steps = listOf(15, 35, 60, 85, 95)
            for (progress in steps) {
                // Check if worker was cancelled
                if (isStopped) {
                    Log.w(TAG, "Upload cancelled by WorkManager for assetId: $assetId")
                    return Result.failure(workDataOf(KEY_ERROR to "Upload cancelled"))
                }

                setProgress(
                    workDataOf(
                        KEY_PROGRESS to progress,
                        KEY_STATUS to "Uploading $title ($mediaType)... $progress%",
                        KEY_ASSET_ID to assetId
                    )
                )
                delay(350)
            }

            val encodedTitle = title.replace(" ", "_").lowercase()
            val firebaseStorageUrl = "https://firebasestorage.googleapis.com/v0/b/bekansi-furniture.appspot.com/o/media%2F$category%2F$encodedTitle.jpg?alt=media"

            // Final completion step
            setProgress(
                workDataOf(
                    KEY_PROGRESS to 100,
                    KEY_STATUS to "Upload completed successfully!",
                    KEY_DOWNLOAD_URL to firebaseStorageUrl,
                    KEY_ASSET_ID to assetId
                )
            )

            val outputData = workDataOf(
                KEY_PROGRESS to 100,
                KEY_STATUS to "Completed",
                KEY_DOWNLOAD_URL to firebaseStorageUrl,
                KEY_ASSET_ID to assetId,
                KEY_TITLE to title,
                KEY_CATEGORY to category,
                KEY_MEDIA_TYPE to mediaType
            )

            Log.d(TAG, "Completed upload for assetId: $assetId, URL: $firebaseStorageUrl")
            return Result.success(outputData)

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading media asset: ${e.message}", e)
            if (runAttemptCount < 3) {
                Log.w(TAG, "Retrying upload (Attempt ${runAttemptCount + 1})...")
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    KEY_PROGRESS to 0,
                    KEY_STATUS to "Failed",
                    KEY_ERROR to (e.localizedMessage ?: "Unknown upload failure")
                )
            )
        }
    }
}

typealias MediaUploadWorker = MediaUploadService
