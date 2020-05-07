package com.example.myserver;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServerActivity extends Activity {
	private static final int SOCKET_PORT = 8080;
	public static ArrayList<SocketBean> mSocketList = new ArrayList<SocketBean>();

	private static final String TAG = "ChatServerActivity";
	ServerSocket serverSocket;//创建ServerSocket对象
	Socket clicksSocket;//连接通道，创建Socket对象
	ExecutorService executorService;   // 创建线程池
	MainActivity.Receive_Thread receive_Thread;
	Button startButton;//发送按钮
	EditText portEditText, ipEditText;//端口号和IP
	TextView receiveTextView;//接收消息框
	Button sendButton;//发送按钮
	EditText sendEditText;//发送消息框
	InputStream inputstream;//创建输入数据流
	OutputStream outputStream;//创建输出数据流

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
		receiveTextView = (TextView) findViewById(R.id.client_num_TextView);
		sendButton = (Button) findViewById(R.id.send_button);
//		sendEditText = (EditText) findViewById(R.id.message_EditText);

		startButton.setOnClickListener(startButtonListener);
		sendButton.setOnClickListener(sendButtonListener);

		textView[0] = findViewById(R.id.receive_TextView1);
		textView[1] = findViewById(R.id.receive_TextView2);
		textView[2] = findViewById(R.id.receive_TextView3);
		textView[3] = findViewById(R.id.receive_TextView4);

//		executorService = Executors.newCachedThreadPool();
	}

	/**
	 * 启动服务按钮监听事件
	 */
	private View.OnClickListener startButtonListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/**
			 * 启动服务器监听线程
			 */
//            ClientCode = 0;
			ChatServer chatServer = new ChatServer();
			chatServer.initServer();
		}
	};
	private View.OnClickListener sendButtonListener = new View.OnClickListener() {

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
}
