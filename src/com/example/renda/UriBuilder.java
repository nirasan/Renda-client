package com.example.renda;

import android.net.Uri;

public class UriBuilder {
    
    private static final String scheme = "http";
    //private static final String authority = "mother.example.com:3000";
    private static final String authority = "pure-thicket-4789.herokuapp.com";
    
    public static Uri oauth_start_uri() {
        return builder().path("/oauth/index").build();
    }
    
    public static String user_exist_url(String mail_address, String access_token) {
        return builder()
                .path("/user/exist")
                .appendQueryParameter("mail_address", mail_address)
                .appendQueryParameter("access_token", access_token)
                .build().toString();
    }
    
    public static String user_register_url() {
        return builder().path("/user/register").build().toString();
    }
    
    public static String user_status_url() {
        return builder().path("/user/status").build().toString();
    }
    
    public static String user_update_count_url() {
        return builder().path("/user/update_count").build().toString();
    }

    public static String user_ranking_url(String category, String mail_address, String access_token) {
        return builder()
                .path("/ranking/" + category)
                .appendQueryParameter("mail_address", mail_address)
                .appendQueryParameter("access_token", access_token)
                .build().toString();
    }
    
    private static Uri.Builder builder() {
        return new Uri.Builder().scheme(scheme).encodedAuthority(authority);
    }
}
