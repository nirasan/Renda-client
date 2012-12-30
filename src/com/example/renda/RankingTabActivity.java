package com.example.renda;

import android.R.drawable;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TabHost;

public class RankingTabActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking_tab);
        initTabs();
    }
    
    protected void initTabs(){
        
        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;
        
        // Tab1
        intent = new Intent().setClass(this, RankingPersonalActivity.class);
        spec = tabHost.newTabSpec("Personal").setIndicator(
                "Personal", res.getDrawable(drawable.ic_menu_view))
                .setContent(intent);
        tabHost.addTab(spec);
          
        // Tab2
        intent = new Intent().setClass(this, RankingGeneralActivity.class);
        spec = tabHost.newTabSpec("General").setIndicator(
                "General" , res.getDrawable(drawable.ic_menu_view))
                .setContent(intent);
        tabHost.addTab(spec);
          
        // Set Default Tab - zero based index
        tabHost.setCurrentTab(0);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ranking_tab, menu);
        return true;
    }
}
