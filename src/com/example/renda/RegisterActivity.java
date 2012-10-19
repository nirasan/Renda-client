package com.example.renda;

import java.util.HashMap;

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

public class RegisterActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void registerButtonOnClick(View v) {
        
        // ユーザー名とパスワードを入力欄から取得
        final String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
        final String password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
        
        // 取得できなければエラーの表示
        if (username.equals("") && password.equals("")) {
            ((TextView)findViewById(R.id.textView1)).setText("please input username and password");
            return;
        }
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザーを作成する
                String uri = UriBuilder.user_add_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("username", username);
                param.put("password", password);
                Http.Result result = Http.Client.request("POST", uri, param);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 作成に成功したらユーザー情報を保存してメイン画面へ
                    case HttpStatus.SC_OK:
                        try {
                            SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                            Editor editor = preferences.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            editor.putInt("score", jsonObject.getInt("score"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        break;
                    // 失敗したらエラーの表示
                    case HttpStatus.SC_NOT_FOUND:
                        ((TextView)findViewById(R.id.textView1)).setText("invalid username or password");
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }
}
