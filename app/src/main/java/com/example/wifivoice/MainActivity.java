package com.example.wifivoice;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{
    private static final String TAG = "MainActivity";

    private Button back_up, back_down, leg_up, leg_down, both_up, both_down, whole_up, whole_down, head_up, head_down;
    private Toolbar toolbar;
    private VoiceWakeuper mIvw;
    private String resultString;

    private UDPClient udpClient;
    private staticValue staticvalue;

    private String IP = "0.0.0.0";
    private int curThresh = 10;
    private int scThresh = 70;
    private static final String APPID = "5b0771e4";

    private int count1,count2,count3,count4,count5,count6,count7,count8,count9,count10 = 0;
    private static final int TIME_UNIT = 1000;
    private long oldHit;
    private long newHit;
    private boolean sended = false;

    private static final byte[] BACK_UP = {0x0c,0x01,0x01,0x48};
    private static final byte[] BACK_DOWN = {0x0c,0x01,0x02,0x49};
    private static final byte[] LEG_UP = {0x0c,0x01,0x03,0x4A};
    private static final byte[] LEG_DOWN = {0x0c,0x01,0x04,0x4B};
    private static final byte[] BOTH_UP = {0x0c,0x01,0x05,0x4C};
    private static final byte[] BOTH_DOWN = {0x0c,0x01,0x06,0x4D};
    private static final byte[] WHOLE_UP = {0x0c,0x01,0x07,0x4E};
    private static final byte[] WHOLE_DOWN = {0x0c,0x01,0x08,0x4F};
    private static final byte[] HEAD_UP = {0x0c,0x01,0x09,0x50};
    private static final byte[] HEAD_DOWN = {0x0c,0x01,0x0A,0x51};
    private static final byte[] STOP_VALUE = {0x0c,0x02,0x01,0x49};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("data_", MODE_PRIVATE);
        IP = pref.getString("IP","0.0.0.0");
        curThresh = pref.getInt("curThresh", 10);
        scThresh = pref.getInt("scThresh", 70);
        SpeechUtility.createUtility(this, SpeechConstant.APPID+"=5b0771e4");
        initView();
        udpClient = new UDPClient();
        staticvalue = new staticValue(IP, scThresh, curThresh);
//        mIvw = VoiceWakeuper.createWakeuper(this, null);
//        initWake();
    }

    private void initView(){
        leg_up = findViewById(R.id.leg_up);
        leg_down = findViewById(R.id.leg_down);
        back_up = findViewById(R.id.back_up);
        back_down = findViewById(R.id.back_down);
        both_up = findViewById(R.id.both_up);
        both_down = findViewById(R.id.both_down);
        whole_up = findViewById(R.id.whole_up);
        whole_down = findViewById(R.id.whole_down);
        head_up = findViewById(R.id.head_up);
        head_down = findViewById(R.id.head_down);

        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        back_up.setOnTouchListener(MainActivity.this);
        back_down.setOnTouchListener(MainActivity.this);
        leg_up.setOnTouchListener(MainActivity.this);
        leg_down.setOnTouchListener(MainActivity.this);
        both_up.setOnTouchListener(MainActivity.this);
        both_down.setOnTouchListener(MainActivity.this);
        whole_up.setOnTouchListener(MainActivity.this);
        whole_down.setOnTouchListener(MainActivity.this);
        head_up.setOnTouchListener(MainActivity.this);
        head_down.setOnTouchListener(MainActivity.this);
    }

    private void initWake() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 启动唤醒
            mIvw.startListening(mWakeuperListener);
        }else {
            Toast.makeText(MainActivity.this, "唤醒未初始化", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onClick(View v) {
//        if(sended){
//            udpClient.send_data(STOP_VALUE);
//            Log.d(TAG,"stop");
//            sended = false;
//            return;
//        }
//        switch (v.getId()){
//            case R.id.back_up:
//                count1 ++;
//                if(count1 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count1 == 1){
//                                count1 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count1 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(BACK_UP);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count1 = 0;
//                    }else {
//                        count1 = 0;
//                    }
//                }
//                break;
//            case R.id.back_down:
//                count2 ++;
//                if(count2 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count2 == 1){
//                                count2 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count2 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(BACK_DOWN);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count2 = 0;
//                    }else {
//                        count2 = 0;
//                    }
//                }
//                break;
//            case R.id.leg_up:
//                count3 ++;
//                if(count3 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count3 == 1){
//                                count3 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count3 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(LEG_UP);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count3 = 0;
//                    }else {
//                        count3 = 0;
//                    }
//                }
//                break;
//            case R.id.leg_down:
//                count4 ++;
//                if(count4 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count4 == 1){
//                                count4 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count4 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(LEG_DOWN);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count4 = 0;
//                    }else {
//                        count4 = 0;
//                    }
//                }
//                break;
//            case R.id.both_up:
//                count5 ++;
//                if(count5 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count5 == 1){
//                                count5 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count5 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(BOTH_UP);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count5 = 0;
//                    }else {
//                        count5 = 0;
//                    }
//                }
//                break;
//            case R.id.both_down:
//                count6 ++;
//                if(count6 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count6 == 1){
//                                count6 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count6 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(BOTH_DOWN);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count6 = 0;
//                    }else {
//                        count6 = 0;
//                    }
//                }
//                break;
//            case R.id.whole_up:
//                count7 ++;
//                if(count7 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count7 == 1){
//                                count7 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count7 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(WHOLE_UP);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count7 = 0;
//                    }else {
//                        count7 = 0;
//                    }
//                }
//                break;
//            case R.id.whole_down:
//                count8 ++;
//                if(count8 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count8 == 1){
//                                count8 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count8 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(WHOLE_DOWN);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count8 = 0;
//                    }else {
//                        count8 = 0;
//                    }
//                }
//                break;
//            case R.id.head_up:
//                count9 ++;
//                if(count9 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count9 == 1){
//                                count9 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count9 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(HEAD_UP);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count9 = 0;
//                    }else {
//                        count9 = 0;
//                    }
//                }
//                break;
//            case R.id.head_down:
//                count10 ++;
//                if(count10 == 1){
//                    oldHit = SystemClock.uptimeMillis();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(TIME_UNIT);
//                            }catch (InterruptedException e){
//                                e.printStackTrace();
//                            }
//                            if(count10 == 1){
//                                count10 = 0;
//                            }
//                        }
//                    }).start();
//                }else if(count10 == 2){
//                    newHit = SystemClock.uptimeMillis();
//                    if(newHit - oldHit < TIME_UNIT){
//                        udpClient.send_data(HEAD_DOWN);
//                        sended = true;
//                        Log.d(TAG, "hit");
//                        count10 = 0;
//                    }else {
//                        count10 = 0;
//                    }
//                }
//                break;
//        }
//    }

        @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.back_up:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(BACK_UP);
                        Log.d(TAG,"s");
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.back_down:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(BACK_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.leg_up:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(LEG_UP);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.leg_down:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(LEG_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.both_up:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(BOTH_UP);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.both_down:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(BOTH_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.whole_up:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(WHOLE_UP);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.whole_down:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(WHOLE_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.head_up:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(HEAD_UP);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
            case R.id.head_down:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        udpClient.send_data(HEAD_DOWN);
                        break;
                    case MotionEvent.ACTION_UP:
                        udpClient.send_data(STOP_VALUE);
                        break;
                }
                break;
        }
        return false;
    }

    private WakeuperListener mWakeuperListener = new WakeuperListener() {
        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "begin");
        }

        @Override
        public void onResult(WakeuperResult wakeuperResult) {
            try {
                String text = wakeuperResult.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                switch(object.optString("id")) {
                    case "0":break;
                    case "1":
                    case "2":
                    case "3":
                        Intent intent = new Intent(MainActivity.this, AsrActivity.class);
                        startActivity(intent);
                        break;
                    default:break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "结果解析错误", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SpeechError speechError){
            Toast.makeText(MainActivity.this, speechError.getPlainDescription(true), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
            switch (i) {
                // EVENT_RECORD_DATA 事件仅在 NOTIFY_RECORD_DATA 参数值为 真 时返回
                case SpeechEvent.EVENT_RECORD_DATA:
                    final byte[] audio = bundle.getByteArray(SpeechEvent.KEY_EVENT_RECORD_DATA);
                    Log.i(TAG, "ivw audio length: " + audio.length);
                    break;
            }
        }

        @Override
        public void onVolumeChanged(int i) {
        }
    };

    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(MainActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" +"5b0771e4" + ".jet");
        return resPath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setting:
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.pronouncement:
                Intent intent1 = new Intent(MainActivity.this, PronouncementActivity.class);
                startActivity(intent1);
                break;
            default:break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
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

    private String gotHostIP(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            Toast.makeText(MainActivity.this, "请开启wifi", Toast.LENGTH_SHORT).show();
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mIvw = VoiceWakeuper.getWakeuper();
//        if (mIvw != null) {
//            mIvw.destroy();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mIvw.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mIvw.startListening(mWakeuperListener);
        IP = staticValue.getIp();
        scThresh = staticValue.getScThread();
        curThresh = staticValue.getCurThread();
    }
}