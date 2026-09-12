package com.redix.carnative

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.redix.carnative.car.RedixCarPresentation
import com.redix.carnative.media.CarMediaSessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var topBarsContainer: View
    private lateinit var customViewContainer: FrameLayout
    private lateinit var btnRestoreToolbar: TextView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var isFullscreenMode = false
    private var currentUrl: String = "https://m.youtube.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep display awake
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize background media session for steering wheel controls
        CarMediaSessionManager.initSession(applicationContext)

        initViews()
        setupBackNavigation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initViews() {
        webView = findViewById(R.id.phonePreviewWebView)
        topBarsContainer = findViewById(R.id.topBarsContainer)
        customViewContainer = findViewById(R.id.customViewContainer)
        btnRestoreToolbar = findViewById(R.id.btnRestoreToolbar)

        val btnFullscreen: TextView = findViewById(R.id.btnFullscreen)
        val btnHideToolbar: TextView = findViewById(R.id.btnHideToolbar)
        val btnYoutube: TextView = findViewById(R.id.btnPresetYoutube)
        val btnSpotify: TextView = findViewById(R.id.btnPresetSpotify)
        val btnGoogle: TextView = findViewById(R.id.btnPresetGoogle)

        // Setup high-performance hardware-accelerated WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            loadWithOverviewMode = true
            useWideViewPort = true
            allowFileAccess = true
            allowContentAccess = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            userAgentString = "Mozilla/5.0 (Linux; Android 15; vivo X200 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }

        // Fullscreen video support for YouTube HTML5 video player
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                topBarsContainer.visibility = View.GONE
                webView.visibility = View.GONE
                btnRestoreToolbar.visibility = View.GONE
                customViewContainer.addView(
                    view,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                customViewContainer.visibility = View.VISIBLE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                customViewContainer.removeView(customView)
                customViewContainer.visibility = View.GONE
                customViewCallback?.onCustomViewHidden()
                customView = null
                customViewCallback = null

                webView.visibility = View.VISIBLE
                if (!isFullscreenMode) {
                    topBarsContainer.visibility = View.VISIBLE
                } else {
                    btnRestoreToolbar.visibility = View.VISIBLE
                }
            }
        }

        // Hide / Fullscreen Controls
        btnFullscreen.setOnClickListener {
            toggleFullscreen(true)
        }

        btnHideToolbar.setOnClickListener {
            toggleFullscreen(true)
        }

        btnRestoreToolbar.setOnClickListener {
            toggleFullscreen(false)
        }

        // Preset Actions
        btnYoutube.setOnClickListener { navigateTo("https://m.youtube.com") }
        btnSpotify.setOnClickListener { navigateTo("https://open.spotify.com") }
        btnGoogle.setOnClickListener { navigateTo("https://www.google.com") }

        // Start on YouTube
        navigateTo(currentUrl)
    }

    private fun toggleFullscreen(enable: Boolean) {
        isFullscreenMode = enable
        if (enable) {
            topBarsContainer.visibility = View.GONE
            btnRestoreToolbar.visibility = View.VISIBLE
            Toast.makeText(this, "Fullscreen Active (Tap '▼ MENU' to restore)", Toast.LENGTH_SHORT).show()
        } else {
            topBarsContainer.visibility = View.VISIBLE
            btnRestoreToolbar.visibility = View.GONE
        }
    }

    private fun navigateTo(url: String) {
        currentUrl = url
        webView.loadUrl(url)

        // If car screen presentation is connected, synchronize URL
        RedixCarPresentation.currentInstance?.loadUrl(url)
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (customView != null) {
                    webView.webChromeClient?.onHideCustomView()
                } else if (isFullscreenMode) {
                    toggleFullscreen(false)
                } else if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
