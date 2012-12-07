package com.example.renda;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuthLoginWebViewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Allow the title bar to show loading progress.
        requestWindowFeature(Window.FEATURE_PROGRESS);
        
        WebView webview = new WebView(this);
        setContentView(webview);
        
        // Enable JavaScript.
        webview.getSettings().setJavaScriptEnabled(true);
        
        webview.setWebChromeClient(new WebChromeClient() {
            // Show loading progress in activity's title bar.
            @Override
            public void onProgressChanged(WebView view, int progress) {
                setProgress(progress * 100);
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            // When start to load page, show url in activity's title bar
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setTitle(url);
            }
             
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
                // Get the cookie from cookie jar.
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie == null) {
                    return;
                }
                // Cookie is a string like NAME=VALUE [; NAME=VALUE]
                Intent result = new Intent();
                String[] pairs = cookie.split(";\\s?");
                for (int i = 0; i < pairs.length; ++i) {
                    String[] parts = pairs[i].split("=", 2);
                    
                    // If token is found, return it to the calling activity.
                    if (parts.length == 2) {
                        for (String s : Arrays.asList("access_token", "mail_address")) {
                            Log.v("onOAuthLoginWebViewActivity 1",s);
                            if (parts[0].equalsIgnoreCase(s)) {
                                try {
                                    result.putExtra(s, URLDecoder.decode(parts[1], "utf8"));
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    if (result.hasExtra("access_token") && result.hasExtra("mail_address")) {
                        setResult(RESULT_OK, result);
                        finish();
                    }
                }
            }
        });

        // Load the page
        Intent intent = getIntent();
        if (intent.getData() != null) {
            webview.loadUrl(intent.getDataString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_oauth_login_web_view, menu);
        return true;
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }
}
