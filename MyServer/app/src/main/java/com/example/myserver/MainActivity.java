package com.example.myserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.UiThread;


public class MainActivity extends Activity {

    public static final int MAXCLIENT = 8;
    public static final int CLIENT_LOGIN = 1;
    public static final int REFRESH = 2;
    public static final int CLIENT_LOGOUT = 3;
    private static Context context;
    private static final String TAG = "MainActivity";
    private static final String TAG_D = "TAG_DEBUG";
//    ServerSocket serverSocket;//创建ServerSocket对象
//    Socket clicksSocket;//连接通道，创建Socket对象
//    ExecutorService executorService;   // 创建线程池
//    Receive_Thread receive_Thread;
    private Button startButton;//发送按钮
    private EditText portEditText, ipEditText, clientNumEditText;//端口号和IP
    private TextView  receiveTextView;//接收消息框
    private Button sendButton;//发送按钮
    private Spinner spinner;
//    InputStream inputstream;//创建输入数据流
//    OutputStream outputStream;//创建输出数据流
    private ConcurrentHashMap<String, Object> ClientMap;   // Set of clients(IP)
    private List<ConcurrentHashMap<String, Object>> ClientList = new CopyOnWriteArrayList<>();
    private List<ConcurrentHashMap<String, Object>> ClientMapList = new CopyOnWriteArrayList<>();
    public static final String CLIENT_IP = "IP";
    public static final String CLIENT_SOCKET = "CLIENTSOCKET";

//    public static final String Client[] = {"No.1:", "No.2:", "No.3:", "No.4:", "No.5:", "No.6:", "No.7:", "No.8:"};
//    public int ClientCode = 0;
    public String CurrentClient;
    public boolean isStart = true;
    public TextView textView[] = new TextView[MAXCLIENT];
    private LineChart lineChart[] = new LineChart[MAXCLIENT];
    private myLineChart mylinechart[] = new myLineChart[MAXCLIENT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 读一下手机wifi状态下的ip地址，只有知道它的ip才能连接它嘛
         */
//        Toast.makeText(MainActivity.this, getLocalIpAddress(), Toast.LENGTH_LONG).show();
        context = getApplicationContext();

        startButton = (Button) findViewById(R.id.start_button);
        portEditText = (EditText) findViewById(R.id.port_EditText);
        ipEditText = (EditText) findViewById(R.id.ip_EditText);
        clientNumEditText = (EditText) findViewById(R.id.client_num_TextView);
        receiveTextView = (TextView) findViewById(R.id.receive_TextView);
        sendButton = (Button) findViewById(R.id.send_button);
        spinner = (Spinner) findViewById(R.id.lv_client);
//        sendEditText.requestFocus();
        receiveTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        startButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startServer();
            }
        });
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String strMsg = CurrentClient;
                sendMsg(CurrentClient,strMsg);
            }
        });
//        spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                CurrentClient = ClientList.get(i).get(CLIENT_IP).toString();
//                Toast.makeText(context,ClientList.get(i).get(CLIENT_IP).toString(),Toast.LENGTH_LONG).show();
//            }
//        });


        textView[0] = findViewById(R.id.receive_TextView1);
        textView[1] = findViewById(R.id.receive_TextView2);
        textView[2] = findViewById(R.id.receive_TextView3);
        textView[3] = findViewById(R.id.receive_TextView4);
        textView[4] = findViewById(R.id.receive_TextView5);
        textView[5] = findViewById(R.id.receive_TextView6);
        textView[6] = findViewById(R.id.receive_TextView7);
        textView[7] = findViewById(R.id.receive_TextView8);

        lineChart[0] = findViewById(R.id.line_chart1);
        lineChart[1] = findViewById(R.id.line_chart2);
        lineChart[2] = findViewById(R.id.line_chart3);
        lineChart[3] = findViewById(R.id.line_chart4);
        lineChart[4] = findViewById(R.id.line_chart1);
        lineChart[5] = findViewById(R.id.line_chart1);
        lineChart[6] = findViewById(R.id.line_chart1);
        lineChart[7] = findViewById(R.id.line_chart1);
        for(int i=0; i<MAXCLIENT; i++){
            mylinechart[i] = new myLineChart(lineChart[i]);
            mylinechart[i].initChartSyn();
        }

