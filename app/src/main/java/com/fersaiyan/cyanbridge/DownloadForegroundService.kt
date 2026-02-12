package com.fersaiyan.cyanbridge

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class DownloadForegroundService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "cyanbridge_download_channel"
        const val ACTION_START = "com.fersaiyan.cyanbridge.START_DOWNLOAD_SERVICE"
        const val ACTION_STOP = "com.fersaiyan.cyanbridge.STOP_DOWNLOAD_SERVICE"
        const val ACTION_UPDATE = "com.fersaiyan.cyanbridge.UPDATE_DOWNLOAD_PROGRESS"
        const val EXTRA_CURRENT_FILE = "current_file"
        const val EXTRA_TOTAL_FILES = "total_files"
        const val EXTRA_FILENAME = "filename"
        const val EXTRA_PROGRESS_PERCENT = "progress_percent"
        const val EXTRA_DOWNLOADED_MB = "downloaded_mb"
        const val EXTRA_TOTAL_MB = "total_mb"
        const val EXTRA_SPEED_MBPS = "speed_mbps"

        fun start(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun updateProgress(
            context: Context,
            currentFile: Int,
            totalFiles: Int,
            filename: String,
            progressPercent: Int,
            downloadedMB: Double,
            totalMB: Double,
            speedMBps: Double
        ) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_CURRENT_FILE, currentFile)
                putExtra(EXTRA_TOTAL_FILES, totalFiles)
                putExtra(EXTRA_FILENAME, filename)
                putExtra(EXTRA_PROGRESS_PERCENT, progressPercent)
                putExtra(EXTRA_DOWNLOADED_MB, downloadedMB)
                putExtra(EXTRA_TOTAL_MB, totalMB)
                putExtra(EXTRA_SPEED_MBPS, speedMBps)
            }
            context.startService(intent)
        }
    }

    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, createNotification(
                    currentFile = 0,
                    totalFiles = 0,
                    filename = "Preparing download...",
                    progressPercent = 0,
                    downloadedMB = 0.0,
                    totalMB = 0.0,
                    speedMBps = 0.0
                ))
            }
            ACTION_UPDATE -> {
                val currentFile = intent.getIntExtra(EXTRA_CURRENT_FILE, 0)
                val totalFiles = intent.getIntExtra(EXTRA_TOTAL_FILES, 0)
                val filename = intent.getStringExtra(EXTRA_FILENAME) ?: ""
                val progressPercent = intent.getIntExtra(EXTRA_PROGRESS_PERCENT, 0)
                val downloadedMB = intent.getDoubleExtra(EXTRA_DOWNLOADED_MB, 0.0)
                val totalMB = intent.getDoubleExtra(EXTRA_TOTAL_MB, 0.0)
                val speedMBps = intent.getDoubleExtra(EXTRA_SPEED_MBPS, 0.0)

                notificationManager?.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        currentFile,
                        totalFiles,
                        filename,
                        progressPercent,
                        downloadedMB,
                        totalMB,
                        speedMBps
                    )
                )
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "File Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of file downloads from device"
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        currentFile: Int,
        totalFiles: Int,
        filename: String,
        progressPercent: Int,
        downloadedMB: Double,
        totalMB: Double,
        speedMBps: Double
    ): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (totalFiles > 0) {
            "Downloading file $currentFile/$totalFiles"
        } else {
            "Preparing download..."
        }

        val displayName = if (!filename.contains(".")) {
            "$filename.mp4"
        } else {
            filename
        }

        val progressText = if (totalMB >= 1.0) {
            String.format("%.1f MB of %.1f MB, %.1f MB/s", downloadedMB, totalMB, speedMBps)
        } else {
            val downloadedKB = downloadedMB * 1024
            val totalKB = totalMB * 1024
            val speedKBps = speedMBps * 1024
            String.format("%.0f KB of %.0f KB, %.1f KB/s", downloadedKB, totalKB, speedKBps)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(displayName)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$displayName\n$progressText"))
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progressPercent, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }
}