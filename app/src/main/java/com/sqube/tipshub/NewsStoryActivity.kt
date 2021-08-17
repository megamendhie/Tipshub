package com.sqube.tipshub

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.sqube.tipshub.databinding.ActivityNewsStoryBinding

class NewsStoryActivity : AppCompatActivity() {
    private var url: String? = null
    private val savedUrl = "saved_url"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        url = if (savedInstanceState != null) savedInstanceState.getString(savedUrl) else intent.getStringExtra("url")
        val binding = ActivityNewsStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i("News", "onCreate: $url")
        with(binding){
            wvNews.settings.builtInZoomControls = true
            wvNews.settings.displayZoomControls = false
            wvNews.settings.useWideViewPort = true
            wvNews.settings.domStorageEnabled = true
            wvNews.settings.pluginState = WebSettings.PluginState.ON
            wvNews.settings.javaScriptEnabled = true
            wvNews.settings.setSupportMultipleWindows(true)
            wvNews.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }
            wvNews.loadUrl(url!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(savedUrl, url)
        super.onSaveInstanceState(outState)
    }
}