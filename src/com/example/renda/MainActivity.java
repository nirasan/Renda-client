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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private int count;
    private int score;
    private String username;
    private String password;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        count    = 0;
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        score    = preferences.getInt("score", 0);
        username = preferences.getString("username", "");
        password = preferences.getString("password", "");
        
        ((TextView)findViewById(R.id.textViewUsername)).setText(username);
        updateScore();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:
                //TODO: プリファレンスの初期化
                startActivity(new Intent(MainActivity.this, LogoActivity.class));
                break;
            case R.id.menu2:
                startActivity(new Intent(MainActivity.this, RankingActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    public void countButtonOnClick(View v) {
        count++;
        updateScore();
    }
    
    public void sendButtonOnClick(View v) {
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ユーザー情報を更新する
                String uri = UriBuilder.user_edit_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("username", username);
                param.put("password", password);
                param.put("score", String.valueOf(count));
                Http.Result result = Http.Client.request("POST", uri, param);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 更新に成功したらユーザー情報を保存してメイン画面へ
                    case HttpStatus.SC_OK:
                        try {
                            // スコア更新
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            score = jsonObject.getInt("score");
                            Editor editor = preferences.edit();
                            editor.putInt("score", score);
                            editor.commit();
                            // 特典表示
                            Toast.makeText(MainActivity.this, "Score +"+String.valueOf(count), Toast.LENGTH_SHORT).show();
                            // カウント初期化
                            count = 0;
                            // 表示更新
                            updateScore();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    // 失敗したらエラーの表示
                    case HttpStatus.SC_NOT_FOUND:
                        Toast.makeText(MainActivity.this, "Send failed", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }
    
    public void rankingButtonOnClick(View v) {
        startActivity(new Intent(MainActivity.this, RankingActivity.class));
    }
    
    private void updateScore() {
        ((TextView)findViewById(R.id.textViewCount)).setText("(" + String.valueOf(count)+ ")");
        ((TextView)findViewById(R.id.textViewScore)).setText(String.valueOf(score));
    }
}
