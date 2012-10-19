package com.example.renda;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
    
    public void loginButtonOnClick(View v) {
        
        final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        
        // ユーザー名とパスワードを入力欄から取得
        final String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
        
        // 取得できなければエラーの表示
        if (username.equals("") && password.equals("")) {
            ((TextView)findViewById(R.id.textView1)).setText("user not found");
            return;
        }
        
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
                            Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            editor.putInt("score", jsonObject.getInt("score"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        break;
                    // ユーザーがいなかったらエラーの表示
                    case HttpStatus.SC_NOT_FOUND:
                        ((TextView)findViewById(R.id.textView1)).setText("user not found");
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }
}
