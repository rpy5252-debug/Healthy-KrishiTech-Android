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
import android.webkit.JavascriptInterface;

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
        webView.addJavascriptInterface(new Object(){
            @JavascriptInterface public void openAsset(String file){
                // Only allow navigation from our bundled app pages, never from a remote website.
                String currentUrl = webView.getUrl();
                if(currentUrl == null || !currentUrl.startsWith("file:///android_asset/")) return;
                // Restrict the bridge to the HTML assets that are actually part of this app.
                if(file == null || !(file.equals("index.html")
                    || file.equals("cd-calculator.html")
                    || file.equals("krishak-anudan.html")
                    || file.equals("lt-calculator.html")
                    || file.equals("lt-summary.html")
                    || file.equals("soil-mb.html"))) return;
                runOnUiThread(() -> webView.loadUrl("file:///android_asset/" + file));
            }
        }, "HKAndroid");
        webView.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(url == null) return false;
                if(url.startsWith("file:///android_asset/")) return false;
                if(url.startsWith("content://") && (url.contains("lt-calculator.html") || url.contains("lt-summary.html") || url.contains("krishak-anudan.html") || url.contains("soil-mb.html") || url.contains("cd-calculator.html"))){
                    String f = url.contains("lt-summary.html") ? "lt-summary.html" : (url.contains("krishak-anudan.html") ? "krishak-anudan.html" : (url.contains("lt-calculator.html") ? "lt-calculator.html" : (url.contains("soil-mb.html") ? "soil-mb.html" : "cd-calculator.html")));
                    view.loadUrl("file:///android_asset/" + f); return true;
                }
                if(url.startsWith("http://") || url.startsWith("https://")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true; }
                catch(Exception e){ return false; }
            }
            @Override public void onPageFinished(WebView view, String url){
                super.onPageFinished(view, url);
                if(url != null && url.contains("index.html")){
                    view.postDelayed(() -> { installMenuActions(view); }, 300);
                }
            }
        });
        webView.loadUrl("file:///android_asset/index.html");
        showLockedSplash();
    }

    private void installMenuActions(WebView view){
        String js =
        "(function(){const d=document.getElementById('sideDrawer');if(!d||d.dataset.hkBound)return;d.dataset.hkBound='1';"+
        "const bs=[...d.querySelectorAll('button')],close=()=>{d.classList.remove('open');document.getElementById('drawerBackdrop')?.classList.remove('open')},pop=(t,m)=>{close();let o=document.getElementById('hkMenuModal');if(o)o.remove();o=document.createElement('div');o.id='hkMenuModal';o.style.cssText='position:fixed;inset:0;z-index:999999;background:rgba(0,0,0,.52);display:flex;align-items:center;justify-content:center;padding:22px';let c=document.createElement('div');c.style.cssText='width:min(92vw,460px);max-height:78vh;overflow:auto;background:#fff;border-radius:22px;padding:22px;box-shadow:0 18px 55px rgba(0,0,0,.28);font-family:Arial,sans-serif';let h=document.createElement('div');h.textContent=t;h.style.cssText='font-size:22px;font-weight:800;color:#075c32;margin-bottom:14px';let b=document.createElement('div');b.textContent=m;b.style.cssText='white-space:pre-line;font-size:17px;line-height:1.55;color:#183c2b';let k=document.createElement('button');k.textContent='OK';k.style.cssText='width:100%;margin-top:20px;border:0;border-radius:14px;padding:13px;background:#087533;color:#fff;font-size:17px;font-weight:700';k.onclick=()=>o.remove();c.append(h,b,k);o.append(c);o.onclick=e=>{if(e.target===o)o.remove()};document.body.append(o)},by=t=>bs.find(b=>b.textContent.includes(t));"+
        "by('Profile')?.addEventListener('click',()=>{let s=null;try{s=JSON.parse(localStorage.getItem('hk_sb_session')||'null')}catch(e){};const u=s?.user||{},m=u.user_metadata||{};pop('Profile','Name: '+(m.name||m.full_name||m.fullName||'Customer')+'\\nEmail: '+(u.email||'—')+'\\nMobile: '+(m.mobile||m.phone||u.phone||'—'))});"+
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
        },3000);
    }

    @Override public void onBackPressed(){
        if(splashView!=null) return;
        if(webView!=null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
