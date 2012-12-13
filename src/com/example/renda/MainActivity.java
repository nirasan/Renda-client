package com.example.renda;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

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
    private String username;
    private String password;
    private SharedPreferences preferences;
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        count    = 0;
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        username = preferences.getString("username", "");
        password = preferences.getString("password", "");
        
        ((TextView)findViewById(R.id.textViewUsername)).setText(username);
        updateView();
        
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
        TextView textViewTime = findTextViewById(R.id.textViewTime);
        float fTime = Float.valueOf(textViewTime.getText().toString());
        fTime += 5.0f;
        textViewTime.setText(String.valueOf(fTime));
    }
    
    // 送信ボタンでカウントの送信
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
                            //score = jsonObject.getInt("score");
                            Editor editor = preferences.edit();
                            //editor.putInt("score", score);
                            editor.commit();
                            // 特典表示
                            Toast.makeText(MainActivity.this, "Score +"+String.valueOf(count), Toast.LENGTH_SHORT).show();
                            // カウント初期化
                            count = 0;
                            // 表示更新
                            updateView();
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
        //startActivity(new Intent(MainActivity.this, RankingActivity.class));
    }
    
    private void updateView() {
        findTextViewById(R.id.textViewCount).setText("(" + String.valueOf(count)+ ")");
    }
    
    private TextView findTextViewById(int id) {
        return (TextView)findViewById(id);
    }
}
