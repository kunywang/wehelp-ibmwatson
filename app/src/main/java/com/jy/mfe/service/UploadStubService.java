package com.jy.mfe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baidubce.BceClientException;
import com.baidubce.BceServiceException;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.PutObjectResponse;
import com.jy.mfe.bean.StubFile;
import com.jy.mfe.bean.TerminalInfo;
import com.jy.mfe.cache.AppCache;
import com.jy.mfe.http.ProtocolSubmitStub;
import com.jy.mfe.util.VCUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author kunpn
 */
public class UploadStubService extends Service {
    public static final String TAG = UploadStubService.class.getSimpleName();


    private static final String SP_NAME = "UploadingFiles";
    private static final String SP_KEY = "upload";
    private static UploadStubService instance ;
    private  List<StubFile> uploadFiles;
    private  Context mContext;
    private  final Object syncList = new Object();
    private boolean bFileUploading = false;
    public UploadStubService() {
    }

    public static UploadStubService getIns(){
        return instance;
    }

    public static void startUploadService(Context context){
        Intent serviceIntent = new Intent(context, UploadStubService.class);
        context.startService(serviceIntent);
    }

    public  List<StubFile> loadFiles() {
        try {
            final String jas = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).
                    getString(SP_KEY, "");
            uploadFiles = JSON.parseArray(jas, StubFile.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uploadFiles == null) {
            uploadFiles = new ArrayList<>();
        }
        return uploadFiles;

    }

    public  boolean push(StubFile info) {
        if (info == null ) {
            return false;
        }
        synchronized (syncList){
            for (StubFile p : uploadFiles) {
                if (TextUtils.equals(p.getNumber(), info.getNumber())) {
                    return false;
                }
            }
            uploadFiles.add(info);

            save();
        }
        return true;
    }

    private  StubFile getFirstStub(){
        if(uploadFiles == null) {
            return null;
        }
        synchronized (syncList){
            Iterator<StubFile> itTask = uploadFiles.iterator();
            if(itTask.hasNext()){
                StubFile firstTask = (StubFile)itTask.next();
                return firstTask;
            }
        }
        return null;
    }

    private  void removeStub(StubFile stubInfo){
        if(uploadFiles == null) {
            return ;
        }
        synchronized (syncList){
            for (StubFile p : uploadFiles) {
                if (TextUtils.equals(p.getNumber(), stubInfo.getNumber())) {
                    uploadFiles.remove(p);
                    save();
                    break;
                }
            }
        }
        return ;
    }

    public  void clear() {
        synchronized (syncList){
            mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().remove(SP_KEY).apply();
            if(uploadFiles != null) {
                uploadFiles.clear();
            }
        }

    }

    private  void save() {
        if(uploadFiles != null){
            mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit().putString(SP_KEY, JSONArray.toJSONString(uploadFiles)).apply();
        }
    }

    private boolean uploadTask(StubFile stubInfo){
        boolean submitRelt = true;
        boolean stubError = false;

        File file = new File(stubInfo.getPath());
        if (file.isFile() && file.exists()) {
            if(true == uploadTaskFileToBD(stubInfo)){
                file.delete();

                //itStub.remove();
            }else{
                stubError = true;
            }
        }

        if(stubError == true){
            submitRelt = false;
        }

        return submitRelt;
    }

    private void submitFiles(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(AppCache.getIns().getLoginState() == false){
                    mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime*5);
                    return;
                }
                final StubFile firstTask = getFirstStub();

                if(firstTask != null){
                    firstTask.setDeviceid(AppCache.getIns().terminalResult.getDeviceid());
                    bFileUploading = true;
                    if(true == uploadTask(firstTask)){
                        new ProtocolSubmitStub(mContext, firstTask, new ProtocolSubmitStub.ResultListener() {
                            @Override
                            public void onSuccess() {
                                removeStub(firstTask);
                                bFileUploading = false;
                                mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime);
                            }

                            @Override
                            public void onError(String msg) {
                                bFileUploading = false;
                                mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime);
                            }
                        });
                    }else{
                        bFileUploading = false;
                        mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime);
                    }
                }
            }
        }).start();
    }


    private boolean uploadTaskFileToBD(StubFile sf){
        try {
            TerminalInfo terminal = AppCache.getIns().terminalResult;
            BosClientConfiguration config = new BosClientConfiguration();
            config.setCredentials(new DefaultBceCredentials("1df16ec2f3dd4f60955892cdc19718b3", "c495e80444b348a18e79413f5583ef92"));
            config.setEndpoint("http://su.bcebos.com");
            String bucketName = "lvrstub";
            BosClient client = new BosClient(config);

            String sTaskKey =  terminal.getStation()+ "/"+ terminal.getDeviceid()  + "/" + VCUtil.getDate() + "/";

            File file = new File(sf.getPath());
            String fileKey = sTaskKey + file.getName();
            PutObjectResponse putObjectFromFileResponse = client.putObject(bucketName, fileKey, file);

            URL url = client.generatePresignedUrl(bucketName, fileKey, -1);
            sf.setPath(fileKey);
            sf.setUrl(url.toString());
            sf.setBucket(bucketName);
            sf.setUploadtime(VCUtil.getTime());
            // 打印ETag
            System.out.println(putObjectFromFileResponse.getETag());

            return true;

        } catch (BceServiceException e) {
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

        return false;
    }

    private final int mTimerCountDown= 100000 ;
    private final int mTimerDelayTime = 1000 *60;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int nMsg = msg.what;
            switch (nMsg) {
                case mTimerCountDown: {
                    if(bFileUploading == true){
                        mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime);
                        return;
                    }
                    StubFile taskInfos = getFirstStub();
                    if(taskInfos != null){
                        submitFiles();
                    }else{
                        mHandler.sendEmptyMessageDelayed(mTimerCountDown, mTimerDelayTime);
                    }

                }break;
                default:break;
            }

        };
    };



    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loadFiles();
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessage(mTimerCountDown);
        return super.onStartCommand(intent, flags, startId);
    }
}
