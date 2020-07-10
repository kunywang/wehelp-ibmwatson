package com.jy.mfe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baidubce.BceClientException;
import com.baidubce.BceServiceException;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.GetObjectRequest;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.jy.mfe.MainActivity;
import com.jy.mfe.bean.PropertySet;
import com.jy.mfe.bean.ScheduleBase;
import com.jy.mfe.bean.ScheduleEvent;
import com.jy.mfe.cache.AppCache;
import com.jy.mfe.http.HK;
import com.jy.mfe.http.HttpPost;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kunpn
 */
public class ScheduleService extends Service {
    private static final String SP_NAME = "ScheduleHouse";
    private static final String SP_KEY = "ScheduleEvent";
    private static ScheduleService instance ;
    private List<ScheduleEvent> mScheduleList = null;
    private  final Object syncList = new Object();
    public static ScheduleService getIns(){
        return instance;
    }
    private Context mContext;
    private Calendar calendar= null;
    private Map<String, String> mAudioFileMap = new HashMap<>();
    public ScheduleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        instance = this;

        loadList();
    }

    public static void startService(Context context){
        Intent serviceIntent = new Intent(context, ScheduleService.class);
        context.startService(serviceIntent);
    }


    public static void stopService(Context context){
        Intent serviceIntent = new Intent(context, ScheduleService.class);
        context.stopService(serviceIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.sendEmptyMessage(mTimerCheck);
        return super.onStartCommand(intent, flags, startId);
    }

    private final int mTimerCheck= 200000 ;
    private final int mTimerCheckTime = 1000 *60;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int nMsg = msg.what;
            switch (nMsg)
            {
                case mTimerCheck:
                {
                    checkSchedule();
                    mHandler.sendEmptyMessageDelayed(mTimerCheck, mTimerCheckTime);
                }break;

                default:break;
            }

        };
    };

    private void checkSchedule(){
        synchronized (syncList){
            if(mScheduleList == null){
                return;
            }
            if(mScheduleList.size() <= 0){
                return;
            }
            Log.e("RTM", "checkSchedule: "+  mScheduleList.size());
            for(int ei = mScheduleList.size()-1; ei >= 0; ei--){
                ScheduleEvent se = mScheduleList.get(ei);
                if(se.getType() == ScheduleBase.SE_TYPE_LOOP){
                    checkLoopEvent(se);
                }else if(se.getType() == ScheduleBase.SE_TYPE_TIMER){
                    checkTimeEvent(se);
                }
            }
        }
    }

    private void checkTimeEvent(ScheduleEvent se){

        calendar = Calendar.getInstance();
        switch (se.getTimeScheduleType()){
            case ScheduleBase.SE_TIME_LOOPER_NON:{
                if( se.getTimeScheduleMonth() == calendar.get(Calendar.MONTH)
                        && se.getTimeScheduleDay() == calendar.get(Calendar.DAY_OF_MONTH)
                        && se.getTimeScheduleHour() == calendar.get(Calendar.HOUR_OF_DAY)
                        && se.getTimeScheduleMinute() == calendar.get(Calendar.MINUTE)){
                    executeSchedule(se);
                    deleteSchedulesToServer(se);
                    mScheduleList.remove(se);
                }
            }break;
            case ScheduleBase.SE_TIME_LOOPER_HOUR:{
                if(se.getTimeScheduleMinute() == calendar.get(Calendar.MINUTE)){
                    executeSchedule(se);
                }
            }break;
            case ScheduleBase.SE_TIME_LOOPER_DAY:{
                if(se.getTimeScheduleHour() == calendar.get(Calendar.HOUR_OF_DAY)
                        && se.getTimeScheduleMinute() == calendar.get(Calendar.MINUTE)){
                    executeSchedule(se);
                }
            }break;
            case ScheduleBase.SE_TIME_LOOPER_WEEK:{
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if(se.getTimeScheduleWeek().contains(day)
                        && se.getTimeScheduleHour() == calendar.get(Calendar.HOUR_OF_DAY)
                        && se.getTimeScheduleMinute() == calendar.get(Calendar.MINUTE)){
                    executeSchedule(se);
                }
            }break;
            case ScheduleBase.SE_TIME_LOOPER_MONTH:{
                if( se.getTimeScheduleDay() == calendar.get(Calendar.DAY_OF_MONTH)
                        && se.getTimeScheduleHour() == calendar.get(Calendar.HOUR_OF_DAY)
                        && se.getTimeScheduleMinute() == calendar.get(Calendar.MINUTE)){
                    executeSchedule(se);

                }
            }break;
            default:break;
        }
    }

    private void checkLoopEvent(ScheduleEvent se){
        int currentOffset = se.getLoopOffset();
        if(currentOffset >= 1){
            se.setLoopOffset(currentOffset - 1);
        }else{
            executeSchedule(se);
            int loopcount = se.getLoopCount();
            if(loopcount >= 1){
                se.setLoopCount(loopcount-1);
            }
            se.setLoopOffset(se.getLoopInterval()-1);

            if(se.getLoopCount() == 0){
                deleteSchedulesToServer(se);
                mScheduleList.remove(se);
                save();
            }
        }
    }

    private void executeSchedule(ScheduleEvent se){
        Intent msgIntent = new Intent(MainActivity.MESSAFE_SCHEDULE_ACTION);
        msgIntent.putExtra(MainActivity.KEY_MESSAGE, JSON.toJSONString(se));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(msgIntent);
        Log.e("RTM", "executeSchedule: "+se.getScheduleId() );
    }

    public  void loadList() {
        try {
            final String jas = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).
                    getString(SP_KEY, "");
            mScheduleList = JSON.parseArray(jas, ScheduleEvent.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mScheduleList == null) {
            mScheduleList = new ArrayList<>();
        }
    }



    public void parseFromServer(String sData){
        synchronized (syncList){
            try {
                mScheduleList.clear();
                List<PropertySet> psList = com.alibaba.fastjson.JSONArray.parseArray(sData, PropertySet.class);
                if(psList != null){
                    for (PropertySet ps:psList) {
                        android.util.Log.e("RTM", "downloadSchedules: " + ps.getProperty() );
                        ScheduleEvent newSE = JSON.parseObject(ps.getProperty(),ScheduleEvent.class );

                        mScheduleList.add(newSE);
                        if(newSE.getEventType() == ScheduleBase.SE_EVENT_TYPE_AUDIOPLAY){
                            getAudioResource(newSE);
                        }
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("RTM", "downloadSchedules: error " + e );
                e.printStackTrace();
            }
            save();
        }
    }

    public  void insertSchedule(ScheduleEvent newSE){
        if (newSE == null ) {
            return ;
        }
        synchronized (syncList){
            for (ScheduleEvent p : mScheduleList) {
                if (TextUtils.equals(p.getScheduleId(), newSE.getScheduleId())) {
                    mScheduleList.remove(p);
                    break;
                }
            }
            mScheduleList.add(newSE);
            if(newSE.getEventType() == ScheduleBase.SE_EVENT_TYPE_AUDIOPLAY){
                getAudioResource(newSE);
            }
            save();
        }
    }

    public  void modifySchedule(ScheduleEvent newSE){
        if (newSE == null ) {
            return ;
        }
        synchronized (syncList){
            for (ScheduleEvent p : mScheduleList) {
                if (TextUtils.equals(p.getScheduleId(), newSE.getScheduleId())) {
                    p.copyFrom(newSE);
                    break;
                }
            }
            save();
        }
    }

    public  void deleteSchedule(ScheduleEvent newSE){
        if (newSE == null ) {
            return ;
        }
        synchronized (syncList){
            for (ScheduleEvent p : mScheduleList) {
                if (TextUtils.equals(p.getScheduleId(), newSE.getScheduleId())){
                    deleteSchedulesToServer(p);
                    mScheduleList.remove(p);
                    break;
                }
            }
            save();
        }
    }

    private void deleteSchedulesToServer(ScheduleEvent nSE){

        JSONObject jo = new JSONObject();
        HK.putJo(jo, "deviceid", AppCache.getIns().terminalResult.getDeviceid());
        HK.putJo(jo, "name", nSE.getScheduleId());
        HK.putJo(jo, "type", ScheduleBase.PropertySchedule);

        new HttpPost(HK.FIRECTRI_PropertyDelete, jo.toString(),  new HttpPost.ResultListener() {
            @Override
            public void onSuccess( String msg) {
                try {
                    JSONObject jo = new JSONObject(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String msg) {

            }
        });
    }

    public String getAudioSourcePath(String scheduleID){
        if(mAudioFileMap.containsKey(scheduleID)){
            return mAudioFileMap.get(scheduleID);
        }else{
            return null;
        }
    }

    private void getAudioResource(final ScheduleEvent se){
        if(se.getEventType() != ScheduleBase.SE_EVENT_TYPE_AUDIOPLAY){
            return;
        }

        new Thread(){
            @Override
            public void run() {
                try{
                    String fileType;
                    if(se.getEventData().contains(".mp3")){
                        fileType = ".mp3";
                    }else if(se.getEventData().contains(".wav")){
                        fileType = ".wav";
                    }else{
                        return;
                    }
                    //deleteFile();
                    BosClientConfiguration config = new BosClientConfiguration();
                    config.setCredentials(new DefaultBceCredentials("1df16ec2f3dd4f60955892cdc19718b3", "c495e80444b348a18e79413f5583ef92"));   //您的AK/SK
                    config.setEndpoint("http://su.bcebos.com");    //传入Bucket所在区域域名
                    BosClient client = new BosClient(config);
                    String filePath = "audiores/" + AppCache.getIns().terminalResult.getDeviceid() + "/" + se.getEventData();
                    GetObjectRequest getObjectRequest1 = new GetObjectRequest("appfile", filePath);
                    String  img_url = Environment.getExternalStorageDirectory()+ "/download";
                    File file = Environment.getExternalStoragePublicDirectory(img_url + "/"+ se.getEventData());
                    if (file.isFile() && file.exists()) {
                        file.delete();
                    }
                    ObjectMetadata audioFile = client.getObject(getObjectRequest1, new File(img_url,se.getEventData()));
                    mAudioFileMap.put(se.getScheduleId(), img_url + "/"+ se.getEventData());
                }
                catch (BceServiceException e) {
                    System.out.println("Error ErrorCode: " + e.getErrorCode());
                    System.out.println("Error RequestId: " + e.getRequestId());
                    System.out.println("Error StatusCode: " + e.getStatusCode());
                    System.out.println("Error Message: " + e.getMessage());
                    System.out.println("Error ErrorType: " + e.getErrorType());

                } catch (BceClientException e) {
                    System.out.println("Error Message: " + e.getMessage());
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public List<ScheduleEvent> getScheduleData(){
        return mScheduleList;
    }

    private  void save() {
        if(mScheduleList != null){
            mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putString(SP_KEY, JSONArray.toJSONString(mScheduleList)).apply();
        }
    }
}
