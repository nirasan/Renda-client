package com.example.renda;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class Http {
    
    public static class Client {
        
        public static Http.Result request(String method, String uri) {
            return request(method, uri, new HashMap<String, String>());
        }
        
        public static Http.Result request(String method, String uri, Map<String, String> param) {
            
            String responseBody = "";
            int statusCode = 0;
            
            // メソッドの検証とリクエストの作成
            if (!method.equals("GET") && !method.equals("POST")) {
                throw new IllegalArgumentException();
            }
            HttpRequestBase request = method.equals("GET")  ? new HttpGet(uri)
                                    :                         new HttpPost(uri);
            
            // POSTでパラメータがあれば設定
            if (method.equals("POST") && !param.isEmpty()) {
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> e : param.entrySet()) {
                    params.add(new BasicNameValuePair(e.getKey(), e.getValue()));
                }
                try {
                    ((HttpPost)request).setEntity(new UrlEncodedFormEntity(params, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            
            // 問い合わせ実行
            DefaultHttpClient httpClient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            CookieStore cookieStore = null;
            try {
                // 問い合わせ成功ならレスポンスのbodyを取得
                responseBody = httpClient.execute(request, responseHandler);
                statusCode = HttpStatus.SC_OK;
                cookieStore= httpClient.getCookieStore();
            }
            catch (ClientProtocolException e) {
                // 200番台以外のステータスコードは例外扱い
                statusCode = ((HttpResponseException)e).getStatusCode();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                // クライアントの終了処理
                httpClient.getConnectionManager().shutdown();
            }
            
            Http.Result result = new Http.Result(statusCode, responseBody);
            if (cookieStore != null) {
                result.cookieStore = cookieStore;
            }
            
            return result;
        }
    }
    
    public static class Result {
        
        public int statusCode;
        public String responseBody;
        public CookieStore cookieStore = null;
        
        public Result(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }

}
