package com.example.wifivoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "SettingActivity";

    private EditText user_, password_;
    private CheckBox remrmber_user;
    private Button re_, scan;
    private ProgressBar progress_bar;
    private SeekBar thresh, thresh1;
    private TextView thresh_value, thresh_value1;
    private Toolbar toolbar;
    private ListView mlistview;

    private WifiAdmin wifiAdmin;
    private LocationManager locationManager;
    private WifiManager wifiManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private List<ScanResult> mwifilist;

    private String IP;
    private String ssid;
    private Boolean RE_STATE = false;

    private int reLink;
    private String threshStr = "语音唤醒阈值：";
    private String threshStr1 = "语音识别阈值：";
    private int curThresh;
    private int scThresh;
    private static final int MAX_ = 99;
    private static final int MIN_ = 65;
    private static final int MAX = 60;
    private static final int MIN = -20;
    private static final byte[] RST_VALUE = {0x52,0x53,0x54};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initview();
        initIntent();
        initBroadReceiver();
    }

    private void initview(){
        wifiAdmin = new WifiAdmin(SettingActivity.this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        progress_bar = findViewById(R.id.progress_bar);
        re_ = findViewById(R.id.re_);
        scan = findViewById(R.id.scan);
        re_.setOnClickListener(SettingActivity.this);
        scan.setOnClickListener(SettingActivity.this);

        mlistview = findViewById(R.id.mlistview);
        pref = getSharedPreferences("user", MODE_PRIVATE);
        remrmber_user = findViewById(R.id.remember_user);
        toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("设置");
        user_ = findViewById(R.id.user_);
        password_ = findViewById(R.id.password_);

        thresh = findViewById(R.id.thresh);
        thresh1 = findViewById(R.id.thresh1);
        thresh_value = findViewById(R.id.thresh_value);
        thresh_value1 = findViewById(R.id.thresh_value1);
        thresh.setMax(MAX - MIN);
        thresh1.setMax(MAX_ - MIN_);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = pref.edit();
                if(remrmber_user.isChecked()){
                    editor.putBoolean("remember_user", true);
                    editor.putBoolean("RE_STATE", RE_STATE);
                    editor.putString("user", user_.getText().toString());
                }else {
                    editor.clear();
                }
                editor.apply();
                finish();
            }
        });
        boolean isRemember = pref.getBoolean("remember_user" ,false);
        if(isRemember){
            String user = pref.getString("user", "");
            user_.setText(user);
            remrmber_user.setChecked(true);
        }

        RE_STATE = pref.getBoolean("RE_STATE", false);
        if(!RE_STATE){
            re_.setText("连接");
        }else {
            re_.setText("连接");
        }

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ssid = mwifilist.get(position).SSID;
                user_.setText(ssid);
            }
        });
    }

    private void initIntent(){
        curThresh = staticValue.getCurThread();
        scThresh = staticValue.getScThread();
        thresh.setProgress(curThresh - MIN);
        thresh_value.setText(threshStr + curThresh);
        thresh1.setProgress(scThresh - MIN_);
        thresh_value1.setText(threshStr1 + scThresh);

        thresh.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                curThresh = seekBar.getProgress() + MIN;
                staticValue.setCurThread(curThresh);
                thresh_value.setText(threshStr + curThresh);
                editor = getSharedPreferences("data_", MODE_PRIVATE).edit();
                editor.putInt("curThresh", curThresh);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        thresh1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scThresh = seekBar.getProgress() + MIN_;
                staticValue.setScThread(scThresh);
                thresh_value1.setText(threshStr1 + scThresh);
                editor = getSharedPreferences("data_", MODE_PRIVATE).edit();
                editor.putInt("scThresh", scThresh);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initBroadReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.re_:
                progress_bar.setVisibility(View.VISIBLE);
                reLink = 1;
                if(password_.length() != 0) {
                    re_.setText("重新设置中");
                    send();
                }else {
                    re_.setText("连接中");
                    Get_IP();
                }
                break;
            case R.id.scan:
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && wifiManager.isWifiEnabled()){
                    progress_bar.setVisibility(View.VISIBLE);
                    wifiAdmin.startScan(SettingActivity.this);
                    scan.setText("扫描中");
                }else if(wifiManager.isWifiEnabled()){
                    Toast.makeText(this, "请打开GPS", Toast.LENGTH_SHORT).show();
                }else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(this, "请打开wifi", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "请打开GPS与wifi", Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }

    public class Utility {
        public void setListViewHeightBasedOnChildren(ListView listView) {
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                return;
            }
            int totalHeight = 0;
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            listView.setLayoutParams(params);
        }
    }

    @Override
    public void onBackPressed() {
        reLink = 3;
        editor = pref.edit();
        if(remrmber_user.isChecked()){
            editor.putBoolean("remember_user", true);
            editor.putString("user", user_.getText().toString());
            editor.putBoolean("RE_STATE", RE_STATE);
        }else {
            editor.clear();
        }
        editor.apply();
        finish();
    }

    private void send(){
        String SSID = user_.getText().toString();
        String PASSWORD = password_.getText().toString();
        MediaType JSON = MediaType.parse("application/json;charset=UTF-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        final String jsonString = "{\n"+"\"sta\":{\"ssid\":\""+ SSID + "\",\"password\":\"" + PASSWORD + "\"},\"mode\":\"apsta\"\n"+"}";
        RequestBody requestBody = RequestBody.create(JSON, jsonString);
        Request request = new Request.Builder()
                .url("http://192.168.4.1/config?cmd=wifi")
                .header("Content-Type", "text/plain;charset=UTF-8")
                .put(requestBody)
                .post(requestBody).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Message message = Message.obtain();
                message.what = 2;
                handler.sendMessage(message);
            }
        });
    }

    private void Get_IP(){
        OkHttpClient client = new OkHttpClient.Builder()
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url("http://192.168.4.1/config?cmd=wifi")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = 3;
                handler.sendMessage(message);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                IP = parseJSONWithJSONObjevt(responseBody);
                staticValue.setIp(IP);
                Message message = Message.obtain();
                message.what = 4;
                handler.sendMessage(message);
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            progress_bar.setVisibility(View.GONE);
            mwifilist = wifiAdmin.getWifiList();
            if(mwifilist != null){
                scan.setText("扫描");
                MyAdapter adapter = new MyAdapter(SettingActivity.this, mwifilist);
                mlistview.setAdapter(adapter);
            }
            //new Utility().setListViewHeightBasedOnChildren(mlistview);
        }
    };

    private String parseJSONWithJSONObjevt(String jsonData){
        String IP_;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject stationObject = jsonObject.getJSONObject("sta");
            IP_ = stationObject.getString("ip");
        }catch (Exception e){
            e.printStackTrace();
            IP_ = "0.0.0.0";
        }
        return IP_;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    progress_bar.setVisibility(View.GONE);
                    Toast.makeText(SettingActivity.this, "设置超时，请确认连接安信可网络", Toast.LENGTH_SHORT).show();
                    re_.setText("连接");
                    break;
                case 2:
                    Toast.makeText(SettingActivity.this, "设置成功, 开始连接", Toast.LENGTH_SHORT).show();
                    RE_STATE = true;
                    Get_IP();
                    re_.setText("连接中");
                    break;
                case 3:
                    if (reLink < 3){
                        Toast.makeText(SettingActivity.this, "连接超时，正在重连第"+ reLink +"次", Toast.LENGTH_SHORT).show();
                        Get_IP();
                        reLink++;
                    }else {
                        Toast.makeText(SettingActivity.this, "连接失败，请检查网络", Toast.LENGTH_SHORT).show();
                        re_.setText("连接");
                        progress_bar.setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    if(IP.equals("0.0.0.0")){
                        Get_IP();
                    }else {
                        editor = getSharedPreferences("data_", MODE_PRIVATE).edit();
                        editor.putString("IP", IP);
                        editor.apply();
                        progress_bar.setVisibility(View.GONE);
                        Toast.makeText(SettingActivity.this, "已成功连接" , Toast.LENGTH_LONG).show();
                        re_.setText("连接");
                        RE_STATE = false;
                    }
                    break;
                default:break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}