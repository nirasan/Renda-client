package com.example.renda;

import java.util.HashMap;
import java.util.List;
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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class AccelerometeraSensorActivity extends Activity {

    private int count;
    private int game_time;
    private String access_token;
    private String mail_address;
    private SharedPreferences preferences;    
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometera_sensor);
        
        count    = 0;
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        mail_address = preferences.getString("mail_address", "");
        access_token = preferences.getString("access_token", "");
        game_time    = preferences.getInt("game_time", 5);

        findTextViewById(R.id.textViewTime).setText("0.0");
        
        imageView = (ImageView)findViewById(R.id.imageView1);
        
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            private int lastZ = 0;
            private boolean lastIsPositive = true;
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    int currZ = (int)event.values[2] / 3;
                    boolean isPositive = lastZ >= currZ ? true : false;
                    boolean isReturn = lastIsPositive && !isPositive ? true : false;
                    
                    TextView textViewTime = findTextViewById(R.id.textViewTime);
                    float fTime = Float.valueOf(textViewTime.getText().toString());
                    fTime = Float.valueOf(String.format("%.1f", fTime));
                    
                    // 残りタイムが有り、前進から後退へ切り替わったタイミングでカウントをインクリメントする
                    if (fTime > 0.0f && isReturn) {
                        count++;
                        findTextViewById(R.id.textViewCount).setText("(" + String.valueOf(count)+ ")");
                    }
                    
                    int drawable_id = isPositive ? R.drawable.hato_pogi : R.drawable.hato_nega;
                    imageView.setImageResource(drawable_id);
                    
                    lastZ = currZ;
                    lastIsPositive = isPositive;
                    
                    //for debug
                    findTextViewById(R.id.textViewZ).setText(String.valueOf(currZ));
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub
            }
        };
        
        // 開始ダイアログの表示
        showStartDialog();
    }

    // 開始ダイアログ
    private void showStartDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccelerometeraSensorActivity.this);
        alertDialogBuilder.setTitle("用意はいいですか？");
        //alertDialogBuilder.setMessage("メッセージ");
        alertDialogBuilder.setPositiveButton(
                "スタート！",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 残りタイムの追加
                        findTextViewById(R.id.textViewTime).setText(String.valueOf(game_time) + ".0");
                        // センサーでカウントアップ開始
                        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                        if (sensors.size() > 0) {
                            Sensor s = sensors.get(0);
                            sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_UI);
                        }
                        // タイマーの開始
                        startTimer();
                    }
                }
        );
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
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
        // センサーによるカウントアップ停止
        sensorManager.unregisterListener(sensorEventListener);
        
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccelerometeraSensorActivity.this);
        alertDialogBuilder.setTitle("おつかれさまでした！");
        //alertDialogBuilder.setMessage("メッセージ");
        alertDialogBuilder.setPositiveButton(
                "結果を送信する",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResultToServer();
                    }
                }
        );
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
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
                            int level = jsonObject.getInt("level");
                            int total_count = jsonObject.getInt("total_count");
                            int next_level_count = jsonObject.getInt("next_level_count");
                            // 終了ダイアログの表示
                            String title = "送信しました";
                            String message =  "発電量:" + count;
                                   message += "\n総発電量:" + total_count;
                            if (next_level_count != 0) {
                                   message += "/" + next_level_count;
                            }
                            if (jsonObject.has("rankin")) {
                                   message += "\nランキング入りしました！";
                            }
                            if (jsonObject.has("levelup")) {
                                   message += "\nレベルが上がりました！";
                            }
                            showFinishDialog(title, message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Toast.makeText(AccelerometeraSensorActivity.this, "something error happens.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }.execute();
    }

    // 終了ダイアログの表示
    private void showFinishDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccelerometeraSensorActivity.this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(
                "ランキングへ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(AccelerometeraSensorActivity.this, RankingSpinnerActivity.class));
                    }
                }
        );
        alertDialogBuilder.setNegativeButton(
                "メニューへ",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(AccelerometeraSensorActivity.this, MainMenuActivity.class));
                    }
                }
        );
        alertDialogBuilder.setCancelable(true);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // アラートダイアログを表示します
        alertDialog.show();
    }
    
    private TextView findTextViewById(int id) {
        return (TextView)findViewById(id);
    }
    
    /*
    @Override
    protected void onResume() {
        super.onResume();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            sensorManager.registerListener(sensorEventListener, s, SensorManager.SENSOR_DELAY_UI);
        }
    }
    */
    
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_accelerometera_sensor, menu);
        return true;
    }
}