//        executorService = Executors.newCachedThreadPool();

    }
    /**
     * 启动服务按钮监听事件
     */
    private void startServer() {

        ipEditText.setText(getHostIpAddress());
        new Thread(new Runnable() {
            @Override
            public void run() {

                ExecutorService executorService = Executors.newCachedThreadPool();
                final int port = Integer.valueOf(portEditText.getText().toString());//获取portEditText中的端口号
                try {
                    ServerSocket serverSocket = new ServerSocket(port);//监听port端口，这个程序的通信端口就是port了
                    while (isStart) {
                        //监听连接 ，如果无连接就会处于阻塞状态，一直在这等着
                        Socket clicksSocket = serverSocket.accept();
                        clicksSocket.setSoTimeout(100000);
                        CurrentClient = getClientIpAddress(clicksSocket);
//                        final SocketBean socketBean = new SocketBean(ClientList.size()+1,CurrentClient, clicksSocket);

                        if (clicksSocket.isConnected()) {
                            ClientMap  = new ConcurrentHashMap<>();
                            ClientMap.put(CLIENT_IP,CurrentClient);
                            ClientMap.put(CLIENT_SOCKET,clicksSocket);    // Insert client to clientset.
                            ClientList.add(ClientMap);    // 更新 List
                            ClientMapList.add(ClientMap);     // 
                            updateListView();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    receiveTextView.setText( CurrentClient+" 接入");
                                    clientNumEditText.setText( String.valueOf(ClientList.size()));
                                }
                            });
//                            sendMsg(CurrentClient,CurrentClient);
                            //启动接收线程
//                            Log.d(TAG_D, "客户端数量：" + ClientMap.size());
//                            int scrollAmount = receiveTextView.getLayout().getLineTop(receiveTextView.getLineCount())
//                                    - receiveTextView.getHeight();
//                            if (scrollAmount > 0)
//                                receiveTextView.scrollTo(0, scrollAmount);
//                            else
//                                receiveTextView.scrollTo(0, 0);
                            Thread.sleep(10);
                            Receive_Thread receive_Thread = new Receive_Thread(clicksSocket);
                            receive_Thread.start();
//                            executorService.execute(receive_Thread);
                        }
                    }
                    serverSocket.close();     // 关闭服务器，所有线程，可以用清理线程池的方式。
                    //                executorService.shutdownNow();
                } catch (IOException | InterruptedException e) {   // InterruptedException 为sleep 的异常
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
//                    if (serverSocket != null) {
//                        try {
//                            //                        isStart = false;
//                            Log.d(TAG_D, "接收线程关闭");
//                            //                        executorService.shutdown();
//                            //                        inputstream.close();
//                            //                        outputStream.close();
//                            serverSocket.close();
//                            receive_Thread.interrupt();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }
            }
        }).start();
    };

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
            Log.d(TAG_D,"启动新的接收线程("+getClientIpAddress(socket)+")");
        }

        @Override
        public void run()//重写run方法
        {
            try
            {
                while(true){
                    final byte[] buf = new byte[1024];
                    final InputStream is = socket.getInputStream();
                    final OutputStream os = socket.getOutputStream();   // 需要和线程对应起来
                    final int len = is.read(buf);
                    final String StrRecv = new String(buf,0,len);
                    final String CurrentClientIP = getClientIpAddress(socket);
                    Log.d(TAG_D,"接收到来自"+CurrentClientIP+"的信息："+StrRecv);
                    if(StrRecv.toLowerCase().contains("disconnect")){
//                        ClientMap.remove(CurrentClientIP);   // 这种删除方式先存疑   2020.05.05
//                        ClientList.remove(getClientIpAddress(socket));    // 更新 List
//                        ClientMap.remove(userIP2SocketBean(ClientMap,getClientIpAddress(socket)));
//                        Log.d(TAG_D,"当前客户端数量："+ClientMap.size());
                        removeClient(CurrentClientIP);    // 更新 List
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                receiveTextView.setText(CurrentClientIP+" 退出");
                                clientNumEditText.setText( String.valueOf(ClientList.size()));
                            }
                        });
//                        int scrollAmount = receiveTextView.getLayout().getLineTop(receiveTextView.getLineCount())
//                                - receiveTextView.getHeight();
//                        if (scrollAmount > 0)
//                            receiveTextView.scrollTo(0, scrollAmount);
//                        else
//                            receiveTextView.scrollTo(0, 0);
                        updateListView();
                        is.close();
                        os.close();
                        socket.close();
                    }else{
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                textView[getClientIndex(CurrentClientIP)].setText(CurrentClientIP);
                                mylinechart[getClientIndex(CurrentClient)].refreshLineChart(buf,len);
                            }
                        });

