package com.nostea.os

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

/** Full browser used by the YouTube tile and the Browser tile. Plain WebView, so YouTube and any website work. */
class BrowserActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var urlBar: EditText
    private lateinit var root: FrameLayout
    private var customView: View? = null
    private var customCallback: WebChromeClient.CustomViewCallback? = null

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun button(label: String, description: String, action: () -> Unit) = TextView(this).apply {
        text = label
        contentDescription = description
        textSize = 20f
        setTextColor(Color.WHITE)
        gravity = Gravity.CENTER
        minimumWidth = dp(44)
        minimumHeight = dp(44)
        setOnClickListener { action() }
    }

    private fun normalize(input: String): String {
        val q = input.trim()
        if (q.isEmpty()) return "https://duckduckgo.com/"
        if (q.startsWith("http://") || q.startsWith("https://")) return q
        if (!q.contains(' ') && q.contains('.')) return "https://$q"
        return "https://duckduckgo.com/?q=" + Uri.encode(q)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        web = WebView(this)
        urlBar = EditText(this).apply {
            setSingleLine()
            setTextColor(Color.WHITE)
            setHintTextColor(0xFFB7BDCA.toInt())
            hint = "Search or type a URL"
            textSize = 14f
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
                ) {
                    web.loadUrl(normalize(text.toString()))
                    web.requestFocus()
                    true
                } else false
            }
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF111318.toInt())
            setPadding(dp(6), dp(6), dp(6), dp(6))
            addView(button("<", "Back") { if (web.canGoBack()) web.goBack() })
            addView(button(">", "Forward") { if (web.canGoForward()) web.goForward() })
            addView(urlBar, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(button("↻", "Reload") { web.reload() })
            addView(button("✕", "Close browser") { finish() })
        }

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(bar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        }
        root = FrameLayout(this).apply { addView(column) }
        setContentView(root)

        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            setSupportMultipleWindows(false)
        }
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                if (uri.scheme == "http" || uri.scheme == "https") return false
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                } catch (e: Exception) {
                    // No app can handle this link; stay on the page.
                }
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                urlBar.setText(url)
            }
        }

        web.webChromeClient = object : WebChromeClient() {
            // Fullscreen video (YouTube's fullscreen button).
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }
                customView = view
                customCallback = callback
                view.setBackgroundColor(Color.BLACK)
                root.addView(view, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            override fun onHideCustomView() {
                customView?.let { root.removeView(it) }
                customView = null
                customCallback?.onCustomViewHidden()
                customCallback = null
            }
        }

        val start = intent.getStringExtra("url") ?: "https://duckduckgo.com/"
        web.loadUrl(start)
    }

    @Deprecated("Back walks the page history, then closes")
    override fun onBackPressed() {
        when {
            customView != null -> web.webChromeClient?.onHideCustomView()
            web.canGoBack() -> web.goBack()
            else -> finish()
        }
    }

    override fun onPause() {
        super.onPause()
        web.onPause()
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
    }

    override fun onDestroy() {
        (web.parent as? ViewGroup)?.removeView(web)
        web.destroy()
        super.onDestroy()
    }
}
