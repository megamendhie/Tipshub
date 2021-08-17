package com.sqube.tipshub

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class NewsStoryActivity : AppCompatActivity() {
    private var url: String? = null
    private val SAVED_URL = "saved_url"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val savedInstanceS = savedInstanceState
        url = if (savedInstanceState != null) savedInstanceState.getString(SAVED_URL) else intent.getStringExtra("url")
        setContentView(R.layout.activity_news_story)
        val webView = findViewById<WebView>(R.id.wvNews)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        Log.i("News", "onCreate: $url")
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.useWideViewPort = true
        webView.settings.domStorageEnabled = true
        webView.settings.pluginState = WebSettings.PluginState.ON
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }
        webView.loadUrl(url!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(SAVED_URL, url)
        super.onSaveInstanceState(outState)
    }
}