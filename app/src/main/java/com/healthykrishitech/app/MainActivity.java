package com.healthykrishitech.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;

public class MainActivity extends Activity {
    private WebView webView;
    private FrameLayout root;
    private View splashView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new FrameLayout(this);
        setContentView(root);

        webView = new WebView(this);
        webView.setVisibility(View.INVISIBLE);
        root.addView(webView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
        showLockedSplash();
    }

    private int rawId(String name) {
        return getResources().getIdentifier(name, "raw", getPackageName());
    }

    private void showLockedSplash() {
        FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.rgb(3,32,18));

        ImageView background = new ImageView(this);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int splashId = rawId("hk_splash_locked");
        if(splashId != 0) background.setImageResource(splashId);
        splash.addView(background, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView logo = new ImageView(this);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int logoId = rawId("hk_logo_locked");
        if(logoId != 0) logo.setImageResource(logoId);
        logo.setAlpha(0f);
        logo.setScaleX(0.42f);
        logo.setScaleY(0.42f);
        logo.setTranslationZ(24f);

        int size=(int)(getResources().getDisplayMetrics().widthPixels*0.68f);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(size,size);
        lp.gravity=Gravity.CENTER;
        splash.addView(logo,lp);

        root.addView(splash,new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        splashView=splash;

        logo.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(1500)
            .setInterpolator(new AccelerateDecelerateInterpolator()).start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            webView.setVisibility(View.VISIBLE);
            webView.setAlpha(0f);
            webView.animate().alpha(1f).setDuration(450).start();
            splash.animate().alpha(0f).setDuration(450).withEndAction(() -> {
                root.removeView(splash);
                splashView=null;
            }).start();
        },4000);
    }

    @Override public void onBackPressed(){
        if(splashView!=null) return;
        if(webView!=null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
