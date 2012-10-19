package com.example.renda;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LogoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        
        final SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        
        ((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
            

            
            @Override
            public void onClick(View v) {
                String username = pref.getString("username", "");
                String password = pref.getString("password", "");
                if (username.equals("") && password.equals("")) {
                    Intent intent = new Intent(LogoActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http");
                builder.encodedAuthority("10.0.2.2:3000");
                builder.path("/user/show");
                builder.appendQueryParameter("username", username);
                builder.appendQueryParameter("password", password);
                
                HttpGet request = new HttpGet(builder.build().toString());
                DefaultHttpClient httpClient = new DefaultHttpClient();
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                try {
                    String responseBody = httpClient.execute(request, responseHandler);
                    JSONObject jsonObject = new JSONObject(responseBody);
                    int score = jsonObject.getInt("score");
                    pref.edit().putInt("score", score);
                }
                catch (ClientProtocolException e) {
                    int status = ((HttpResponseException)e).getStatusCode();
                    if (status == 404) {
                        Intent intent = new Intent(LogoActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finally {
                    httpClient.getConnectionManager().shutdown();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_logo, menu);
        return true;
    }
}
