package com.example.wifivoice;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by pc on 2018/6/5.
 */

public class UDPClient implements Runnable{
    private final String TAG = "UDPClient";

    private final int PORT = 3000;
    private static DatagramSocket socket = null;
    private static DatagramPacket inpacket, outpacket;
    private byte[] inBuff = new byte[1024];

    private int RESEND_TIMEOUT = 750;
    private int RESEND_TIMES = 0;

    private boolean udpLife = true;
    private static boolean received = false;
    private int weightValue;

    public UDPClient(){
        if(socket == null){
            try {
                socket = new DatagramSocket(PORT);
                Log.d(TAG, "suc");
            }catch (SocketException e){
                e.printStackTrace();
                Log.d(TAG, "failed");
            }
        }else {
            Log.d(TAG, "not null");
        }
    }

    public boolean isUdpLife(){
        return udpLife;
    }

    public static boolean isReceived(){
        return received;
    }

    public void setUdpLife(boolean b){
        udpLife = b;
    }

    public static void setReceived(boolean b){
        received = b;
    }

    public int getWeightValue(){
        return weightValue;
    }

    public void send_data(final String local_ip,final byte[] output_data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress address = null;
                try {
                    String IP = staticValue.getIp();
                    address = InetAddress.getByName(IP);
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }

                String data = new String(output_data);
                String str = local_ip + data;
                byte[] b = str.getBytes();
                outpacket = new DatagramPacket(b, b.length, address, PORT);

                try {
                    socket.send(outpacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                inpacket = new DatagramPacket(inBuff, inBuff.length);
                if(RESEND_TIMES < 3) {
                    try {
                        socket.setSoTimeout(RESEND_TIMEOUT);
                        socket.receive(inpacket);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                        RESEND_TIMES ++;
                        try {
                            socket.send(outpacket);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    RESEND_TIMES = 0;
                }
            }
        }).start();
    }

    public void send_data(final byte[] output_data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int times = 0;
                InetAddress address = null;
                try {
                    String IP = staticValue.getIp();
                    address = InetAddress.getByName(IP);
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                outpacket = new DatagramPacket(output_data, output_data.length, address, PORT);
                try {
                    socket.send(outpacket);
                    Log.d(TAG, staticValue.getIp());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                inpacket = new DatagramPacket(inBuff, inBuff.length);
                while (times < 2){
                    try {
                        socket.setSoTimeout(RESEND_TIMEOUT);
                        socket.receive(inpacket);
                    } catch (SocketTimeoutException e) {
                        e.printStackTrace();
                        times ++;
                        try {
                            socket.send(outpacket);
                            continue;
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }).start();
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(0);
        }catch (SocketException e){
            e.printStackTrace();
        }

        inpacket = new DatagramPacket(inBuff, inBuff.length);
        while (udpLife){
            try {
                socket.receive(inpacket);
                setReceived(true);
                String RcvMsg = new String(inpacket.getData(), inpacket.getOffset(), inpacket.getLength());
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        Log.d(TAG, "UDP closed");
        socket.close();
    }
}
