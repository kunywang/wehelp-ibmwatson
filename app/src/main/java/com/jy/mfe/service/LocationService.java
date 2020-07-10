package com.jy.mfe.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;

import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jy.mfe.GenetekApp;
import com.jy.mfe.MainActivity;
import com.jy.mfe.R;
import com.jy.mfe.cache.AppCache;
import com.jy.mfe.rtm.ChatManager;
import com.jy.mfe.rtm.RtmNotifyBean;
import com.jy.mfe.util.CommonUtil;

import io.agora.rtm.RtmClient;

public class LocationService extends Service implements AMapLocationListener {

    public static final String TAG = LocationService.class.getSimpleName();

    private boolean mbLogined = false;
    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    public static boolean isLocated = false;

    public static void startLocationService(Context context){
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.startService(serviceIntent);
    }


    public static void stopLocationService(Context context){
        Intent serviceIntent = new Intent(context, LocationService.class);
        context.stopService(serviceIntent);
    }

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AMapLocationClient locationClient = new AMapLocationClient(this.getApplicationContext());
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        locationOption.setSensorEnable(true);
        locationOption.setOnceLocation(false);
        locationOption.setInterval(AppCache.getIns().softConfig.getLocateInterval()*1000);
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setGpsFirst(true);
        locationOption.setHttpTimeOut(30000);
        locationOption.setNeedAddress(true);
        locationOption.setOnceLocationLatest(false);
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);

        locationOption.setWifiScan(true);
        locationOption.setLocationCacheEnable(true);
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);
        locationClient.startLocation();


        mChatManager = GenetekApp.ins().getChatManager();
        mRtmClient = mChatManager.getRtmClient();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setNotification("genetek");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation.getErrorCode() == 0) {
            isLocated = true;
            int locType = aMapLocation.getLocationType();
            if(locType == AMapLocation.LOCATION_TYPE_GPS){
                AppCache.getIns().terminalHB.setLocate("GPS");
            } else if(locType == AMapLocation.LOCATION_TYPE_OFFLINE){
                AppCache.getIns().terminalHB.setLocate("offline");
            }else if(locType == AMapLocation.LOCATION_TYPE_LAST_LOCATION_CACHE){
                AppCache.getIns().terminalHB.setLocate("cache");
            }else {
                AppCache.getIns().terminalHB.setLocate("amap");
            }

            float fSpeedkmh = ((float)(Math.round((aMapLocation.getSpeed()*36/10)*10))/10);
            float fbearing = ((float)(Math.round(aMapLocation.getBearing()*10))/10);
            double dLon = ((float)(Math.round(aMapLocation.getLongitude()*1000000))/1000000);
            double dLat = ((float)(Math.round(aMapLocation.getLatitude()*1000000))/1000000);
            AppCache.getIns().terminalHB.setBearing(fbearing);
            AppCache.getIns().terminalHB.setSpeed(fSpeedkmh);
            if(aMapLocation.getLatitude() != 0){
                AppCache.getIns().terminalHB.setLatitude(dLat);
            }
            if(aMapLocation.getLongitude() != 0){
                AppCache.getIns().terminalHB.setLongitude(dLon);
            }

            AppCache.getIns().terminalHB.setHeight(aMapLocation.getAltitude());
            AppCache.getIns().terminalHB.setAddress(aMapLocation.getAddress());

        }else{
            AppCache.getIns().terminalHB.setLocate("invalid");
        }

        if(mChatManager.mIsInRtmChat == false){
            mChatManager.loginRtmChat(AppCache.getIns().terminalResult.getDeviceid());
        }else{
            sendLocationToStation();
        }
    }

    private void sendLocationToStation(){
        RtmNotifyBean rtmLoc = new RtmNotifyBean();
        rtmLoc.setTitle(RtmNotifyBean.RTM_TITLE_LOC);
        rtmLoc.setSender(AppCache.getIns().terminalResult.getName());
        rtmLoc.setDepartment(AppCache.getIns().terminalResult.getStation());
        rtmLoc.setData(JSONObject.toJSONString(AppCache.getIns().terminalHB));
        String peerID = CommonUtil.getStringMD5(AppCache.getIns().terminalResult.getStation());
        String content = JSONObject.toJSONString(rtmLoc);
        mChatManager.sendPeerMessage(content,peerID );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setNotification(String text) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = null;
        String CHANNEL_ONE_ID = "jy.com.mfe";
        NotificationManager mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            Uri mUri = Settings.System.DEFAULT_NOTIFICATION_URI;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ONE_ID, "driver", NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("Genetek");
            mChannel.setSound(mUri, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            mManager.createNotificationChannel(mChannel);

            notification = new Notification.Builder(this, CHANNEL_ONE_ID)
                    .setChannelId(CHANNEL_ONE_ID)
                    .setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setContentIntent(pi)
                    .build();
        } else {
            // 提升应用权限
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setContentIntent(pi)
                    .build();
        }
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(10001, notification);
    }
}
