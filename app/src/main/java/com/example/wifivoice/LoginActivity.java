package com.example.wifivoice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private Button login_ok;
    private EditText login_password;
    private CheckBox remember_pass;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private static final String PASSWORD = "12345678";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        //initView();

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(LoginActivity.this, TabActivity.class);
                startActivity(intent);
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, 2000);
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
}
