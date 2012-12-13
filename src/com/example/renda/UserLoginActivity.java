package com.example.renda;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

public class UserLoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        
        final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        
        // ユーザー名とパスワードをプリファレンスから取得
        final String access_token = preferences.getString("access_token", "");
        final String mail_address = preferences.getString("mail_address", "");
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザーの最新情報をサーバーに問い合わせる
                String uri = UriBuilder.user_exist_url(mail_address, access_token);
                Http.Result result = Http.Client.request("GET", uri);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // mail_address, access_tokenともに正しく問い合わせに成功したらユーザー情報を更新してメイン画面へ
                    case HttpStatus.SC_OK:
                        /*
                        // cookie経由でアクセストークンの更新
                        if (result.cookieStore != null) {
                            List<Cookie> cookies = result.cookieStore.getCookies();
                            for (Cookie cookie : cookies) {
                                if (cookie.getName().equals("access_token")) {
                                    Log.v("UserLogin set cookie", cookie.getValue());
                                    Editor editor = preferences.edit();
                                    editor.putString("access_token", cookie.getValue());
                                    editor.commit();
                                }
                            }
                        }
                        */
                        startActivity(new Intent(UserLoginActivity.this, MainActivity.class));
                        break;
                        /*
                        try {
                            Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            editor.putInt("score", jsonObject.getInt("score"));
                            editor.commit();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        break;
                        */
                    // ユーザーがいなかったらユーザー登録画面へ
                    case HttpStatus.SC_NOT_FOUND:
                        startActivity(new Intent(UserLoginActivity.this, RegisterActivity.class));
                        break;
                        /*
                        ((TextView)findViewById(R.id.textView1)).setText("user not found");
                        break;
                        */
                    // メールアドレスは存在するがアクセストークンが不正な場合
                    case HttpStatus.SC_BAD_REQUEST:
                        // プリファレンスを空に
                        Editor editor = preferences.edit();
                        editor.clear();
                        editor.commit();
                        // OAuth認証をやりなおす
                        startActivity(new Intent(UserLoginActivity.this, OAuthLoginActivity.class));
                        break;
                    default:
                        Toast.makeText(UserLoginActivity.this, "Send failed", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_login, menu);
        return true;
    }
}
