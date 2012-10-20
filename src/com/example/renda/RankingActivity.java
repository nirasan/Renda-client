package com.example.renda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends Activity {
    
    private ArrayList<HashMap<String, Integer>> userdatas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);
        
        userdatas = new ArrayList<HashMap<String,Integer>>();
        
        new AsyncTaskWithDialog<Http.Result>(this) {
            
            @Override
            protected Http.Result doInBackground(Void...voids) {
                // ランキング情報を取得
                String uri = UriBuilder.user_ranking_url();
                Http.Result result = Http.Client.request("GET", uri);
                return result;
            }
            
            @Override
            protected void onPostExecuteWithDismissDialog(Http.Result result) {
                switch (result.statusCode) {
                    case HttpStatus.SC_OK:
                        try {
                            // ランキング情報の変換
                            JSONArray jsonArray = new JSONArray(result.responseBody);
                            int length = jsonArray.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String username = jsonObject.getString("username");
                                int score = jsonObject.getInt("score");
                                HashMap<String, Integer> userdata = new HashMap<String, Integer>();
                                userdata.put(username, score);
                                userdatas.add(userdata);
                            }
                            // ランキングのリスト表示
                            ArrayAdapter<HashMap<String, Integer>> adapter = new UserdataAdapter(RankingActivity.this, R.layout.ranking_row, userdatas);
                            ListView listView = (ListView)findViewById(R.id.listView1);
                            listView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    // 失敗したらエラーの表示
                    case HttpStatus.SC_NOT_FOUND:
                        Toast.makeText(RankingActivity.this, "load failed", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }.execute();
    }
    
    private class UserdataAdapter extends ArrayAdapter<HashMap<String, Integer>> {
        
        private ArrayList<HashMap<String, Integer>> items;
        private LayoutInflater inflater;
        
        public UserdataAdapter(Context context, int textViewResourceId, ArrayList<HashMap<String, Integer>> items) {  
            super(context, textViewResourceId, items);  
            this.items = items;  
            this.inflater = (LayoutInflater) context  
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        }
        
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            // ビューを受け取る  
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成  
                view = inflater.inflate(R.layout.ranking_row, null);
            }
            // 表示すべきデータの取得
            HashMap<String, Integer> item = items.get(position);
            for (Map.Entry<String, Integer> entry : item.entrySet()) {
                ((TextView)view.findViewById(R.id.textViewRankingRank)).setText(String.valueOf(position + 1)+"位");
                ((TextView)view.findViewById(R.id.textViewRankingName)).setText( entry.getKey() + "(Score:" + String.valueOf(entry.getValue()) + ")");
            }
            return view;
        }  
    }
}
