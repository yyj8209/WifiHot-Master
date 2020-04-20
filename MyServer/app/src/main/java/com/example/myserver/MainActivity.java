package com.example.myserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends Activity {

    ServerSocket serverSocket;//创建ServerSocket对象
    Socket clicksSocket;//连接通道，创建Socket对象
    Button startButton;//发送按钮
    EditText portEditText, ipEditText;//端口号和IP
    EditText receiveEditText;//接收消息框
    Button sendButton;//发送按钮
    EditText sendEditText;//发送消息框
    InputStream inputstream;//创建输入数据流
    OutputStream outputStream;//创建输出数据流

    public static final int MAXCLIENT = 8;
    public static final String Client[] = {"No.1", "No.2", "No.3", "No.4", "No.5", "No.6", "No.7", "No.8"};
    public int ClientCode = 0;
    public String CurrentClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 读一下手机wifi状态下的ip地址，只有知道它的ip才能连接它嘛
         */
//        Toast.makeText(MainActivity.this, getLocalIpAddress(), Toast.LENGTH_LONG).show();

        startButton = (Button) findViewById(R.id.start_button);
        portEditText = (EditText) findViewById(R.id.port_EditText);
        ipEditText = (EditText) findViewById(R.id.ip_EditText);
        receiveEditText = (EditText) findViewById(R.id.receive_EditText);
        sendButton = (Button) findViewById(R.id.send_button);
        sendEditText = (EditText) findViewById(R.id.message_EditText);

        startButton.setOnClickListener(startButtonListener);
        sendButton.setOnClickListener(sendButtonListener);
    }
    /**
     * 启动服务按钮监听事件
     */
    private OnClickListener startButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            /**
             * 启动服务器监听线程
             */
            ClientCode = 0;
            ServerSocket_thread serversocket_thread = new ServerSocket_thread();
            serversocket_thread.start();
        }
    };
    /**
     * 服务器监听线程
     */
    class ServerSocket_thread extends Thread
    {
        public void run()//重写Thread的run方法
        {
            try
            {
                int port =Integer.valueOf(portEditText.getText().toString());//获取portEditText中的端口号
                serverSocket = new ServerSocket(port);//监听port端口，这个程序的通信端口就是port了
                ipEditText.setText(getHostIpAddress());
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            while (true)
            {
                try
                {
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    clicksSocket = serverSocket.accept();
                    inputstream = clicksSocket.getInputStream();//
                    // 为了显示多个客户端
                    CurrentClient = getClientIpAddress(clicksSocket);
                    Client[ClientCode].concat(CurrentClient);
                    ClientCode ++;
//                    Toast.makeText(getApplicationContext(),ClientCode+CurrentClient,Toast.LENGTH_LONG);
                    //启动接收线程
                    Receive_Thread receive_Thread = new Receive_Thread(ClientCode);
                    receive_Thread.start();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     *
     * 接收线程
     *
     */
    class Receive_Thread extends Thread//继承Thread
    {
        int clientCode;
        Receive_Thread(int ClientCode){
            this.clientCode = ClientCode;
        }
        public void run()//重写run方法
        {
            while (true)
            {
                try
                {
                    final byte[] buf = new byte[1024];
                    final int len = inputstream.read(buf);
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            receiveEditText.setText(new String(buf,0,len)+"线程"+clientCode);
                        }
                    });
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 发送消息按钮事件
     */
    private OnClickListener sendButtonListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream = clicksSocket.getOutputStream();
                        if (outputStream != null) {
                            outputStream.write(sendEditText.getText().toString().getBytes("utf-8"));
//                            outputStream.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    };
    /**
     *
     * 获取WIFI下ip地址
     */
    private String getClientIpAddress(Socket socket) {
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//        // 获取32位整型IP地址
//        int ipAddress = wifiInfo.getIpAddress();
//        //返回整型地址转换成“*.*.*.*”地址
//        return String.format("%d.%d.%d.%d",
//                (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
//                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return socket.getRemoteSocketAddress().toString()+"/"+socket.getInetAddress().toString();
    }


    public String getHostIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if (intf.getName().contains("wlan")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && (inetAddress.getAddress().length == 4)) {
                            Log.d("Main", "+++getWifiApIpAddress"+inetAddress.getHostAddress());
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Main", ex.toString());
        }
        return null;
    }
}