package com.floatingchess

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat

class FloatingChessService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var webView: WebView

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startMyForeground()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Create WebView dynamically
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            loadUrl("file:///android_asset/chess_overlay.html")
        }

        // Window Layout parameters
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            320, // width in dp
            420, // height in dp
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        floatingView = webView
        windowManager.addView(floatingView, params)

        // Make window draggable
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        floatingView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    true
                }
                else -> false
            }
        }
    }

    private fun startMyForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "floating_chess_channel"
            val channel = NotificationChannel(channelId, "Floating Chess", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Floating Chess Board Active")
                .setContentText("Tap to keep chess overlay alive")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()

            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }
}
