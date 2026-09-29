package com.healthykrishitech.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private WebView webView;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(url.startsWith("http://") || url.startsWith("https://")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; }
                catch(Exception e){ return false; }
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
    }
    @Override public void onBackPressed(){
        if(webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
