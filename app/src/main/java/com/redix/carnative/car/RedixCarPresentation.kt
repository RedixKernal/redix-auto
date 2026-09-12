package com.redix.carnative.car

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.*
import android.webkit.*
import android.widget.*

/**
 * High-performance hardware-accelerated in-car display presentation.
 * Renders directly onto the car head unit screen canvas.
 */
class RedixCarPresentation(
    outerContext: Context,
    display: Display
) : Presentation(outerContext, display) {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var topBarLayout: LinearLayout
    private lateinit var webView: WebView
    private lateinit var customViewContainer: FrameLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    companion object {
        var currentInstance: RedixCarPresentation? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentInstance = this

        // Keep car display screen on during in-car driving / playback
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setupInCarViews()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupInCarViews() {
        rootLayout = RelativeLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#070A10"))
        }

        // --- Top In-Car Bar: Quick Dock Presets ---
        topBarLayout = LinearLayout(context).apply {
            id = View.generateViewId()
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 8, 16, 8)
            setBackgroundColor(Color.parseColor("#0D121C"))

            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_TOP)
            }
            layoutParams = params
        }

        val logoBadge = TextView(context).apply {
            text = "⚡ REDIX AUTO"
            setTextColor(Color.parseColor("#FF2A44"))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(8, 4, 16, 4)
        }
        topBarLayout.addView(logoBadge)

        // Presets
        val presets = listOf(
            "▶ YouTube" to "https://m.youtube.com",
            "🎮 Twitch" to "https://m.twitch.tv",
            "🗺 Maps" to "https://maps.google.com",
            "🎵 SoundCloud" to "https://m.soundcloud.com",
            "🎧 Spotify" to "https://open.spotify.com"
        )

        for ((label, url) in presets) {
            val btn = Button(context).apply {
                text = label
                setTextColor(Color.WHITE)
                textSize = 11f
                setBackgroundColor(Color.parseColor("#1B2434"))
                setPadding(12, 4, 12, 4)
                val btnParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                layoutParams = btnParams
                setOnClickListener {
                    loadUrl(url)
                }
            }
            topBarLayout.addView(btn)
        }

        // Refresh Action
        val refreshBtn = Button(context).apply {
            text = "🔄"
            setTextColor(Color.WHITE)
            textSize = 12f
            setBackgroundColor(Color.parseColor("#2A354A"))
            setPadding(8, 4, 8, 4)
            val btnParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 4, 0)
            }
            layoutParams = btnParams
            setOnClickListener {
                webView.reload()
            }
        }
        topBarLayout.addView(refreshBtn)

        rootLayout.addView(topBarLayout)

        // --- Hardware-Accelerated Chromium WebView ---
        webView = WebView(context).apply {
            val wvParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(RelativeLayout.BELOW, topBarLayout.id)
            }
            layoutParams = wvParams
            setBackgroundColor(Color.BLACK)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                mediaPlaybackRequiresUserGesture = false
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)

                // High-performance mobile Chrome UA
                userAgentString = "Mozilla/5.0 (Linux; Android 15; vivo X200 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Mobile Safari/537.36"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    safeBrowsingEnabled = false
                }
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    if (customView != null) {
                        callback?.onCustomViewHidden()
                        return
                    }
                    customView = view
                    customViewCallback = callback
                    topBarLayout.visibility = View.GONE
                    webView.visibility = View.GONE
                    customViewContainer.addView(view)
                    customViewContainer.visibility = View.VISIBLE
                }

                override fun onHideCustomView() {
                    if (customView == null) return
                    topBarLayout.visibility = View.VISIBLE
                    webView.visibility = View.VISIBLE
                    customViewContainer.removeView(customView)
                    customViewContainer.visibility = View.GONE
                    customViewCallback?.onCustomViewHidden()
                    customView = null
                    customViewCallback = null
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Auto-unmute video and enable playback
                    view?.evaluateJavascript(
                        """
                        (function() {
                            var v = document.querySelector('video');
                            if (v) {
                                v.muted = false;
                                v.play().catch(function(e){});
                            }
                        })();
                        """.trimIndent(),
                        null
                    )
                }
            }
        }
        rootLayout.addView(webView)

        // Fullscreen Container
        customViewContainer = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            setBackgroundColor(Color.BLACK)
        }
        rootLayout.addView(customViewContainer)

        setContentView(rootLayout)

        // Load YouTube by default
        loadUrl("https://m.youtube.com")
    }

    fun loadUrl(url: String) {
        if (::webView.isInitialized) {
            webView.post {
                webView.loadUrl(url)
            }
        }
    }

    /**
     * Injects car touchscreen tap events directly into in-car browser views.
     */
    fun dispatchCarClick(x: Float, y: Float) {
        if (!::rootLayout.isInitialized) return
        rootLayout.post {
            val downTime = SystemClock.uptimeMillis()
            val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
            val upEvent = MotionEvent.obtain(downTime, downTime + 40, MotionEvent.ACTION_UP, x, y, 0)

            rootLayout.dispatchTouchEvent(downEvent)
            rootLayout.dispatchTouchEvent(upEvent)

            downEvent.recycle()
            upEvent.recycle()
        }
    }

    /**
     * Injects car touchscreen scrolling into WebView.
     */
    fun dispatchCarScroll(distanceX: Float, distanceY: Float) {
        if (!::webView.isInitialized) return
        webView.post {
            webView.scrollBy(distanceX.toInt(), distanceY.toInt())
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (currentInstance == this) {
            currentInstance = null
        }
        try {
            webView.destroy()
        } catch (e: Exception) {
            // Ignore cleanup
        }
    }
}
