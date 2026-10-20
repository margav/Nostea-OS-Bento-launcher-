package com.nostea.os

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONArray
import org.json.JSONObject

/** The Nostea home screen: the Bento UI (assets/index.html) running inside a WebView. */
class MainActivity : Activity() {

    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this)
        setContentView(web)
        with(web.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }
        web.webViewClient = object : WebViewClient() {
            // The launcher UI never navigates away; links go through the bridge instead.
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = true
        }
        web.addJavascriptInterface(Bridge(), "NosteaNative")
        web.loadUrl("file:///android_asset/index.html")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Home button pressed while Nostea is already showing: close any open app panel.
        if (intent.hasCategory(Intent.CATEGORY_HOME)) {
            web.evaluateJavascript("window.nosteaHome && window.nosteaHome()", null)
        }
    }

    @Deprecated("Launcher: back never leaves the home screen")
    override fun onBackPressed() {
        web.evaluateJavascript("window.nosteaBack && window.nosteaBack()", null)
    }

    inner class Bridge {

        @JavascriptInterface
        fun openUrl(url: String) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) return
            runOnUiThread {
                startActivity(Intent(this@MainActivity, BrowserActivity::class.java).putExtra("url", url))
            }
        }

        @JavascriptInterface
        fun intent(kind: String): Boolean {
            val i = when (kind) {
                "phone" -> Intent(Intent.ACTION_DIAL)
                "sms" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
                "camera" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                "maps" -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="))
                "gallery" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_GALLERY)
                "music" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MUSIC)
                "calendar" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
                "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
                "homesettings" -> Intent(Settings.ACTION_HOME_SETTINGS)
                else -> return false
            }
            return try {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(i)
                true
            } catch (e: Exception) {
                false
            }
        }

        @JavascriptInterface
        fun getApps(): String {
            val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val pm = packageManager
            val list = pm.queryIntentActivities(query, 0)
                .filter { it.activityInfo.packageName != packageName }
                .map { Pair(it.loadLabel(pm).toString(), it.activityInfo.packageName) }
                .sortedBy { it.first.lowercase() }
            val arr = JSONArray()
            for ((label, pkg) in list) {
                arr.put(JSONObject().put("label", label).put("pkg", pkg))
            }
            return arr.toString()
        }

        @JavascriptInterface
        fun launchApp(pkg: String) {
            val i = packageManager.getLaunchIntentForPackage(pkg) ?: return
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
    }
}
