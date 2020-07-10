package com.jy.mfe.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;

public class CommonUtil {
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        float px = dpValue * scale;
        int pxInt = (int)px;
        return px == pxInt? pxInt: pxInt + 1;
    }

    public static boolean isGPSEnabled(Context context) {
        boolean enabled = false;
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null)
        {
            enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        return enabled;
    }

    public static boolean isLocationEnabled(Context context) {
        boolean enabled = false;
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);

        if(locationManager != null)
        {
            enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }

        return enabled;
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean available = false;
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm != null)
        {
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if(networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected())
            {
                available = true;
            }
        }

        return available;
    }

    public static boolean isMainProcess(Context context) {
        String processName = getProcessName(context);
        return processName != null && processName.equals(context.getPackageName());
    }

    public static String getProcessName(Context context) {
        String processName = null;
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = activityManager == null? null: activityManager.getRunningAppProcesses();

        if(processInfos != null && !processInfos.isEmpty())
        {
            int pid = android.os.Process.myPid();

            for(ActivityManager.RunningAppProcessInfo processInfo: processInfos)
            {
                if(processInfo.pid == pid)
                {
                    processName = processInfo.processName;
                    break;
                }
            }
        }

        return processName;
    }

    public static void virbrate(Context context, Long time) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (null != vibrator) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(time,DEFAULT_AMPLITUDE));
            }else {
                vibrator.vibrate(time);
            }
        }

    }


    public static String permissionPermanentlyDenied(String... permissions ) {
        StringBuffer sb = new StringBuffer();
        for(String per : permissions) {
            switch (per) {
                case  Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    sb.append("SD卡 ");
                    break;
                case  Manifest.permission.CAMERA:
                    sb.append("相机 ");
                    break;
                case   Manifest.permission.RECORD_AUDIO:
                    sb.append("录音 ");
                    break;
                case  Manifest.permission.READ_PHONE_STATE:
                    sb.append("电话 ");
                    break;
                default:
            }
        }
        return sb.toString();
    }

    public static String getStringMD5(String text) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        String md5String = "";
        try {
            MessageDigest instance = MessageDigest.getInstance("MD5");
            byte[] bytes = instance.digest(text.getBytes());
            StringBuffer sb = new StringBuffer();
            for(Byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                sb.append(temp);
            }
            md5String = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5String;
    }

    public static boolean isServiceRunning(Context context, String serviceName) {
        if (("").equals(serviceName) || serviceName == null) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = null;
        if (myManager != null) {
            runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                    .getRunningServices(300);
        }
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName()
                    .equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

}
