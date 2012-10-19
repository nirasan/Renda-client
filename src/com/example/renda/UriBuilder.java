package com.example.renda;

import android.net.Uri;

public class UriBuilder {
    
    public static String user_show_url(String username, String password) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.encodedAuthority("10.0.2.2:3000");
        builder.path("/user/show");
        builder.appendQueryParameter("username", username);
        builder.appendQueryParameter("password", password);
        return builder.build().toString();
    }

}
