package com.example.renda;

import android.net.Uri;

public class UriBuilder {
    
    private static final String scheme = "http";
    private static final String authority = "10.0.2.2:3000";
     
    public static String user_show_url(String username, String password) {
        Uri.Builder builder = builder();
        builder.path("/user/show");
        builder.appendQueryParameter("username", username);
        builder.appendQueryParameter("password", password);
        return builder.build().toString();
    }
    
    public static String user_add_url() {
        Uri.Builder builder = builder();
        builder.path("/user/add");
        return builder.build().toString();
    }
    
    public static String user_edit_url() {
        Uri.Builder builder = builder();
        builder.path("/user/edit");
        return builder.build().toString();
    }
    
    public static String user_ranking_url() {
        Uri.Builder builder = builder();
        builder.path("/user/ranking");
        return builder.build().toString();
    }
    
    private static Uri.Builder builder() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(scheme);
        builder.encodedAuthority(authority);
        return builder;
    }
}
