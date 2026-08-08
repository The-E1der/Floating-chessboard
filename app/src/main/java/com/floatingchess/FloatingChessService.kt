package com.floatingchess

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebView

class FloatingChessService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Set up the window parameters for the floating overlay
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        // Initial position of the overlay
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 200

        // Initialize and configure the WebView
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            
            // Set transparent background so only the chessboard shows
            setBackgroundColor(0x00000000)
            
            loadUrl("file:///android_asset/chess_overlay.html")
        }

        // Add the WebView to the WindowManager
        windowManager.addView(webView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the view when the service stops to prevent memory leaks
        if (::webView.isInitialized) {
            windowManager.removeView(webView)
        }
    }
}
