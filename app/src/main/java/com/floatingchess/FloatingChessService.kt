package com.floatingchess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage

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

    // Converts Android's 'dp' (density-independent pixels) to raw screen pixels
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingChess", "BUILD-CHECK v2: onCreate() running, updated code is live")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Hardcode the max width right out the gate!
        val windowWidthPx = dpToPx(340)

        layoutParams = WindowManager.LayoutParams(
            windowWidthPx, // No more WRAP_CONTENT for width
            dpToPx(480), // Safe fallback until JS reports the real content height
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
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
            setBackgroundColor(0x00000000)
            
            addJavascriptInterface(WebAppInterface(), "AndroidBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                    Log.d("FloatingChess", "JS console: ${msg.message()} (${msg.sourceId()}:${msg.lineNumber()})")
                    return true
                }
            }

            loadUrl("file:///android_asset/chess_overlay.html")
        }

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
                    MotionEvent.ACTION_UP -> {
                        isMoveMode = false
                        webView.evaluateJavascript(
                            "moveModeActive = false; document.getElementById('move-btn').style.opacity = '0.5'; document.getElementById('move-btn').style.backgroundColor = '#333';",
                            null
                        )
                        true
                    }
                    else -> false
                }
            } else {
                false 
            }
        }

        windowManager.addView(webView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webView.isInitialized) windowManager.removeView(webView)
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun toggleMoveMode(enabled: Boolean) {
            isMoveMode = enabled
        }

        @JavascriptInterface
        fun minimizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = dpToPx(70) // Shrink to bubble width
                layoutParams.height = dpToPx(70) // Shrink to bubble height
                windowManager.updateViewLayout(webView, layoutParams)
            }
        }

        @JavascriptInterface
        fun maximizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = dpToPx(340)
package com.floatingchess

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.widget.Toast

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

    // Converts Android's 'dp' (density-independent pixels) to raw screen pixels
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingChess", "BUILD-CHECK v2: onCreate() running, updated code is live")
        Toast.makeText(this, "FloatingChess BUILD-CHECK v2 loaded", Toast.LENGTH_LONG).show()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Hardcode the max width right out the gate!
        val windowWidthPx = dpToPx(340)

        layoutParams = WindowManager.LayoutParams(
            windowWidthPx, // No more WRAP_CONTENT for width
            dpToPx(480), // Safe fallback until JS reports the real content height
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
            settings.useWideViewPort = false
            settings.loadWithOverviewMode = false
            setBackgroundColor(0x00000000)
            
            addJavascriptInterface(WebAppInterface(), "AndroidBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                    Log.d("FloatingChess", "JS console: ${msg.message()} (${msg.sourceId()}:${msg.lineNumber()})")
                    return true
                }
            }

            loadUrl("file:///android_asset/chess_overlay.html")
        }

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
                    MotionEvent.ACTION_UP -> {
                        isMoveMode = false
                        webView.evaluateJavascript(
                            "moveModeActive = false; document.getElementById('move-btn').style.opacity = '0.5'; document.getElementById('move-btn').style.backgroundColor = '#333';",
                            null
                        )
                        true
                    }
                    else -> false
                }
            } else {
                false 
            }
        }

        windowManager.addView(webView, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::webView.isInitialized) windowManager.removeView(webView)
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun toggleMoveMode(enabled: Boolean) {
            isMoveMode = enabled
        }

        @JavascriptInterface
        fun minimizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = dpToPx(70) // Shrink to bubble width
                layoutParams.height = dpToPx(70) // Shrink to bubble height
                windowManager.updateViewLayout(webView, layoutParams)
            }
        }

        @JavascriptInterface
        fun maximizeWindow() {
            Handler(Looper.getMainLooper()).post {
                layoutParams.width = dpToPx(340)
                windowManager.updateViewLayout(webView, layoutParams)
                Log.d("FloatingChess", "maximizeWindow: width=${layoutParams.width} height=${layoutParams.height}")
                Toast.makeText(this@FloatingChessService, "Maximize step1: w=${layoutParams.width} h=${layoutParams.height}", Toast.LENGTH_SHORT).show()
            }
        }

        // Called from JS with the *actual* measured height of #main-container,
        // in raw pixels (already multiplied by devicePixelRatio on the JS side).
        // This replaces guessing a dp value in Kotlin, which is what was
        // unreliable before.
        @JavascriptInterface
        fun updateContentHeight(heightPx: Int) {
            Handler(Looper.getMainLooper()).post {
                Log.d("FloatingChess", "updateContentHeight received: ${heightPx}px (density=${resources.displayMetrics.density})")
                Toast.makeText(this@FloatingChessService, "reportHeight -> ${heightPx}px (density=${resources.displayMetrics.density})", Toast.LENGTH_LONG).show()
                if (heightPx > 0) {
                    layoutParams.height = heightPx
                    windowManager.updateViewLayout(webView, layoutParams)
                }
            }
        }
    }
}
