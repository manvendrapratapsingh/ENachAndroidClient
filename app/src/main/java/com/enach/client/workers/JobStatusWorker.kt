package com.enach.client.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.enach.client.R
import com.enach.client.data.repository.ENachRepository
import com.enach.client.utils.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class JobStatusWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val repository: ENachRepository
) : CoroutineWorker(context, params) {
    
    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): JobStatusWorker
    }
    
    companion object {
        const val CHANNEL_ID = "job_status_channel"
        const val CHANNEL_NAME = "Job Status Updates"
        const val NOTIFICATION_ID = 1001
    }
    
    override suspend fun doWork(): Result {
        val jobId = inputData.getString("job_id") ?: return Result.failure()
        
        return try {
            val result = repository.getJobStatus(jobId).first()
            
            when (result) {
                is Resource.Success -> {
                    result.data?.let { jobStatusResponse ->
                        val jobStatus = jobStatusResponse.status.lowercase()
                        
                        when (jobStatus) {
                            "completed" -> {
                                showNotification(
                                    "Job Completed",
                                    "Your e-NACH processing job has been completed successfully."
                                )
                                Result.success()
                            }
                            "validation_required" -> {
                                showNotification(
                                    "Validation Required",
                                    "Job requires manual validation. Please review and confirm."
                                )
                                Result.success()
                            }
                            "failed" -> {
                                showNotification(
                                    "Job Failed",
                                    "Job has failed. ${jobStatusResponse.errorMessage ?: "Unknown error"}"
                                )
                                Result.failure()
                            }
                            "processing", "pending" -> {
                                // Still processing, retry later
                                Result.retry()
                            }
                            "validated" -> {
                                showNotification(
                                    "Job Validated",
                                    "Job has been validated successfully."
                                )
                                Result.success()
                            }
                            else -> Result.retry()
                        }
                    } ?: Result.failure()
                }
                is Resource.Error -> {
                    // Network error, retry
                    Result.retry()
                }
                is Resource.Loading -> {
                    Result.retry()
                }
                else -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    private fun showNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for e-NACH job status updates"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Handle the case where the permission is not granted.
            // For now, we'll just return, but in a real app, you'd want to
            // request the permission from the user.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
