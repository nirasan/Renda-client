package com.example.renda;

import java.util.HashMap;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerButtonOnClick(View v) {
        
        // メールアドレスとアクセストークンをプリファレンスから取得
        final SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
        final String access_token = preferences.getString("access_token", "");
        final String mail_address = preferences.getString("mail_address", "");
        
        // ユーザー名を入力欄から取得
        final String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        
        // 取得できなければエラーの表示
        if (username.equals("")) {
            ((TextView)findViewById(R.id.textView1)).setText("please input username");
            return;
        }
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザーを作成する
                String uri = UriBuilder.user_register_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("username", username);
                param.put("mail_address", mail_address);
                param.put("access_token", access_token);
                Http.Result result = Http.Client.request("POST", uri, param);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 作成に成功したらユーザー情報を保存してメイン画面へ
                    case HttpStatus.SC_OK:
                        Editor editor = preferences.edit();
                        editor.putString("username", username);
                        editor.commit();
                        startActivity(new Intent(RegisterActivity.this, MainMenuActivity.class));
                        break;
                    // 失敗したらログインしなおし
                    case HttpStatus.SC_BAD_REQUEST:
                        startActivity(new Intent(RegisterActivity.this, OAuthLoginActivity.class));
                        break;
                    default:
                        Toast.makeText(RegisterActivity.this, "something error happens", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
    }
}
