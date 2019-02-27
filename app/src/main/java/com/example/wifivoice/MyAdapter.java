package com.example.wifivoice;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import static android.content.ContentValues.TAG;

public class MyAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<ScanResult> list;

    public MyAdapter(Context context, List<ScanResult> list){
        this.inflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int level;
        View view = null;
        view = inflater.inflate(R.layout.wifi_signal, parent, false);
        ScanResult scanResult = list.get(position);
        TextView wifi_ssid = view.findViewById(R.id.ssid);
        ImageView wifi_level = view.findViewById(R.id.wifi_level);
        wifi_ssid.setText(scanResult.SSID);
        level=WifiManager.calculateSignalLevel(scanResult.level,5);
        if(scanResult.capabilities.contains("WEP")||scanResult.capabilities.contains("PSK")||
                scanResult.capabilities.contains("EAP")){
            wifi_level.setImageResource(R.drawable.wifi_signal_lock);
        }else{
            wifi_level.setImageResource(R.drawable.wifi_signal_open);
        }
        wifi_level.setImageLevel(level);
        //判断信号强度，显示对应的指示图标
        return view;
    }
}
