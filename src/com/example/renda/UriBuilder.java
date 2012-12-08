package com.example.renda;

import android.net.Uri;

public class UriBuilder {
    
    private static final String scheme = "http";
    private static final String authority = "mother.example.com:3000";
     
    public static String user_exist_url(String mail_address, String access_token) {
        return builder()
                .path("/user/exist")
                .appendQueryParameter("mail_address", mail_address)
                .appendQueryParameter("access_token", access_token)
                .build().toString();
    }
    
    public static String user_register_url() {
        return builder().path("/user/register")
                .build().toString();
    }
    
    public static String user_edit_url() {
        return builder().path("/user/edit")
                .build().toString();
    }
    
    public static String user_ranking_url() {
        return builder().path("/user/ranking")
                .build().toString();
    }
    
    private static Uri.Builder builder() {
        return new Uri.Builder().scheme(scheme).encodedAuthority(authority);
    }
}
