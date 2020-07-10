package com.jy.mfe.cache;

import android.content.Context;

import com.jy.mfe.bean.DeviceConfig;
import com.jy.mfe.bean.TerminalHeartBeat;
import com.jy.mfe.bean.TerminalInfo;

/**
 * @author kunpn
 */
public class AppCache {
    private static final String SP_NAME_APP = "JFMFEAPP";
    private static final String SP_KEY_DID = "deviceId";
    private static final String SP_KEY_SVRIP = "serverip";
    private static final String SP_KEY_SVRPORT = "serveport";
    private static final String SP_KEY_UDPIP = "udpip";
    private static final String SP_KEY_UDPPORT = "udpeport";
    private static final String SP_KEY_CONFIG = "vidoeconfig";

    public static final int WORKSTATE_NORMAL = 0x00;
    public static final int WORKSTATE_VIDEOSENDING = 0x02;
    public static final int WORKSTATE_RECORDING = 0x04;

    private static AppCache instance ;
    public static AppCache getIns(){
        if(instance==null) {
            synchronized (AppCache.class) {
                if(instance==null) {
                    instance = new AppCache();

                }
            }
        }
        return instance;
    }

    public  TerminalHeartBeat terminalHB = new TerminalHeartBeat();
    public  TerminalInfo terminalResult = new TerminalInfo();
    public  int m_nProtalVersion = 1;
    public  int m_nDeviceType = 201;
    public DeviceConfig softConfig = new DeviceConfig();

    private boolean isLogined = false;
    public boolean getLoginState(){
        return isLogined;
    }
    public void setLoginState(boolean logined){
        isLogined = logined;
    }

    public String getDeviceId(Context context){
        String did = context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).getString(SP_KEY_DID, "00110011");
        terminalHB.setDeviceid(did);
        terminalResult.setDeviceid(did);
        return did;
    }

    public void setDeviceId(Context context, String strID){
        terminalHB.setDeviceid(strID);
        terminalResult.setDeviceid(strID);
        context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).edit().putString(SP_KEY_DID, strID).apply();
    }

    public String getServerIP(Context context){
        String did = context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).
                getString(SP_KEY_SVRIP, "118.190.158.181");
        return did;
    }

    public String getServerPort(Context context){
        String did = context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).
                getString(SP_KEY_SVRPORT, "8082");
        return did;
    }

    public void getDeviceConfig(Context context){
        String did = context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).
                getString(SP_KEY_CONFIG, "");

       // try {
         //   softConfig= JSON.parseObject(did, DeviceConfig.class);
        //}catch (Exception e){
            softConfig = new DeviceConfig();
            softConfig.setAutoRecord(1);
            softConfig.setLocateInterval(120);
            softConfig.setVideoBitRate(1024);
            softConfig.setVideoHeight(720);
            softConfig.setVideoWidth(1280);
       // }

        return ;
    }

    public void setDeviceConfig(Context context, DeviceConfig dc){
        context.getSharedPreferences(SP_NAME_APP, Context.MODE_PRIVATE).edit().putString(SP_KEY_CONFIG, dc.toString()).apply();
    }

}
