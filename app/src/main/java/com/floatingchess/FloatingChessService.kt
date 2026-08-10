package com.floatingchess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.os.Handler
import android.os.Looper

class FloatingChessService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
    private lateinit var layoutParams: WindowManager.LayoutParams
    
    private var isMoveMode = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        layoutParams = WindowManager.LayoutParams(
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
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 200

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            setBackgroundColor(0x00000000)
            
            // This is the bridge connecting your JS to Kotlin
            addJavascriptInterface(WebAppInterface(), "AndroidBridge")
            
            loadUrl("file:///android_asset/chess_overlay.html")
        }

        // Intercept touches if Move Mode is active
        webView.setOnTouchListener { _, event ->
            if (isMoveMode) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(webView, layoutParams)
                        true
                    }
                    else -> false
                }
            } else {
                false // Let touch pass through to your HTML piece dragging
            }
        }

        windowManager.addView(webView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webView.isInitialized) windowManager.removeView(webView)
    }

    // The functions your HTML can trigger
    inner class WebAppInterface {
        @JavascriptInterface
        fun toggleMoveMode(enabled: Boolean) {
            isMoveMode = enabled
        }

        @JavascriptInterface
        fun minimizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = 150 // Shrink window for bubble
                layoutParams.height = 150
                windowManager.updateViewLayout(webView, layoutParams)
            }
        }

        @JavascriptInterface
        fun maximizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                windowManager.updateViewLayout(webView, layoutParams)
            }
        }
    }
}
