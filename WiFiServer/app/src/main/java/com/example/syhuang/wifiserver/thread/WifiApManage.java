package com.example.syhuang.wifiserver.thread;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
//import com.tulip.common.util.LogU;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Administrator on 2020-3-24.

 * @author weijinsong
 * @date 9/16/19
 */
public class WifiApManage {
    private static final String AP_CONNECTED = "0x2";
    private static final String AP_DEVICE_TYPE = "wlan0";
    private static final int WIFI_NONE_TYPE = 0;
    private static final int WIFI_WEP_TYPE = 1;
    private static final int WIFI_WPA_TYPE = 2;
    private static final int WIFI_PSK_TYPE = 3;


    private static WifiApManage mWifiApManage;
    private WifiManager mWifiManager;
    private WifiConfiguration mWifiConfiguration;
    ConnectivityManager mConnectivityManager;

    public static WifiApManage newInstance(Context context) {
        if (null == mWifiApManage) {
            mWifiApManage = new WifiApManage(context);
        }
        return mWifiApManage;
    }

    public WifiApManage(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * 创建热点
     *
     * @param ssid   热点名称
     * @param passwd 热点密码
     * @param type   热点类型
     */
    public void startWifiAp(String ssid, String passwd, int type) {
        Method method;

        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        try {
            WifiConfiguration netConfig = new WifiConfiguration();

            netConfig.SSID = ssid;
            netConfig.preSharedKey = passwd;
            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

            switch (type) {
                case WIFI_NONE_TYPE:
                    netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    netConfig.preSharedKey = null;
                    break;
                case WIFI_WEP_TYPE:
                    netConfig.allowedKeyManagement.set(4);
                    break;
                case WIFI_WPA_TYPE:
                    netConfig.allowedKeyManagement.set(4);
                    break;
                case WIFI_PSK_TYPE:
                    netConfig.allowedKeyManagement.set(4);
                    break;
                default:
                    break;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                Method configMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                configMethod.invoke(mWifiManager, netConfig);
//                mConnectivityManager.startTethering(TETHERING_WIFI, true /* showProvisioningUi */,
//                        mOnStartTetheringCallback, new Handler(Looper.getMainLooper()));
                Log.d("wifi","Android Version:"+Build.VERSION.SDK_INT);
            } else {
                method = mWifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class, boolean.class);
                method.invoke(mWifiManager, netConfig, true);
                Log.d("wifi","Android Version:"+Build.VERSION.SDK_INT);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建热点
     * https://www.jianshu.com/p/2fa9f4557185
     * @param ssid   热点名称
     * @param passwd 热点密码
     * @param type   热点类型
     */
    public void startTethering(String ssid, String passwd, int type) {

        if (mWifiManager != null) {
            int wifiState = mWifiManager.getWifiState();
            boolean isWifiEnabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
            if (isWifiEnabled)
                mWifiManager.setWifiEnabled(false);
        }
        if (mConnectivityManager != null) {
            try {
                Field internalConnectivityManagerField = ConnectivityManager.class.getDeclaredField("mService");
                internalConnectivityManagerField.setAccessible(true);
                WifiConfiguration apConfig = new WifiConfiguration();
                apConfig.SSID = ssid;
                apConfig.preSharedKey = passwd;

                StringBuffer sb = new StringBuffer();
                Class internalConnectivityManagerClass = Class.forName("android.net.IConnectivityManager");
                ResultReceiver dummyResultReceiver = new ResultReceiver(null);
                try {

                    Method mMethod = mWifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                    mMethod.invoke(mWifiManager, apConfig);
//                    Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                    Method startTetheringMethod = mConnectivityManager.getClass().getDeclaredMethod("startTethering",
                            int.class,
                            ResultReceiver.class,
                            boolean.class);

                    startTetheringMethod.invoke(internalConnectivityManagerClass,
                            0,
                            dummyResultReceiver,
                            true);
                } catch (NoSuchMethodException e) {
                    Method startTetheringMethod = internalConnectivityManagerClass.getDeclaredMethod("startTethering",
                            int.class,
                            ResultReceiver.class,
                            boolean.class,
                            String.class);

//                    startTetheringMethod.invoke(internalConnectivityManagerClass,
//                            0,
//                            dummyResultReceiver,
//                            false,
//                            context.getPackageName());
                } catch (InvocationTargetException e) {
                    sb.append(11 + (e.getMessage()));
                    e.printStackTrace();
                } finally {
//                    log.setText(sb.toString());
                }


            } catch (Exception e) {
                Log.e("startTethering", Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 检查是否开启Wifi热点
     *
     * @return
     */
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (boolean) method.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭热点
     */
    @SuppressLint("ObsoleteSdkInt")
    public void closeWifiAp() {
        Method method ;
        if (isWifiApEnabled()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    method = mWifiManager.getClass().getDeclaredMethod("stopSoftAp");
                    method.invoke(mWifiManager);
                    Log.d("wifi","closeWifiApp");
                } else {
                    method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                    method.setAccessible(true);
                    WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                    Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    method2.invoke(mWifiManager, config, false);
                    Log.d("wifi","closeWifiApp");
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开热点
     */
    public void openWifiAp() {
        Method method;
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }

        if (!isWifiApEnabled()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mWifiConfiguration = getWifiApInfo();
                    Method configMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                    configMethod.invoke(mWifiManager, mWifiConfiguration);
                    method = mWifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                    //返回热点打开状态
                    method.invoke(mWifiManager, mWifiConfiguration);
                } else {
                    method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                    method.setAccessible(true);
                    WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                    Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                    method2.invoke(mWifiManager, config, true);
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public WifiConfiguration getWifiApInfo() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                mWifiConfiguration = (WifiConfiguration) method.invoke(mWifiManager);
//                LogU.e("enter ssid : " + mWifiConfiguration.SSID + " pwd : " + mWifiConfiguration.preSharedKey);
            } else {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                mWifiConfiguration = (WifiConfiguration) method.invoke(mWifiManager);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return mWifiConfiguration;
    }


    /**
     * 开热点手机获得其他连接手机IP的方法
     *
     * @return 其他手机IP 数组列表
     */
    public ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIp = new ArrayList<String>();
        BufferedReader br = null;
        FileReader fileReader = null;
        boolean flags = true;
        try {
            String line;
            fileReader = new FileReader("/proc/net/arp");
            br = new BufferedReader(fileReader);

            mWifiManager.getDhcpInfo().toString();

            while ((line = br.readLine()) != null) {
                if (!flags) {
                    final String[] splitted = line.split(" + ");
                    if (splitted != null && splitted.length >= 6) {
                        if (splitted[2].equals(AP_CONNECTED) && splitted[5].equals(AP_DEVICE_TYPE)) {
                            connectedIp.add(splitted[0] + " " + splitted[3]);
                        }
                    }
                }
                flags = false;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fileReader) {
                    fileReader.close();
                }

                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return connectedIp;
    }

    public String getClientDeviceName() {
        BufferedReader br = null;
        FileReader fileReader = null;

        try {
            fileReader = new FileReader("/data/misc/dhcp/dnsmasq.leases");
            br = new BufferedReader(fileReader);
            String line = "";
            while ((line = br.readLine()) != null) {
//                LogU.e("enter device name : " + line);
                if (line.indexOf("") != 1) {
                    String[] fields = line.split(" ");
                    //校验数据是不是破损
                    if (fields.length > 4) {
                        //返回第4个栏位

                        return fields[3];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fileReader) {
                    fileReader.close();
                }

                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

}
