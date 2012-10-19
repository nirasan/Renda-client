package com.example.renda;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
    
    private ProgressDialog progressDialog;
    String tag = "LoginActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        ((Button)findViewById(R.id.button1)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                // ロード中のダイアログを表示
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(true);
                progressDialog.setMessage("ロード中");
                progressDialog.show();
                
                // バックグラウンドでサーバー通信をおこなう
                new AsyncTask<Void, Void, Void>() {
                    
                    @Override
                    protected Void doInBackground(Void...voids) {
                        
                        // ユーザー名とパスワードを入力欄から取得
                        String username = ((EditText)findViewById(R.id.editTextUsername)).getText().toString();
                        String password = ((EditText)findViewById(R.id.editTextPassword)).getText().toString();
                        
                        // 取得できなければ通信しない
                        if (username.equals("") && password.equals("")) {
                            return null;
                        }
                        
                        // ユーザーの最新情報を参照するURLの作成
                        Uri.Builder builder = new Uri.Builder();
                        builder.scheme("http");
                        builder.encodedAuthority("10.0.2.2:3000");
                        builder.path("/user/show");
                        builder.appendQueryParameter("username", username);
                        builder.appendQueryParameter("password", password);
                        
                        // ユーザーの最新情報を参照
                        HttpGet request = new HttpGet(builder.build().toString());
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();
                        
                        try {
                            String responseBody = httpClient.execute(request, responseHandler);
                            JSONObject jsonObject = new JSONObject(responseBody);
                            // ユーザー情報をプリファレンスに保存
                            SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
                            Editor editor = pref.edit();
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putInt("score", jsonObject.getInt("score"));
                            // メイン画面に遷移
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        catch (ClientProtocolException e) {
                            // ユーザーが存在しなければエラーの表示
                            int status = ((HttpResponseException)e).getStatusCode();
                            if (status == 404) {
                                //((TextView)findViewById(R.id.textView1)).setText("user not found");
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            httpClient.getConnectionManager().shutdown();
                        }
                        return null;
                    }
                    
                    @Override
                    protected void onPostExecute(Void v) {
                        // 処理が終わったのでロード中のダイアログを終了させる
                        progressDialog.dismiss();
                    }
                }.execute();
            }
        });
    }
}
