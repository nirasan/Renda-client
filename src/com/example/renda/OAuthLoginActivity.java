package com.example.renda;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.webkit.CookieSyncManager;

public class OAuthLoginActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_login);
        
        CookieSyncManager.createInstance(getApplicationContext());
        
        Intent intent = new Intent(this, OAuthLoginWebViewActivity.class);
        intent.setData(Uri.parse("http://mother.example.com:3000/oauth/index"));
        startActivityForResult(intent, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_oauth_login, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode != RESULT_OK || data == null) {
                    return;
                }
                // Get the token.
                String access_token = data.getStringExtra("access_token");
                String mail_address = data.getStringExtra("mail_address");
                if (access_token != null && mail_address != null) {
                    /* Use the token to access data */
                    Log.v("onActivityResult",access_token);
                    Log.v("onActivityResult",mail_address);
                  
                    final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                    Editor editor = preferences.edit();
                    editor.putString("access_token", access_token);
                    editor.putString("mail_address", mail_address);
                    editor.commit();
                    
                    startActivity(new Intent(OAuthLoginActivity.this, UserLoginActivity.class));
                }
                return;
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }
}
