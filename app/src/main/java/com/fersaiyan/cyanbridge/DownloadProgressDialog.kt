package com.fersaiyan.cyanbridge

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView

class DownloadProgressDialog(context: Context) : Dialog(context) {
    private lateinit var progressBar: ProgressBar
    private lateinit var titleText: TextView
    private lateinit var filenameText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_download_progress)
        setCancelable(false)

        progressBar = findViewById(R.id.progressBar)
        titleText = findViewById(R.id.titleText)
        filenameText = findViewById(R.id.filenameText)
        statusText = findViewById(R.id.statusText)
    }

    fun updateProgress(
        currentFile: Int,
        totalFiles: Int,
        fileName: String,
        downloadedBytes: Long,
        totalBytes: Long,
        speedBytesPerSec: Double
    ) {
        val progressPercent = if (totalBytes > 0) {
            ((downloadedBytes.toDouble() / totalBytes) * 100).toInt()
        } else 0

        progressBar.progress = progressPercent

        titleText.text = "Downloading file $currentFile/$totalFiles"

        // Add extension if missing for cleaner display
        val displayName = if (!fileName.contains(".")) {
            "$fileName.mp4"  // Add .mp4 to extensionless video files
        } else {
            fileName
        }
        filenameText.text = displayName

        // Smart formatting: use MB if >= 1 MB, otherwise KB
        val downloadedMB = downloadedBytes / (1024.0 * 1024.0)
        val totalMB = totalBytes / (1024.0 * 1024.0)

        val sizeText = if (totalMB >= 1.0) {
            String.format("%.1f MB of %.1f MB", downloadedMB, totalMB)
        } else {
            val downloadedKB = downloadedBytes / 1024.0
            val totalKB = totalBytes / 1024.0
            String.format("%.0f KB of %.0f KB", downloadedKB, totalKB)
        }

        // Speed: show MB/s if >= 1 MB/s, otherwise KB/s
        val speedMBps = speedBytesPerSec / (1024.0 * 1024.0)
        val speedText = if (speedMBps >= 1.0) {
            String.format("%.1f MB/s", speedMBps)
        } else {
            val speedKBps = speedBytesPerSec / 1024.0
            String.format("%.1f KB/s", speedKBps)
        }

        // Combine size and speed on one line
        statusText.text = "$sizeText, $speedText"
    }

    fun setTitle(title: String) {
        titleText.text = title
    }
}