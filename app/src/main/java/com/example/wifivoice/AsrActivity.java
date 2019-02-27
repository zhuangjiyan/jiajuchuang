package com.example.wifivoice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.speech.util.FucUtil;
import com.example.speech.util.JsonParser;
import com.example.speech.util.XmlParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ResourceUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

public class AsrActivity extends AppCompatActivity {
    private static String TAG = "AsrActivity";

    private Toolbar toolbar;
    private UDPClient udpClient;
    private Toast mToast;
    private SpeechRecognizer mAsr;
    // 缓存
    private SharedPreferences mSharedPreferences;
    // 本地语法文件
    private String mLocalGrammar;
    // 本地语法构建路径
    private String grmPath = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/msc/test";
    private String mResultType = "json";
    private String IP;

    private  final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private  final String GRAMMAR_TYPE_BNF = "bnf";
    private String mEngineType = "local";

    private int scThresh;

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
        setContentView(R.layout.activity_asr);
        SpeechUtility.createUtility(this, SpeechConstant.APPID+"=5b0771e4");
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        mLocalGrammar = FucUtil.readFile(this,"command.bnf", "utf-8");
        mSharedPreferences = getSharedPreferences("Asr",	MODE_PRIVATE);
        mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        initView();
        udpClient = new UDPClient();
        setParam();
        mAsr.startListening(mRecognizerListener);
    }

    String mContent;// 语法、词典临时变量
    int ret = 0;// 函数调用返回值

    private void initView(){
        toolbar = findViewById(R.id.toolbar11);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("语音识别");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsr.stopListening();
                mAsr.cancel();
                mAsr.destroy();
                finish();
            }
        });

        scThresh = staticValue.getScThread();
        IP = staticValue.getIp();
        Log.d(TAG, scThresh+"");
        Log.d(TAG, IP);

        mContent = new String(mLocalGrammar);
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        // 设置引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        //使用8k音频的时候请解开注释
//					mAsr.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        // 设置资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, grammarListener);
        if(ret != ErrorCode.SUCCESS){
            showTip("语法构建失败,错误码：" + ret);
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };

    /**
     * 构建语法监听器。
     */
    private GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
                    Editor editor = mSharedPreferences.edit();
                    if(!TextUtils.isEmpty(grammarId))
                        editor.putString(KEY_GRAMMAR_ABNF_ID, grammarId);
                    editor.commit();
                }
            }else{
                showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result && !TextUtils.isEmpty(result.getResultString())) {
                String text = JsonParser.parseIatResult(result.getResultString());
                int sc = JsonParser.parseScResult(result.getResultString());
                showTip(text +"  "+ sc);
                if(sc > scThresh) {
                    switch (text){
                        case "北盛":
                        case "背升":
                            udpClient.send_data(gotHostIP(), BACK_UP);
                            break;
                        case "背平":
                        case "北平":
                        case "备品":
                            udpClient.send_data(gotHostIP(), BACK_DOWN);
                            break;
                        case "腿升":
                        case "腿伸":
                            udpClient.send_data(gotHostIP(), LEG_UP);
                            break;
                        case "腿平":
                            udpClient.send_data(gotHostIP(), LEG_DOWN);
                            break;
                        case "床头升":
                            udpClient.send_data(gotHostIP(), HEAD_UP);
                            break;
                        case "床头降":
                            udpClient.send_data(gotHostIP(), HEAD_DOWN);
                            break;
                        case "床体升":
                            udpClient.send_data(gotHostIP(), WHOLE_UP);
                            break;
                        case "床体降":
                            udpClient.send_data(gotHostIP(), WHOLE_DOWN);
                            break;
                        case "坐起":
                            udpClient.send_data(gotHostIP(), BOTH_UP);
                            break;
                        case "躺下":
                            udpClient.send_data(gotHostIP(), BOTH_DOWN);
                            break;
                        default:break;
                    }
                }
            } else {
                Log.d(TAG, "recognizer result : null");
            }
            mAsr.startListening(mRecognizerListener);
        }

        @Override
        public void onEndOfSpeech() {
            Log.d(TAG,"结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG,"开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG,"onError Code："	+ error.getErrorCode());
            mAsr.startListening(mRecognizerListener);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    private void showTip(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });
    }

    /**
     * 参数设置
     * @param
     * @return
     */
    public void setParam(){
        // 清空参数
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置本地识别资源
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置返回结果格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, mResultType);
        // 设置本地识别使用语法id
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "wake");
        // 设置识别的门限值
        mAsr.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        mAsr.setParameter(SpeechConstant.VAD_BOS, "6000");
        // 使用8k音频的时候请解开注释
        //mAsr.setParameter(SpeechConstant.SAMPLE_RATE, "8000");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");
    }

    private String gotHostIP(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            showTip("请开启wifi");
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    //获取识别资源路径
    private String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( null != mAsr ){
            mAsr.stopListening();
            mAsr.cancel();
            mAsr.destroy();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAsr.stopListening();
        mAsr.cancel();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mAsr.startListening(mRecognizerListener);
    }
}