//                        Message message = Message.obtain();
//                        message.what = REFRESH;
////                        message.arg1 = Integer.parseInt(CurrentClientIP);
//                        message.obj = CurrentClientIP;
//                        handler.sendMessage(message);
                    }
                }
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * handler对象，用来接收消息~现在没有调通 20200514
     *///
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {  //这个是发送过来的消息
            // 处理从子线程发送过来的消息
            int arg1 = msg.arg1;  //获取消息携带的属性值
            int what = msg.what;
            Object result = msg.obj;
            switch (what){
                case CLIENT_LOGIN:
                    Log.d(TAG_D,"消息处理：用户接入" + result.toString());
                case REFRESH:
                    Log.d(TAG_D,"消息处理：处理数据" + result.toString());
                    Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_LONG).show();
                case CLIENT_LOGOUT:
                    Log.d(TAG_D,"消息处理：用户退出" + result.toString());
            }
//            Bundle bundle = msg.getData(); // 用来获取消息里面的bundle数据
        };
    };
    /**
     * 发送消息按钮事件
     */
    private void sendMsg(String userIP,String strMsg) {
        final Socket CurrentSocket = getClientSocket(userIP);
        final String msg = strMsg;
//        Toast.makeText(context, "reserved", Toast.LENGTH_LONG).show();
        Log.d(TAG_D,"Client IP: "+getClientIpAddress(CurrentSocket));
        // TODO Auto-generated method stub
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                        OutputStream outputStream = CurrentSocket.getOutputStream();
                        if (outputStream != null) {
                            outputStream.write(msg.getBytes("utf-8"));
//                            outputStream.flush();
                        Thread.sleep(50);
                        outputStream.close();
                        }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        if(CurrentSocket!=null){
            try{
                CurrentSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    };

    private SocketBean userIP2SocketBean(ArrayList<SocketBean> arrayList,String userIP){
        SocketBean socketBean = null;
//        String ip = new String("0.0.0.0");
//        Socket socket = null;
        Iterator iterator = arrayList.iterator();
        while(iterator.hasNext()){
            socketBean = (SocketBean)iterator.next();
//            ip = socketBean.ip;
//            socket = socketBean.socket;
            if(!userIP.equals(socketBean.ip))
                break;
        }
        return socketBean;
    }
    private Socket userIP2Socket(ConcurrentHashMap<String, Socket> hashMap,String userIP) {
        Socket socket = null;
        for (Object key : hashMap.keySet()) {
            socket = hashMap.get(key);
            if (userIP.equals(key))
                break;
        }
        return socket;
    }
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
        return socket.getInetAddress().toString().replace("/", "");
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

    private void updateListView(){

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
//                ClientList.clear();
                SimpleAdapter adapter=new SimpleAdapter
                    (context, ClientList, R.layout.client_item,
                            new String[]{CLIENT_IP}, new int[]{R.id.client});
                spinner.setAdapter(adapter);
//                listView.setAdapter(new myListAdapter(ClientList, context));
//                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            }
        });
    }

    private void removeClient(String CurrentClient){

        for(int i=0; i<ClientList.size(); i++){
            if(CurrentClient.equals(ClientList.get(i).get(CLIENT_IP))){
                ClientList.remove(i);
                ClientMapList.remove(i);     //
                break;
            }
        }
    }

    private int getClientIndex(String CurrentClient) {
        int i = 0;
        for (; i < ClientList.size(); i++) {
            if (CurrentClient.equals(ClientList.get(i).get(CLIENT_IP))) {
                break;
            }
        }
        return i;
    }

    private Socket getClientSocket(String CurrentClient) {
        Socket socket = null;
        for (int i = 0; i < ClientList.size(); i++) {
            if (CurrentClient.equals(ClientMapList.get(i).get(CLIENT_IP))) {
                socket = (Socket) ClientList.get(i).get(CLIENT_SOCKET);
                break;
            }
        }
        return socket;
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
    @Override
    public void onBackPressed()
    {
        exit();
    }

    public void exit()
    {
        boolean exit = true;


        if(exit)
        {
            this.finish();
        }

    }
}