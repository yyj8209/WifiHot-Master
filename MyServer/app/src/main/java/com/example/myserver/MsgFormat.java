package com.example.myserver;

public class MsgFormat {
    public String ip;
    public byte[] data;
    public int len;

    public MsgFormat(String ip, byte[] data, int len) {
        this.ip = ip;
        this.data = data;
        this.len = len;
    }
}

