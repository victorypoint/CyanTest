package com.fersaiyan.cyanbridge

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class LivePreviewDialog(context: Context) : Dialog(context) {
    private lateinit var previewImage: ImageView
    private lateinit var previewStatus: TextView
    private lateinit var btnStopPreview: Button

    var onStopCallback: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_live_preview)

        previewImage = findViewById(R.id.previewImage)
        previewStatus = findViewById(R.id.previewStatus)
        btnStopPreview = findViewById(R.id.btnStopPreview)

        btnStopPreview.setOnClickListener {
            onStopCallback?.invoke()
            dismiss()
        }

        setCancelable(false)

        // Set dialog to 90% of screen width with 4:3 aspect ratio
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // Apply 4:3 aspect ratio sizing
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.95).toInt() // 95% of screen width
        val imageHeight = (dialogWidth * 3 / 4) // 4:3 aspect ratio

        window?.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    fun updateFrame(bitmap: Bitmap) {
        previewImage.setImageBitmap(bitmap)
    }

    fun updateStatus(status: String) {
        previewStatus.text = status
    }
}