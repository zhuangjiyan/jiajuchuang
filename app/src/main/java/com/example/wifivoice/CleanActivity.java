package com.example.wifivoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CleanActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "CleanActivity";

    private Button t_clean, female_clean, dry, adjust, clean_power, clean_stop;
    private Toolbar toolbar;

    private static final byte[] T_VALUE = {0x0c,0x01,0x07,0x00,0x14};
    private static final byte[] FEMALE_VALUE = {0x0c,0x01,0x07,0x00,0x14};
    private static final byte[] DRY_VALUE = {0x0c,0x01,0x07,0x00,0x14};
    private static final byte[] ADJUST_VALUE = {0x0c,0x01,0x07,0x00,0x14};
    private static final byte[] POWER_VALUE = {0x0c,0x01,0x07,0x00,0x14};
    private static final byte[] STOP_VALUE = {0x0c,0x01,0x07,0x00,0x14};

    private UDPClient udpClient;
    private String IP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean);
        initView();
        udpClient = new UDPClient();
        IP = staticValue.getIp();
    }

    private void initView(){
        toolbar = findViewById(R.id.toolbar4);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("冲洗");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        t_clean = findViewById(R.id.t_clean);
        female_clean = findViewById(R.id.female_clean);
        dry = findViewById(R.id.dry);
        adjust = findViewById(R.id.adjust);
        clean_power = findViewById(R.id.clean_power);
        clean_stop = findViewById(R.id.clean_stop);

        t_clean.setOnClickListener(CleanActivity.this);
        female_clean.setOnClickListener(CleanActivity.this);
        dry.setOnClickListener(CleanActivity.this);
        adjust.setOnClickListener(CleanActivity.this);
        clean_power.setOnClickListener(CleanActivity.this);
        clean_stop.setOnClickListener(CleanActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.t_clean:
                udpClient.send_data(T_VALUE);
                break;
            case R.id.female_clean:
                udpClient.send_data(FEMALE_VALUE);
                break;
            case R.id.dry:
                udpClient.send_data(gotHostIP(), DRY_VALUE);
                break;
            case R.id.adjust:
                udpClient.send_data(gotHostIP(), ADJUST_VALUE);
                break;
            case R.id.clean_power:
                udpClient.send_data(gotHostIP(), POWER_VALUE);
                break;
            case R.id.clean_stop:
                udpClient.send_data(gotHostIP(), STOP_VALUE);
                break;
            default:break;
        }
    }

    private String gotHostIP(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(CleanActivity.this, "请开启wifi", Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder dialog = new AlertDialog.Builder(CleanActivity.this);
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
        IP = staticValue.getIp();
    }
}
