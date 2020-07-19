package com.wayful.DataProcessing;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;

public class Data_syn {

    // 将字节数组转化为16进制字符串，确定长度
    public static String bytesToHexString(byte[] bytes, int a) {
        String result = "";
        for (int i = 0; i < a; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);// 将高24位置0
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }
    // 将字节数组转化为16进制字符串，不确定长度
    public static String Bytes2HexString(byte[] b) {
        String ret = "";
        for (int i =0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);// 将高24位置0
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
    // 将16进制字符串转化为字节数组
    public static byte[] hexStr2Bytes(String paramString) {
        int i = paramString.length() / 2;

        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true) {
            if (j >= i)
                return arrayOfByte;
            int k = 1 + j * 2;
            int l = k + 1;
            arrayOfByte[j] = (byte) (0xFF & Integer.decode(
                    "0x" + paramString.substring(j * 2, k)
                            + paramString.substring(k, l)).intValue());
            ++j;
        }
    }

    /**
     * 将double转为数值，并最多保留num位小数。例如当num为2时，1.268为1.27，1.2仍为1.2；1仍为1，而非1.00;100.00则返回100。
     *
     * @param d
     * @param num 小数位数
     * @return
     */
    public static String double2String(double d, int num) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(num);//保留两位小数
        nf.setGroupingUsed(false);//去掉数值中的千位分隔符

        String temp = nf.format(d);
        if (temp.contains(".")) {
            String s1 = temp.split("\\.")[0];
            String s2 = temp.split("\\.")[1];
            for (int i = s2.length(); i > 0; --i) {
                if (!s2.substring(i - 1, i).equals("0")) {
                    return s1 + "." + s2.substring(0, i);
                }
            }
            return s1;
        }
        return temp;
    }

    /**
     * 将double转为数值，并最多保留num位小数。
     *
     * @param d
     * @param num 小数个数
     * @param defValue 默认值。当d为null时，返回该值。
     * @return
     */
    public static String double2String(Double d, int num, String defValue){
        if(d==null){
            return defValue;
        }

        return double2String(d,num);
    }

    /**
     * 将一组16进制（字符）数组，按串口格式，转化为三组float信号。
     *
     * @param bytes 串口数据
     * @param a 数据长度
     * @return
     * 分两步实现：1、把16进制的bytes转换为float类型，每组8个数；2、取出其中第2、3、4 即为3个通道值。
     */
    public static float[][] bytesToFloat(byte[] bytes, int a, int BytesPerRow) {
        // 1、把16进制的bytes（字符）转换为float类型，每组8个数
        int len = a/4;     // 接收的float 数据个数。
        float []data = new float[len];
        byte[] b ={(byte) 0x00,0x00,0x00,0x00};
        for (int i = 0; i < len; i++){
            for(int j = 0; j < 4; j++)    // 把4 个字节转化为 float 数值。
                b[j] = bytes[4*i + j];
            ByteBuffer buf=ByteBuffer.allocateDirect(4); //无额外内存的直接缓存
            buf=buf.order( ByteOrder.LITTLE_ENDIAN); // 默认大端，小端用这行（低位在前）
            buf.put(b);
            buf.rewind();
            data[i] = buf.getFloat();
        }
        // 2、取出其中第2、3、4 即为3个通道值。从文件读取数据时，为1、2、3通道。
        int len1 = a/BytesPerRow;    // 目前测试：回传数据均为32的整数倍。
        int BEGIN = (BytesPerRow -24)/8;   // 32个字节时，第2、3、4数据即为3个通道值。24字节时为1、2、3 数据。
        float [][]result = new float[3][len1];
        for (int i = 0; i < 3; i++) {
            for(int j = 0; j < len1; j++)
                result[i][j] = data[BEGIN + BytesPerRow/4*j + i];
        }
        return result;
    }
    /**
     * 将一组数字数组，按串口格式，转化为三组float信号。
     *
     * @param bytes 串口数据
     * @param a 数据长度
     * @return
     * 分两步实现：1、把10进制的bytes转换为float类型，取出其中第2、3、4 即为3个通道值。
     * 考虑到可能存在的数据丢失，先转换为字符串，检测包头和包尾，丢弃无效数据再转为Float。2020.6.28
     * 用于探测器的数据直接显示。
     */
    public static float[][] BytesToFloat(byte[] bytes, int a, int BytesPerRow) {
        // 1、把16进制的bytes 转换为字符串，检测包头和包尾,去年污损的包。
        int Len = bytes.length;
        String HexString = bytesToHexString(bytes, Len);
        String Head = "CC000103";
        String StringExHead = "";
        int nTokensExHead = 2*BytesPerRow - Head.length();
        String[] sTokens = HexString.split(Head);
        for(String s:sTokens){
            if(s.length() == nTokensExHead)
                StringExHead += s;
        }
        String Tail = "FFFCFFFF";
        String StringExTail = "";
        int nTokensExTail = nTokensExHead - Tail.length();
        String[] sTokensExTail = StringExHead.split(Tail);
        for(String s:sTokensExTail){
            if(s.length() == nTokensExTail)
                StringExTail += s;
        }
//        Log.d("Debug","本帧输出数据：" + StringExTail);

        // 2、转换为Float类型。
        byte[] NewBytes = hexStr2Bytes(StringExTail);
        float [][]result = bytesToFloat(NewBytes, NewBytes.length, BytesPerRow-8);
        return result;

    }
}
