package com.example.renda;

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
        final String access_token = preferences.getString("access_token", "");
        final String mail_address = preferences.getString("mail_address", "");
        
        // 取得できなければOAuthログイン画面へ
        if (access_token.equals("") && mail_address.equals("")) {
            startActivity(new Intent(LogoActivity.this, OAuthLoginActivity.class));
        }
        // 取得できればユーザーログイン画面へ
        else {
            startActivity(new Intent(LogoActivity.this, UserLoginActivity.class));
        }
        
        /* TODO: UserLoginActivity に移動
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザーの最新情報をサーバーに問い合わせる
                String uri = UriBuilder.user_show_url(username, password);
                Http.Result result = Http.Client.request("GET", uri);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 問い合わせに成功したらユーザー情報を更新してメイン画面へ
                    case HttpStatus.SC_OK:
                        try {
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            Editor editor = preferences.edit();
                            editor.putInt("score", jsonObject.getInt("score"));
                            editor.commit();
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
        */
    }
}
