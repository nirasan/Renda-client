package com.example.renda;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private int count;
    private int life;
    private int used_life;
    private String username;
    private String access_token;
    private String mail_address;
    private SharedPreferences preferences;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        count    = 0;
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        username = preferences.getString("username", "");
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
                            life = jsonObject.getInt("life");
                            used_life = 0;
                            // 表示更新
                            updateView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "something error happens.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
        
        // タイマーで残り時間があれば減らす
        final Timer mTimer = new Timer(true);
        final Handler mHandler = new Handler();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView textViewTime = findTextViewById(R.id.textViewTime);
                        float fTime = Float.valueOf(textViewTime.getText().toString());
                        fTime = Float.valueOf(String.format("%.1f", fTime));
                        if (fTime > 0.0f) {
                            fTime -= 0.1;
                            textViewTime.setText(String.format("%.1f", fTime));
                        }
                    }
                });
            }
        }, 100, 100);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // ログアウト
            case R.id.menu1:
                SharedPreferences preferences = getSharedPreferences("user", MODE_PRIVATE);
                Editor editor = preferences.edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(MainActivity.this, LogoActivity.class));
                break;
            // ランキング
            case R.id.menu2:
                startActivity(new Intent(MainActivity.this, RankingActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    // カウントボタン押下時に残り時間があればカウントアップ
    public void countButtonOnClick(View v) {
        TextView textViewTime = findTextViewById(R.id.textViewTime);
        float fTime = Float.valueOf(textViewTime.getText().toString());
        fTime = Float.valueOf(String.format("%.1f", fTime));
        if (fTime > 0.0f) {
            count++;
            updateView();
        }
    }
    
    // スタートボタン押下で残り時間を増やしてカウント可能な状態開始
    public void startButtonOnClick(View v) {
        if (life > 0) {
            // 残りタイムの加算
            TextView textViewTime = findTextViewById(R.id.textViewTime);
            float fTime = Float.valueOf(textViewTime.getText().toString());
            fTime += 5.0f;
            textViewTime.setText(String.valueOf(fTime));
            // ライフの消費
            life--;
            used_life++;
            updateView();
        }
    }
    
    // 送信ボタンでカウントの送信
    public void sendButtonOnClick(View v) {
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // カウントの送信
                String uri = UriBuilder.user_update_count_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("mail_address", mail_address);
                param.put("access_token", access_token);
                param.put("count",        String.valueOf(count));
                param.put("used_life",    String.valueOf(used_life));
                Http.Result result = Http.Client.request("POST", uri, param);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    // 更新に成功したらユーザー情報を保存してメイン画面へ
                    case HttpStatus.SC_OK:
                        try {
                            // ステータス更新
                            JSONObject jsonObject = new JSONObject(result.responseBody);
                            life = jsonObject.getInt("life");
                            used_life = 0;
                            // カウント初期化
                            count = 0;
                            // 表示更新
                            updateView();
                            // ステータス表示
                            if (jsonObject.has("rankin")) {
                                Toast.makeText(MainActivity.this, "ランキング入りしました", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "送信しました", Toast.LENGTH_LONG).show();                                
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "something error happens.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
    }
    
    public void rankingButtonOnClick(View v) {
        //startActivity(new Intent(MainActivity.this, RankingActivity.class));
    }
    
    private void updateView() {
        findTextViewById(R.id.textViewUsername).setText(username);
        findTextViewById(R.id.textViewCount).setText("(" + String.valueOf(count)+ ")");
        findTextViewById(R.id.textViewLife).setText(String.valueOf(life));
    }
    
    private TextView findTextViewById(int id) {
        return (TextView)findViewById(id);
    }
}
