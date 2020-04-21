package com.example.myclient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    private static TCPClient tcpClient = null;
    private TCPClient(){}
    public static TCPClient getInstance(){
        if(tcpClient == null){
            synchronized ((TCPClient.class)){
                tcpClient = new TCPClient();
            }
        }
        return tcpClient;
    }

    String TAG = "Socket";
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private SocketThread socketThead;
    private boolean isStop = false;

    private class SocketThread extends Thread{
        private String ip;
        private int port;
        public SocketThread(String ip, int port){
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run(){
            Log.d(TAG,"SocketThread start");
            super.run();

            try{
                if(socket != null){
                    socket.close();
                    socket = null;
                }

                InetAddress inetAddress = InetAddress.getByName(ip);
                socket = new Socket(inetAddress,port);

                if(isConnect()){
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                    isStop = false;
                    uiHandler.sendEmptyMessage(1);
                }
                else{
                    uiHandler.sendEmptyMessage(-1);
                    Log.e(TAG,"SocketThread connect fail");
                    return;
                }
            }
            catch (IOException e){
                uiHandler.sendEmptyMessage(-1);
                Log.e(TAG,"SocketThread connect io exception = "+e.getStackTrace());
                e.printStackTrace();
                return;
            }
            Log.d(TAG,"SochetThread connect over");

            while (isConnect() && !isStop && !isInterrupted()){
                int size;
                try{
                    byte[] buffer = new byte[1024];
                    if(inputStream == null)  return;
                    size = inputStream.read(buffer);
                    if(size>0){
                        Message msg = new Message();
                        msg.what = 100;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("data",buffer);
                        bundle.putInt("size",size);
                        bundle.putInt("requestCode",requestCode);
                        msg.setData(bundle);
                        uiHandler.sendMessage(msg);
                    }
                    Log.i(TAG,"SocketThread read listening");
                }
                catch (IOException e){
                    uiHandler.sendEmptyMessage(-1);
                    Log.e(TAG,"SocketThread read io exception = "+e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
    //==============================socket connect============================
    /**
     * connect socket in thread
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void connect(String ip, int port){
        socketThead = new SocketThread(ip, port);
        socketThead.start();
    }

    /**
     * socket is connect
     * */
    public boolean isConnect(){
        boolean flag = false;
        if (socket != null) {
            flag = socket.isConnected();
        }
        return flag;
    }

    /**
     * socket disconnect
     * */
    public void disconnect() {
        isStop = true;
        sendStrCmds(' '+ socketThead.ip,1001);   // try sending info to exit
        try {
            if (outputStream != null) {
                outputStream.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (socketThead != null) {
            socketThead.interrupt();//not intime destory thread,so need a flag
        }
    }



    /**
     * send byte[] cmd
     * Exception : android.os.NetworkOnMainThreadException
     * */
    public void sendByteCmd(final byte[] mBuffer,int requestCode) {
        this.requestCode = requestCode;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (outputStream != null) {
                        outputStream.write(mBuffer);
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    /**
     * send string cmd to serial
     */
    public void sendStrCmds(String cmd, int requestCode) {
        try {
            byte[] mBuffer = cmd.getBytes("utf-8");
            sendByteCmd(mBuffer,requestCode);
        }
        catch (UnsupportedEncodingException e1){
            e1.printStackTrace();
        }
    }


    /**
     * send prt content cmd to serial
     */
    public void sendChsPrtCmds(String content, int requestCode) {
        try {
            byte[] mBuffer = content.getBytes("GB2312");
            sendByteCmd(mBuffer,requestCode);
        }
        catch (UnsupportedEncodingException e1){
            e1.printStackTrace();
        }
    }


    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                //connect error
                case -1:
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectFail();
                        disconnect();
                    }
                    break;

                //connect success
                case 1:
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onConnectSuccess();
                    }
                    break;

                //receive data
                case 100:
                    Bundle bundle = msg.getData();
                    byte[] buffer = bundle.getByteArray("data");
                    
                    int size = bundle.getInt("size");
                    int mequestCode = bundle.getInt("requestCode");
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onDataReceive(buffer, size, mequestCode);
                    }
                    break;
            }
        }
    };


    /**
     * socket response data listener
     * */
    private OnDataReceiveListener onDataReceiveListener = null;
    private int requestCode = -1;
    public interface OnDataReceiveListener {
        public void onConnectSuccess();
        public void onConnectFail();
        public void onDataReceive(byte[] buffer, int size, int requestCode);
    }
    public void setOnDataReceiveListener(
            OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
