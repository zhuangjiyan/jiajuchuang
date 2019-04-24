package com.example.wifivoice;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Button login_ok;
    private EditText login_password;
    private CheckBox remember_pass;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private List<String> needPermission;
    private final int REQUEST_CODE_PERMISSION = 0;

    private String[] permissionArray = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //initView();

        permission_ask();
    }

//    private void initView(){
//        Toolbar toolbar = findViewById(R.id.toolbar3);
//        setSupportActionBar(toolbar);
//        remember_pass = findViewById(R.id.remember_pass);
//        login_password = findViewById(R.id.login_password);
//        login_ok = findViewById(R.id.login_ok);
//        pref = PreferenceManager.getDefaultSharedPreferences(this);
//        boolean isRemember = pref.getBoolean("remember_password" ,false);
//        if(isRemember){
//            String password = pref.getString("password", "");
//            login_password.setText(password);
//            remember_pass.setChecked(true);
//        }
//
//        login_ok.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String password = login_password.getText().toString();
//                if(password.equals(PASSWORD)){
//                    editor = pref.edit();
//                    if(remember_pass.isChecked()){
//                        editor.putBoolean("remember_password", true);
//                        editor.putString("password", password);
//                    }else {
//                        editor.clear();
//                    }
//                    editor.apply();
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    startActivity(intent);
//                }else {
//                    Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    private void permission_ask(){
        needPermission = new ArrayList<>();
        for(String permissionName:permissionArray){
            if (!checkIsAskPermission(this, permissionName)) {
                needPermission.add(permissionName);
            }
        }
        if(needPermission.size() > 0){
            ActivityCompat.requestPermissions(this, needPermission.toArray(new String[needPermission.size()]), REQUEST_CODE_PERMISSION);
        }else {
            begin();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_PERMISSION:
                Map<String, Integer> permissionMap = new HashMap<>();
                for(String name:needPermission){
                    permissionMap.put(name, PackageManager.PERMISSION_GRANTED);
                }
                for(int i=0;i < permissions.length;i++){
                    permissionMap.put(permissions[i], grantResults[i]);
                }
                if(checkIsAskPermissionState(permissionMap, permissions)){
                    begin();
                }else {
                    Toast.makeText(this, "请开启全部权限后使用", Toast.LENGTH_SHORT).show();
                    begin();
                }
        }
    }

    private boolean checkIsAskPermission(Context context, String permission){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else {
            return true;
        }
    }

    private boolean checkIsAskPermissionState(Map<String, Integer> maps, String[] list){
        for(int i = 0;i < list.length;i++){
            if(maps.get(list[i]) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void begin(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
    }
}
