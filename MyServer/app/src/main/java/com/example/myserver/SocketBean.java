package com.example.myserver;

import java.net.Socket;

public class SocketBean {
	public int id;
	public String ip;
	public Socket socket;
	public String deviceId;
	public String nickName;
	public String loginTime;
	
	public SocketBean(int id, String ip, Socket socket) {
		this.id = id;
		this.ip = ip;
		this.socket = socket;
		this.deviceId = "";
		this.nickName = "";
		this.loginTime = "";
	}

}
