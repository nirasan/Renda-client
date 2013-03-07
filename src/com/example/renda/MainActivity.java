package com.example.renda;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private int count;
    private String access_token;
    private String mail_address;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        count    = 0;
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        mail_address = preferences.getString("mail_address", "");
        access_token = preferences.getString("access_token", "");

        findTextViewById(R.id.textViewTime).setText("5.0");
        
        // 開始ダイアログの表示
        showStartDialog();
    }

    // 開始ダイアログ
    private void showStartDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // アラートダイアログのタイトルを設定します
        alertDialogBuilder.setTitle("用意はいいですか？");
        // アラートダイアログのメッセージを設定します
        //alertDialogBuilder.setMessage("メッセージ");
        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton(
                "スタート！",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startTimer();
                    }
                }
        );
        // アラートダイアログのキャンセルが可能かどうかを設定します
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // アラートダイアログを表示します
        alertDialog.show();
    }
    
    // タイマーの開始
    private void startTimer() {
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
                        } else {
                            showSendDialog();
                            mTimer.cancel();
                        }
                    }
                });
            }
        }, 100, 100);
    }
    
    // カウントボタン押下時に残り時間があればカウントアップ
    public void countButtonOnClick(View v) {
        TextView textViewTime = findTextViewById(R.id.textViewTime);
        float fTime = Float.valueOf(textViewTime.getText().toString());
        fTime = Float.valueOf(String.format("%.1f", fTime));
        if (fTime > 0.0f) {
            count++;
            findTextViewById(R.id.textViewCount).setText("(" + String.valueOf(count)+ ")");
        }
    }

    // 結果送信ダイアログの表示
    private void showSendDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // アラートダイアログのタイトルを設定します
        alertDialogBuilder.setTitle("おつかれさまでした！");
        // アラートダイアログのメッセージを設定します
        //alertDialogBuilder.setMessage("メッセージ");
        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton(
                "結果を送信する",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResultToServer();
                    }
                }
        );
        // アラートダイアログのキャンセルが可能かどうかを設定します
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // アラートダイアログを表示します
        alertDialog.show();
    }
    
    // 送信ボタンで結果をサーバーへ送信
    public void sendResultToServer() {
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // カウントの送信
                String uri = UriBuilder.user_update_count_url();
                HashMap<String, String> param = new HashMap<String, String>();
                param.put("mail_address", mail_address);
                param.put("access_token", access_token);
                param.put("count",        String.valueOf(count));
                param.put("used_life",    "1");
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
                            int life = jsonObject.getInt("life");
                            Editor editor = preferences.edit();
                            editor.putInt("life", life);
                            editor.commit();
                            // 終了ダイアログの表示
                            String message = jsonObject.has("rankin") ? "ランキング入りしました" : "送信しました";
                            showFinishDialog(message);
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

    // 終了ダイアログの表示
    private void showFinishDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        // アラートダイアログのタイトルを設定します
        alertDialogBuilder.setTitle(message);
        // アラートダイアログのメッセージを設定します
        //alertDialogBuilder.setMessage("メッセージ");
        // アラートダイアログの肯定ボタンがクリックされた時に呼び出されるコールバックリスナーを登録します
        alertDialogBuilder.setPositiveButton(
                "ランキングへ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, RankingSpinnerActivity.class));
                    }
                }
        );
        alertDialogBuilder.setNegativeButton(
                "メニューへ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
                    }
                }
        );
        // アラートダイアログのキャンセルが可能かどうかを設定します
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // アラートダイアログを表示します
        alertDialog.show();
    }
    
    private TextView findTextViewById(int id) {
        return (TextView)findViewById(id);
    }
}
