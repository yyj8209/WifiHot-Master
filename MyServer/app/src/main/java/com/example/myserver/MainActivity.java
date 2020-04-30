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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final int MAXCLIENT = 8;

    private static final String TAG = "MainActivity";
    private static final String TAG_D = "TAG_DEBUG";
//    ServerSocket serverSocket;//创建ServerSocket对象
//    Socket clicksSocket;//连接通道，创建Socket对象
//    ExecutorService executorService;   // 创建线程池
    Receive_Thread receive_Thread;
    Button startButton;//发送按钮
    EditText portEditText, ipEditText;//端口号和IP
    EditText receiveEditText;//接收消息框
    Button sendButton;//发送按钮
    EditText sendEditText;//发送消息框
//    InputStream inputstream;//创建输入数据流
//    OutputStream outputStream;//创建输出数据流

    ArrayList<SocketBean> ClientList;   // Set of clients(IP)
//    public static final String Client[] = {"No.1:", "No.2:", "No.3:", "No.4:", "No.5:", "No.6:", "No.7:", "No.8:"};
//    public int ClientCode = 0;
    public String CurrentClient;
    public boolean isStart = true;
    public TextView textView[] = new TextView[4];

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

        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer();
                    }
                }).start();
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });

        textView[0] = findViewById(R.id.receive_TextView1);
        textView[1] = findViewById(R.id.receive_TextView2);
        textView[2] = findViewById(R.id.receive_TextView3);
        textView[3] = findViewById(R.id.receive_TextView4);

//        executorService = Executors.newCachedThreadPool();
        ClientList = new ArrayList<SocketBean>();
    }
    /**
     * 启动服务按钮监听事件
     */
    private void startServer() {

        ServerSocket serverSocket = null;
        ExecutorService executorService = Executors.newCachedThreadPool();
        int port =Integer.valueOf(portEditText.getText().toString());//获取portEditText中的端口号
        try{
                serverSocket = new ServerSocket(port);//监听port端口，这个程序的通信端口就是port了
                ipEditText.setText(getHostIpAddress());
                while (isStart)
                {
                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                    Socket clicksSocket = serverSocket.accept();
                    clicksSocket.setSoTimeout(5000);
                    CurrentClient = getClientIpAddress(clicksSocket).replace("/","");
                    final SocketBean socketBean = new SocketBean("",clicksSocket);
                    socketBean.id = CurrentClient;
                    socketBean.socket = clicksSocket;
                    receive_Thread = new Receive_Thread(clicksSocket);

                    if (clicksSocket.isConnected()) {
                        ClientList.add(socketBean);    // Insert client to clientset.
                        //启动接收线程
//                        receive_Thread.start();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receiveEditText.setText("当前接入："+CurrentClient);
                            }
                        });
                        Log.d(TAG_D,"客户端数量："+ClientList.size());
                        executorService.execute(receive_Thread);
                    }
                }
                serverSocket.close();     // 关闭服务器，所有线程，可以用清理线程池的方式。
//                executorService.shutdownNow();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (serverSocket != null) {
                    try {
//                        isStart = false;
                        Log.d(TAG_D,"接收线程关闭");
//                        executorService.shutdown();
//                        inputstream.close();
//                        outputStream.close();
                        serverSocket.close();
                        receive_Thread.interrupt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    };

    /**
     * 服务器监听线程
     */
    class ServerSocket_thread extends Thread
    {
        @Override
        public void run()//重写Thread的run方法
        {
//            try
//            {
//                int port =Integer.valueOf(portEditText.getText().toString());//获取portEditText中的端口号
//                serverSocket = new ServerSocket(port);//监听port端口，这个程序的通信端口就是port了
//                ipEditText.setText(getHostIpAddress());
//            }
//            catch (IOException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            try
//            {
//                while (isStart)
//                {
//                    //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
//                    clicksSocket = serverSocket.accept();
//                    clicksSocket.setSoTimeout(5000);
////                    inputstream = clicksSocket.getInputStream();//
//                    // 为了显示多个客户端
//                    CurrentClient = getClientIpAddress(clicksSocket).replace("/","");
//                    //启动接收线程
//                    receive_Thread = new Receive_Thread(clicksSocket);
//                    receive_Thread.start();
//                    final SocketBean socketBean = new SocketBean("",clicksSocket);
//                    socketBean.id = CurrentClient;
//                    socketBean.socket = clicksSocket;
//
//                    if (clicksSocket.isConnected()) {
//                        ClientList.add(socketBean);    // Insert client to clientset.
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                receiveEditText.setText("当前接入："+CurrentClient);
//                            }
//                        });
//                        Log.d(TAG_D,"客户端数量："+ClientList.size());
////                        executorService.execute(receive_Thread);
//
////                    new Thread(new Runnable() {
////                        @Override
////                        public void run() {
////                            try {
//////                                inputstream = clicksSocket.getInputStream();//
////                                outputStream = clicksSocket.getOutputStream();
////                                if (outputStream != null) {
////                                    outputStream.write(CurrentClient.getBytes("utf-8"));
////                                }
//////                                    Thread.sleep(10);
//////                                    outputStream.close();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            }
////                        }
////                    }).start();
//
//                    }
//                }
////                clicksSocket.close();     // 关闭所有线程，可以用清理线程池的方式。
////                executorService.shutdownNow();
//            }
//            catch (IOException e)
//            {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } finally {
//                if (serverSocket != null) {
//                    try {
////                        isStart = false;
//                        receive_Thread.interrupt();
//                        Log.d(TAG_D,"接收线程关闭");
////                        executorService.shutdown();
////                        inputstream.close();
////                        outputStream.close();
//                        serverSocket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
    }
    /**
     *
     * 接收线程
     *
     */
    class Receive_Thread extends Thread//继承Thread
    {
        Socket socket;
        Receive_Thread(){};
        Receive_Thread(Socket socket){
            this.socket = socket;
            Log.d(TAG_D,"启动新线程");
        }

        @Override
        public void run()//重写run方法
        {
            try
            {
                while(true){
//                    Log.d(TAG_D,String.valueOf(socket.isClosed()));
                    final byte[] buf = new byte[1024];
                    final InputStream is = socket.getInputStream();
                    final OutputStream os = socket.getOutputStream();   // 需要和线程对应起来
                    final int len = is.read(buf);
                    Log.d(TAG_D,new String(buf,0,len));
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            textView[ClientList.size()-1].setText(new String(buf,0,len));
                        }
                    });
                }

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
////                            inputstream = clicksSocket.getInputStream();//
////                            outputStream = clicksSocket.getOutputStream();
//                            if (os != null) {
//                                os.write(CurrentClient.getBytes("utf-8"));
//                            }
////                                    Thread.sleep(10);
////                                    outputStream.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    /**
     * 发送消息按钮事件
     */
    private void sendMsg() {
        Toast.makeText(MainActivity.this, "reserved", Toast.LENGTH_LONG).show();
            // TODO Auto-generated method stub
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                            outputStream = clicksSocket.getOutputStream();
//                            if (outputStream != null) {
//                                outputStream.write(sendEditText.getText().toString().getBytes("utf-8"));
//    //                            outputStream.flush();
//                            }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
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
        return socket.getInetAddress().toString();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (serverSocket != null) {
//            try {
//                Log.d(TAG_D,"ServerSocket closed.");
////                        executorService.shutdown();
////                        inputstream.close();
////                        outputStream.close();
//                serverSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}