package com.example.renda;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class Http {
    
    public static class Client {
        
        public static Http.Result getRequest(String uri) {
            
            String responseBody = "";
            int statusCode = 0;
            
            HttpGet request = new HttpGet(uri);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                responseBody = httpClient.execute(request, responseHandler);
                statusCode = HttpStatus.SC_OK;
            }
            catch (ClientProtocolException e) {
                statusCode = ((HttpResponseException)e).getStatusCode();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                httpClient.getConnectionManager().shutdown();
            }
            
            return new Http.Result(statusCode, responseBody);
        }
    }
    
    public static class Result {
        
        public int statusCode;
        public String responseBody;
        
        public Result(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }
    }

}
