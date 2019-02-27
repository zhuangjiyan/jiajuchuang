package com.example.wifivoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

public class WeightActivity extends AppCompatActivity {
    private static String TAG = "WeightActivity";

    private Toolbar toolbar;
    private UDPClient udpClient;
    private String IP;
    private int[] weightValue;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);
        initView();
        udpClient = new UDPClient();
        IP = staticValue.getIp();
        pref = getSharedPreferences("weight", MODE_PRIVATE);
//        new Thread(udpClient).start();
//        while (true){
//            if(udpClient.isReceived()){
//                editor = pref.edit();
//            }
//        }
    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar01);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("体重");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private String gotHostIP(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            Toast.makeText(WeightActivity.this, "请开启wifi", Toast.LENGTH_SHORT).show();
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(WeightActivity.this);
        dialog.setTitle("家居床");
        dialog.setMessage("确认要离开么");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}