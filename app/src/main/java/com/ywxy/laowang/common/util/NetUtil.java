package com.ywxy.laowang.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NetUtil {


    private static final char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F'};


    /**
     * 判断网络环境，如何有网络则为true
     *
     * @param paramContext
     * @return
     */
    public static boolean checkNet(Context paramContext) {
        return checkNet(paramContext, true);
    }

    public static boolean checkNet(Context paramContext, boolean needToast) {
        boolean net = false;
        try {
            NetworkInfo localNetworkInfo = ((ConnectivityManager) paramContext
                    .getSystemService("connectivity")).getActiveNetworkInfo();
            if ((localNetworkInfo != null)
                    && (localNetworkInfo.isConnectedOrConnecting())) {
                net = true;
            } else {
                net = false;
                if (needToast) {

                    Toast.makeText(paramContext, "您的网络连接已中断", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception localException) {
        }
        return net;
    }

    public static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI && activeNetInfo.isConnected();
    }

    public static String md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String md5sum(String filename) {
        InputStream fis;
        byte[] buffer = new byte[1024];
        int numRead = 0;
        MessageDigest md5;
        try {
            fis = new FileInputStream(filename);
            md5 = MessageDigest.getInstance("MD5");
            while ((numRead = fis.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
            }
            fis.close();
            return toHexString(md5.digest());
        } catch (Exception e) {
            System.out.println("error");
            return null;
        }
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * 获取联网的方式如：wifi
     */
    public static final String NET_NAME_NO_NET = "NO_NET";
    public static final String NET_NAME_UNKNOWN = "UNKNOWN";
    public static final String NET_NAME_WIFI = "WIFI";
    public static final String NET_NAME_2G = "2G";
    public static final String NET_NAME_3G = "3G";
    public static final String NET_NAME_4G = "4G";

    public static final int NET_TYPE_NO_NET = -1;
    public static final int NET_TYPE_UNKNOWN = 0;
    public static final int NET_TYPE_WIFI = (1 << 0);
    public static final int NET_TYPE_2G = (1 << 1);
    public static final int NET_TYPE_3G = (1 << 2);
    public static final int NET_TYPE_4G = (1 << 3);

    public static String networkTypeToName(int type) {
        switch (type) {
            case NET_TYPE_NO_NET:
                return NET_NAME_NO_NET;
            case NET_TYPE_WIFI:
                return NET_NAME_WIFI;
            case NET_TYPE_2G:
                return NET_NAME_2G;
            case NET_TYPE_3G:
                return NET_NAME_3G;
            case NET_TYPE_4G:
                return NET_NAME_4G;
        }
        return NET_NAME_UNKNOWN;
    }

    public static String getNetworkName(Context context) {
        return networkTypeToName(getNetworkType(context));
    }

    private static long lastGetNetworkTypeTime = System.currentTimeMillis();
    private static int cacheNetworkType = NET_TYPE_UNKNOWN;

    public static int getNetworkTypeCache(Context context, int cacheMs) {
        if (System.currentTimeMillis() - lastGetNetworkTypeTime > cacheMs) {
            cacheNetworkType = getNetworkType(context);
            lastGetNetworkTypeTime = System.currentTimeMillis();
        }
        return cacheNetworkType;
    }

    public static int getNetworkType(Context context) {
        int ret = NET_TYPE_UNKNOWN;
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isAvailable()) {
                // 注意一：
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
                // 但是有些电信机器，仍可以正常联网，
                // 所以当成net网络处理依然尝试连接网络。
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。
                ret = NET_TYPE_NO_NET;
            } else {
                // NetworkInfo不为null开始判断是网络类型
                int netType = networkInfo.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    // wifi net处理
                    ret = NET_TYPE_WIFI;
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {
                    // 注意二：
                    // 判断是否电信wap:
                    // 不要通过getExtraInfo获取接入点名称来判断类型，
                    // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
                    // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
                    // 所以可以通过这个进行判断！

                    // boolean is3G = isFastMobileNetwork(context);
                    // return is3G ? "3G":"2G";

                    TelephonyManager telephonyManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    switch (telephonyManager.getNetworkType()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            ret = NET_TYPE_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case 12: // TelephonyManager.NETWORK_TYPE_EVDO_B
                        case 14: // TelephonyManager.NETWORK_TYPE_EHRPD
                        case 15: // TelephonyManager.NETWORK_TYPE_HSPAP
                            ret = NET_TYPE_3G;
                            break;
                        case 13: // TelephonyManager.NETWORK_TYPE_LTE // API level 11
                            ret = NET_TYPE_4G;
                            break;
                        default:
                            ret = NET_TYPE_UNKNOWN;
                            break;
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            ret = NET_TYPE_UNKNOWN;
        }
        return ret;
    }

    /**
     * 获取运营商信息
     *
     * @return
     */
    public static String getMno(Context activity) {
        String str = "";
        try {
            TelephonyManager tel = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            str = tel.getSimOperatorName();
            if (str != null && !"".equals(str)) {
                return str;
            }

            String IMSI = tel.getSubscriberId();
            if (IMSI == null) {
                return "";
            }

            if (IMSI.startsWith("46000"))
                str = "中国移动";
            else if (IMSI.startsWith("46002"))
                str = "中国移动";
            else if (IMSI.startsWith("46001"))
                str = "中国联通";
            else if (IMSI.startsWith("46003"))
                str = "中国电信";
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return str;
    }

    public static String getLocalMacAddress() {
        String mac = "";
        try {
            String path = "sys/class/net/eth0/address";
            FileInputStream fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[1024 * 8];
            int byteCount_name = fis_name.read(buffer_name);
            if (byteCount_name > 0) {
                mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }

            if (mac.length() == 0 || mac == null) {
                path = "sys/class/net/eth0/wlan0";
                FileInputStream fis = new FileInputStream(path);
                byte[] buffer = new byte[1024 * 8];
                int byteCount = fis.read(buffer);
                if (byteCount > 0) {
                    mac = new String(buffer, 0, byteCount, "utf-8");
                }
            }

            if (mac.length() == 0 || mac == null) {
                return "";
            }
        } catch (Exception io) {

        }
        return mac.trim();
    }
}
