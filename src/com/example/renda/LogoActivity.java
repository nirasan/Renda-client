package com.example.renda;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class LogoActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
    }

    public void startButtonOnClick(View v) {
        
        final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        
        // ユーザー名とパスワードをプリファレンスから取得
        final String username = preferences.getString("username", "");
        final String password = preferences.getString("password", "");
        
        // 取得できなければログイン画面へ
        if (username.equals("") && password.equals("")) {
            startActivity(new Intent(LogoActivity.this, LoginActivity.class));
        }
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザーの最新情報をサーバーに問い合わせる
                String uri = UriBuilder.user_show_url(username, password);
                Http.Result result = Http.Client.getRequest(uri);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 問い合わせに成功したらユーザー情報を更新してメイン画面へ
                    case HttpStatus.SC_OK:
                        try {
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            preferences.edit().putInt("score", jsonObject.getInt("score"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(LogoActivity.this, MainActivity.class));
                        break;
                    // ユーザーがいなかったらログイン画面へ
                    case HttpStatus.SC_NOT_FOUND:
                        startActivity(new Intent(LogoActivity.this, LoginActivity.class));
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }
}
