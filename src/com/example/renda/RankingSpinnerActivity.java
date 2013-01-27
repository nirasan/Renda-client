package com.example.renda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class RankingSpinnerActivity extends Activity {

    private String access_token;
    private String mail_address;
    private SharedPreferences preferences;

    private ArrayList<Map<String, String>> list_datas;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_spinner);
        
        preferences = getSharedPreferences("user", MODE_PRIVATE);
        mail_address = preferences.getString("mail_address", "");
        access_token = preferences.getString("access_token", "");

        list_datas = new ArrayList<Map<String,String>>();
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // アイテムを追加します
        adapter.add("personal");
        adapter.add("general");
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        // アダプターを設定します
        spinner.setAdapter(adapter);
        
        // スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                
                Spinner spinner = (Spinner) parent;
                final String item = (String) spinner.getSelectedItem();
                
                new AsyncTaskWithDialog<Http.Result>(RankingSpinnerActivity.this) {
                    
                    @Override
                    protected Http.Result doInBackground(Void...voids) {
                        // ランキングの取得
                        String uri = UriBuilder.user_ranking_url(item,mail_address, access_token);
                        Http.Result result = Http.Client.request("GET", uri);
                        return result;
                    }
                    
                    @Override
                    protected void onPostExecuteWithDismissDialog(Http.Result result) {
                        switch (result.statusCode) {
                            case HttpStatus.SC_OK:
                                try {
                                    // ランキングデータの初期化
                                    list_datas.clear();
                                    // ランキング情報の変換
                                    JSONArray jsonArray = new JSONArray(result.responseBody);
                                    int length = jsonArray.length();
                                    for (int i = 0; i < length; i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String username = jsonObject.getString("username");
                                        int count = jsonObject.getInt("count");
                                        int rank = jsonObject.getInt("rank");
                                        String created_at = jsonObject.getString("created_at");
                                        Map<String, String> list_data = new HashMap<String, String>();
                                        list_data.put("title", String.valueOf(rank) + "位: " + username);
                                        list_data.put("sub_title", String.valueOf(count) + " " + created_at);
                                        list_datas.add(list_data);
                                    }
                                    // ランキングのリスト表示
                                    ListView listView = (ListView)findViewById(R.id.listView1); 
                                    SimpleAdapter adapter = new SimpleAdapter(
                                            RankingSpinnerActivity.this, 
                                            list_datas,
                                            android.R.layout.simple_list_item_2,
                                            new String[] { "title", "sub_title" },
                                            new int[] { android.R.id.text1, android.R.id.text2 }
                                    );
                                    listView.setAdapter(adapter);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            // 失敗したらエラーの表示
                            case HttpStatus.SC_NOT_FOUND:
                                Toast.makeText(RankingSpinnerActivity.this, "load failed", Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                    }
                }.execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(RankingSpinnerActivity.this, MainMenuActivity.class));
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ranking_spinner, menu);
        return true;
    }
}
