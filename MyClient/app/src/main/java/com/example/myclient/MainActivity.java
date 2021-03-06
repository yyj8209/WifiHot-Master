package com.example.myclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    private TextView tv_state, tv_send, tv_receive;
    private Button btn_connect, btn_disconnect, btn_send, btn_clear;
    private EditText et_ip, et_port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_state = (TextView)findViewById(R.id.tv_state);
        tv_send = (TextView)findViewById(R.id.tv_send);
        tv_receive = (TextView)findViewById(R.id.tv_receive);
        btn_connect = (Button)findViewById(R.id.bt_connect);
        btn_disconnect = (Button)findViewById(R.id.bt_disconnect);
        btn_send = (Button)findViewById(R.id.bt_send);
        btn_clear = (Button)findViewById(R.id.bt_clear);
        et_ip = (EditText)findViewById(R.id.ed_ip);
        et_port = (EditText)findViewById(R.id.ed_port);

        initListener();
        initDataReceiver();
    }

    private void initListener(){
        //socket connect
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = et_ip.getText().toString();
                String port = et_port.getText().toString();

                if(TextUtils.isEmpty(ip)){
                    Toast.makeText(MainActivity.this,"IP地址为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(port)){
                    Toast.makeText(MainActivity.this,"端口号为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                connect(ip, Integer.parseInt(port));
            }
        });

        //socket disconnect
        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        //socket send
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TCPClient.getInstance().isConnect()) {
                    byte[] data = tv_send.getText().toString().getBytes();
                    send(data);
                } else {
                    Toast.makeText(MainActivity.this,"尚未连接，请连接Socket",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //clear receive
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_receive.setText("");
            }
        });
    }

    /**
     * socket data receive
     * */
    private void initDataReceiver(){
        TCPClient.getInstance().setOnDataReceiveListener(dataReceiveListener);
    }

    /**
     * socket connect
     * */
    private void connect(String ip, int port){
        TCPClient.getInstance().connect(ip, port);
    }

    /**
     * socket disconnect
     * */
    private void disconnect(){
        TCPClient.getInstance().disconnect();
        tv_state.setText("未连接");
    }

    /**
     * socket send
     * */
    private void send(byte[] data){
        TCPClient.getInstance().sendByteCmd(data,1001);
    }


    /**
     * socket data receive
     * data(byte[]) analyze
     * */
    private TCPClient.OnDataReceiveListener dataReceiveListener = new TCPClient.OnDataReceiveListener() {
        @Override
        public void onConnectSuccess() {
            Log.i(TAG,"onDataReceive connect success");
            tv_state.setText("已连接");
        }

        @Override
        public void onConnectFail() {
            Log.e(TAG,"onDataReceive connect fail");
            tv_state.setText("未连接");
        }

        @Override
        public void onDataReceive(byte[] buffer, int size, int requestCode) {
            //获取有效长度的数据
            byte[] data = new byte[size];
            System.arraycopy(buffer, 0, data, 0, size);

            final String oxValue = String.valueOf(data); // Arrays.toString(data);  // HexUtil.Byte2Ox(data));
            Log.i(TAG,"onDataReceive requestCode = "+requestCode + ", content = "+oxValue);

            tv_receive.setText(tv_receive.getText().toString() + oxValue + "\n");

        }
    };



    @Override
    protected void onDestroy() {
        TCPClient.getInstance().disconnect();
        super.onDestroy();
    }
}
