package com.example.syhuang.wifiserver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.syhuang.wifiserver.thread.ConnectThread;
import com.example.syhuang.wifiserver.thread.ListenerThread;
import com.example.syhuang.wifiserver.thread.WifiApManage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HotspotServer";
    public static final int DEVICE_CONNECTING = 1;//有设备正在连接热点
    public static final int DEVICE_CONNECTED  = 2;//有设备连上热点
    public static final int SEND_MSG_SUCCSEE  = 3;//发送消息成功
    public static final int SEND_MSG_ERROR    = 4;//发送消息失败
    public static final int GET_MSG            = 6;//获取新消息
    private TextView      text_state;

    /**
     * 连接线程
     */
    private ConnectThread connectThread;


    /**
     * 监听线程
     */
    private ListenerThread listenerThread;
    /**
     * 端口号
     */
    private static final int    PORT              = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.create_server).setOnClickListener(this);
        findViewById(R.id.close_server).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);
        text_state = (TextView) findViewById(R.id.receive);

        /**
         * 先开启监听线程，在开启连接
         */
        listenerThread = new ListenerThread(PORT, handler);
        listenerThread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //        开启连接线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("ip", "+++onCreat:getWifiApIpAddress()" + getWifiApIpAddress());
                    //本地路由开启通信
                    String ip = getWifiApIpAddress();
                    if (ip != null) {
                    } else {
                        ip = "192.168.43.1";
                        Log.d("ip", "+++ 192.168.43.1");
                    }
                    Socket socket = new Socket(ip, PORT);
                    connectThread = new ConnectThread(MainActivity.this, socket, handler);
                    connectThread.start();

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text_state.setText("创建通信失败");
                        }
                    });

                }
            }
        }).start();


    }

    //    /**
    //     * 获取已连接的
    //     *
    //     * @return
    //     */
    //    private String getIp() {
    //        //检查Wifi状态
    //        if (!wifiManager.isWifiEnabled())
    //            wifiManager.setWifiEnabled(true);
    //        WifiInfo wi = wifiManager.getConnectionInfo();
    //        //获取32位整型IP地址
    //        int ipAdd = wi.getIpAddress();
    //        //把整型地址转换成“*.*.*.*”地址
    //        String ip = intToIp(ipAdd);
    //        return ip;
    //    }
    //
    //    private String intToIp(int i) {
    //        return (i & 0xFF) + "." +
    //                ((i >> 8) & 0xFF) + "." +
    //                ((i >> 16) & 0xFF) + "." +
    //                (i >> 24 & 0xFF);
    //    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_server:
                //TODO implement
                createWifiHotspot();
                break;
            case R.id.close_server:
                //TODO implement
//                closeWifiHotspot();
//                wifiApManage.closeWifiAp();
                break;
            case R.id.send:
                //TODO implement
                if (connectThread != null) {
                    connectThread.sendData("这是来自Wifi_server的消息");
                } else {
                    Log.d("AAA", "+++send:connectThread == null");
                }
                break;
        }
    }


    /**
     * 创建Wifi热点--疯狂尝试
     */
//    private void startHotSpot(final Intent intent) {
////        final WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//
//        if (manager!= null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
//                @Override
//                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
//                    super.onStarted(reservation);
////                    sReservation = reservation;
//                    PendingIntent pendingIntent = intent.getParcelableExtra("pendingIntent");
//                    Intent data = new Intent();
//                    data.putExtra(Constants.KEY_SSID, reservation.getWifiConfiguration().SSID);
//                    data.putExtra(Constants.KEY_PRESHARE, reservation.getWifiConfiguration().preSharedKey);
//                    final String ipAddress = getLocalIpAddress();
//                    data.putExtra(Constants.KEY_IP, ipAddress);
//                    try {
//                        DebugLog.info("pendingIntent.send:" + ipAddress);
//                        if(pendingIntent != null) {
//                            pendingIntent.send(HotService.this, 200, data);
//                        }
//                    } catch (PendingIntent.CanceledException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onStopped() {
//                    super.onStopped();
//                }
//
//                @Override
//                public void onFailed(int reason) {
//                    super.onFailed(reason);
//                }
//            }, null);
//        }
//    }
//————————————————
//    版权声明：本文为CSDN博主「dingpwen」的原创文章，遵循CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
//    原文链接：https://blog.csdn.net/dingpwen/article/details/105071698
    /**
     * 创建Wifi热点
     */
    private void createWifiHotspot() {

        //192.168.43.59
                Log.d("ip", "+++createWifiHotspot:getWifiApIpAddress()" + getWifiApIpAddress() +
                        "\n");
        try {
                //        开启连接线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("ip", "+++createWifiHotspot-run:getWifiApIpAddress()" + getWifiApIpAddress()
                            );
                            String ip = getWifiApIpAddress();
                            if (ip != null) {
                            } else {
                                //一般Android手机是
                                ip = "192.168.43.1";
                            }
                            //本地路由开启通信
                            Socket socket = new Socket(ip, PORT);
                            connectThread = new ConnectThread(MainActivity.this, socket, handler);
                            connectThread.start();


                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    text_state.setText("创建通信失败");
                                }
                            });

                        }
                    }
                }).start();
                Thread.sleep(1000);

                //                listenerThread = new ListenerThread(PORT, handler);
                //                listenerThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            text_state.setText("创建热点失败");
        }
    }


    public String getWifiApIpAddress() {
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DEVICE_CONNECTING:
                    connectThread = new ConnectThread(MainActivity.this, listenerThread.getSocket(), handler);
                    connectThread.start();
                    break;
                case DEVICE_CONNECTED:
                    text_state.setText("设备连接成功");
                    break;
                case SEND_MSG_SUCCSEE:
                    text_state.setText("发送消息成功:" + msg.getData().getString("MSG"));
                    break;
                case SEND_MSG_ERROR:
                    text_state.setText("发送消息失败:" + msg.getData().getString("MSG"));
                    break;
                case GET_MSG:
                    text_state.setText("收到消息:" + msg.getData().getString("MSG"));
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
