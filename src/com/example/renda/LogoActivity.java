package com.example.renda;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class LogoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_logo, menu);
        return true;
    }
}
