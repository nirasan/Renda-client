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
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

    int level;
    int game_time;
    int total_count;
    int next_level_count;
    String username;
    String mail_address;
    String access_token;
    SharedPreferences preferences;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_menu);
        
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        username     = preferences.getString("username", "");
        mail_address = preferences.getString("mail_address", "");
        access_token = preferences.getString("access_token", "");

        // ユーザー情報を更新する
        new AsyncTaskWithDialog<Http.Result>(this) {
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザー情報を更新する
                String uri = UriBuilder.user_status_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("mail_address", mail_address);
                param.put("access_token", access_token);
                Http.Result result = Http.Client.request("POST", uri, param);
                return result;
            }
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 更新に成功したらユーザー情報を保存
                    case HttpStatus.SC_OK:
                        try {
                            // ステータス更新
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            level            = jsonObject.getInt("level");
                            game_time        = jsonObject.getInt("game_time");
                            total_count      = jsonObject.getInt("total_count");
                            next_level_count = jsonObject.getInt("next_level_count");
                            Editor editor = preferences.edit();
                            editor.putInt("level", level);
                            editor.putInt("game_time", game_time);
                            editor.putInt("total_count", total_count);
                            editor.putInt("next_level_count", next_level_count);
                            editor.commit();
                            // 表示更新
                            updateView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Toast.makeText(MainMenuActivity.this, "something error happens.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }
    
    public void mainButtonOnClick(View view) {
        //startActivity(new Intent(MainMenuActivity.this, MainActivity.class));
        startActivity(new Intent(MainMenuActivity.this, AccelerometeraSensorActivity.class));
    }
    public void rankingButtonOnClick(View view) {
        startActivity(new Intent(MainMenuActivity.this, RankingSpinnerActivity.class));
    }
    public void logoutButtonOnClick(View view) {
        // プリファレンスを空に
        Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        // OAuth認証をやりなおす
        startActivity(new Intent(MainMenuActivity.this, OAuthLoginActivity.class));
    }
    
    private void updateView() {
        findTextViewById(R.id.textViewUsername).setText(username);
        findTextViewById(R.id.textViewLevel).setText(String.valueOf(level));
        if (next_level_count != 0) {
            findTextViewById(R.id.textViewCount).setText(String.valueOf(total_count) + "/" + String.valueOf(next_level_count));
        } else {
            findTextViewById(R.id.textViewCount).setText(String.valueOf(total_count));
        }
    }
    
    private TextView findTextViewById(int id) {
        return (TextView)findViewById(id);
    }
}
