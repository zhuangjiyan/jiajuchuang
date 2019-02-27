package com.example.wifivoice;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class TabActivity extends AppCompatActivity {

    private String[] tags = {"A_tag", "B_tag", "C_tag"};
    private String[] title = {"床体", "冲洗", "体重"};
    private Intent[] intents = new Intent[3];

    private LocalActivityManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        TabHost tabHost = findViewById(R.id.tabhost);
        TabWidget tabWidget = findViewById(android.R.id.tabs);
        tabWidget.setBackgroundColor(Color.WHITE);
        manager = new LocalActivityManager(TabActivity.this, true);
        manager.dispatchCreate(savedInstanceState);
        tabHost.setup(manager);
        init_intent();
        for(int i = 0;i < intents.length;i++){
            LayoutInflater inflater = this.getLayoutInflater();
            View view = inflater.inflate(R.layout.tab,null);
            TextView textView = view.findViewById(R.id.tab_text);
            textView.setText(title[i]);
            TabHost.TabSpec spec = tabHost.newTabSpec(tags[i])
                    .setContent(intents[i])
                    .setIndicator(view);
            tabHost.addTab(spec);
        }
    }

    private void init_intent(){
        intents[0] = new Intent(this,MainActivity.class);
        intents[1] = new Intent(this,CleanActivity.class);
        intents[2] = new Intent(this,WeightActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.dispatchResume();
    }
}
