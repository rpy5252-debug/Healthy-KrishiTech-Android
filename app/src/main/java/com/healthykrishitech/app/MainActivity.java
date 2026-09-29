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

    private void installMenuActions(WebView view){
        String js =
        "(function(){const d=document.getElementById('sideDrawer');if(!d||d.dataset.hkBound)return;d.dataset.hkBound='1';"+
        "const bs=[...d.querySelectorAll('button')],close=()=>{d.classList.remove('open');document.getElementById('drawerBackdrop')?.classList.remove('open')},pop=(t,m)=>{close();setTimeout(()=>alert(t+'\\n\\n'+m),80)},by=t=>bs.find(b=>b.textContent.includes(t));"+
        "by('Profile')?.addEventListener('click',()=>{let s=null;try{s=JSON.parse(localStorage.getItem('hk_sb_session')||'null')}catch(e){};const u=s?.user||{},m=u.user_metadata||{};pop('Profile','Name: '+(m.name||'Customer')+'\\nEmail: '+(u.email||'—')+'\\nMobile: '+(m.mobile||'—'))});"+
        "by('My Purchases')?.addEventListener('click',async()=>{close();let s=null;try{s=JSON.parse(localStorage.getItem('hk_sb_session')||'null')}catch(e){};if(!s?.user?.id){pop('My Purchases','Login required.');return}try{const r=await fetch(SB_URL+'/rest/v1/subscriptions?select=status,expires_at,plan_months&user_id=eq.'+encodeURIComponent(s.user.id),{headers:{apikey:SB_KEY,Authorization:'Bearer '+s.access_token}});if(!r.ok)throw Error('HTTP '+r.status);const rows=await r.json();pop('My Purchases',rows.length?rows.map((x,i)=>(i+1)+'. '+(x.status||'unknown')+' • '+(x.plan_months||'—')+' month • Valid till '+(x.expires_at?new Date(x.expires_at).toLocaleDateString('en-IN'):'—')).join('\\n'):'अभी कोई active purchase नहीं मिला।')}catch(e){pop('My Purchases','Data load नहीं हुआ: '+e.message)}});"+
        "by('Progress Report')?.addEventListener('click',()=>pop('Progress Report','Progress Report module अभी backend records से connected नहीं है।'));"+
        "by('Rate App')?.addEventListener('click',()=>pop('Rate App','Play Store listing publish होने के बाद rating link activate होगा।'));"+
        "const share=async()=>{close();const t='Healthy KrishiTech — जीवन का आधार देश का किसान. किसानों के लिए उपयोगी agriculture calculators.';try{if(navigator.share){await navigator.share({title:'Healthy KrishiTech',text:t});return}}catch(e){}pop('Share App',t)};"+
        "by('Refer & Earn')?.addEventListener('click',share);by('Share App')?.addEventListener('click',share);"+
        "by('Refund Policy')?.addEventListener('click',()=>pop('Refund Policy','Approved final Refund Policy text अभी app source में उपलब्ध नहीं है।'));"+
        "by('WhatsApp Support')?.addEventListener('click',()=>pop('WhatsApp Support','Support WhatsApp number अभी app source में configured नहीं है।'));})();";
        view.evaluateJavascript(js,null);
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
