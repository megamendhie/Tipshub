package com.sqube.tipshub;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class NewsStoryActivity extends AppCompatActivity {

    private String url;
    private String SAVED_URL = "saved_url";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle savedInstanceS = savedInstanceState;
        if(savedInstanceState!=null)
            url = savedInstanceState.getString(SAVED_URL);
        else
            url = getIntent().getStringExtra("url");
        setContentView(R.layout.activity_news_story);
        WebView webView = findViewById(R.id.wvNews);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        Log.i("News", "onCreate: "+url);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_URL, url);
        super.onSaveInstanceState(outState);
    }
}
