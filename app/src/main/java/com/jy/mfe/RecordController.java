package com.jy.mfe;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.jy.mfe.VideoEncoder.MediaEncoder;
import com.jy.mfe.VideoEncoder.VideoEncoder;
import com.jy.mfe.VideoEncoder.VideoEncoderObserver;
import com.jy.mfe.audio.AudioEncodeCallback;
import com.jy.mfe.audio.AudioEncoder;
import com.jy.mfe.bean.StubFile;
import com.jy.mfe.cache.AppCache;
import com.jy.mfe.service.UploadStubService;
import com.jy.mfe.talk.POCENV;
import com.jy.mfe.tts.TTSController;
import com.jy.mfe.util.VCUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
/**
 * @author kunpn
 */
public class RecordController implements VideoEncoderObserver {

    private static RecordController ins;
    private final Object sync = new Object();
    private Timer timer;
    private TimerTask timerTask;
    private boolean muxerWorking = false;
    private AudioEncoder audioEncoder = null;
    private VideoEncoder videoEncoder = null;

    private MediaMuxer muxer;
    private int audioTrack = -1;
    private int videoTrack = -1;
    private boolean fullDiskMsg = false;
    private RecordCallback rcb = null;
    private MediaFormat videoFormat = null;
    private MediaFormat audioFormat = null;
    private String recordPath;
    private String currentStubPath = "";
    private String currentStubName = "";
    public boolean autoRecord = false;
    private Context mContext = null;
    public static TTSController mTtsManager;

    private RecordController(RecordCallback callback, Context context){
        rcb = callback;
        recordPath = getAppRootOfSdCardRemovable();
        mTtsManager = MainActivity.mTtsManager;
        mContext = context;
        if(recordPath == null){
            recordPath = Environment.getExternalStorageDirectory().toString();
        }
    }

    public void startMuxer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // It runs every ten minutes.
                stopMuxer();
                if(autoRecord == false){
                    return;
                }
                if (getDiskUsage() < 10f) {
                    if (rcb != null && !fullDiskMsg) {
                        fullDiskMsg = rcb.onDiskIsFull();
                    }
                    return;
                } else {
                    if (rcb != null && fullDiskMsg) {
                        rcb.onDiskIsReady();
                    }
                    fullDiskMsg = false;
                }
                currentStubPath = generateFileName();
                newMuxer(currentStubPath);
            }
        };
        timer.schedule(timerTask, 50, 5*60000);
    }

    public void onRecordKey(){
        if(autoRecord == false){
           // mTtsManager.TaskSpeak(mContext.getString(R.string.startrecord));
            mTtsManager.playSound(R.raw.startrecord);
        }else{
            //mTtsManager.TaskSpeak(mContext.getString(R.string.stoprecord));
            mTtsManager.playSound(R.raw.stoprecord);
        }
        recHandler.sendEmptyMessage(150);
    }

    public void onRecordStart(){
        mTtsManager.playSound(R.raw.startrecord);
        recHandler.sendEmptyMessage(151);
    }

    public void onRecordStop(){
        mTtsManager.playSound(R.raw.stoprecord);
        recHandler.sendEmptyMessage(152);
    }

    private Handler recHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int nMsg = msg.what;
            switch (nMsg) {
                case 150: {
                    if(autoRecord == false){
                        resumeRecord();
                    }else{
                        pauseRecord();
                    }
                }
                break;
                case 151: {
                    resumeRecord();
                }
                break;
                case 152: {
                    pauseRecord();
                }
                break;
                default:
                    break;
            }
        }
    };

    public void pauseRecord(){
        autoRecord = false;
        GenetekApp.ins().mRecordState = false;

        cancelTimer();
        stopMuxer();
    }

    public void resumeRecord(){
        autoRecord = true;
        GenetekApp.ins().mRecordState = true;

        if(muxerWorking == false){
            cancelTimer();
            startMuxer();
        }
    }
    public boolean isVideoFormatReady(){
        if(videoFormat != null){
            return  true;
        }else{
            return false;
        }
    }
    public boolean isAudioFormatReady(){
        if(audioFormat != null){
            return  true;
        }else{
            return false;
        }
    }

    public static RecordController getIns(){
        return ins;
    }

    public static RecordController ins(RecordCallback callback, Context context) {
        if (ins == null) {
            ins = new RecordController(callback, context);
        }
        return ins;
    }


    private float getDiskUsage() {
        File datapath = GenetekApp.ins().getExternalFilesDir("");
        StatFs dataFs = new StatFs(datapath.getPath());

        BigDecimal a = new BigDecimal(dataFs.getAvailableBytes());
        BigDecimal t = new BigDecimal(dataFs.getTotalBytes());
        return a.divide(t, 4, RoundingMode.HALF_UP).floatValue() * 100f;
    }

    private String getAppRootOfSdCardRemovable()    {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))        {
            return null;        }
        /**
         *  * 这一句取的还是内置卡的目录。
         *  * /storage/emulated/0/Android/data/com.newayte.nvideo.phone/cache
         *  * 神奇的是，加上这一句，这个可移动卡就能访问了。         * 猜测是相当于执行了某种初始化动作。
         *  */
        StorageManager mStorageManager = (StorageManager) GenetekApp.ins().getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try        {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            if(length == 1){
                Object storageVolumeElement = Array.get(result, 0);
                String path = (String) getPath.invoke(storageVolumeElement);
                return path;
            }else{
                for (int i = 0; i < length; i++)            {
                    Object storageVolumeElement = Array.get(result, i);
                    String path = (String) getPath.invoke(storageVolumeElement);
                    if ((Boolean) isRemovable.invoke(storageVolumeElement))                {
                        return path;
                    }
                }
            }
        }
        catch (Exception e)        {
            e.printStackTrace();
        }
        return null;
    }

    public void onDestory() {
        autoRecord = false;
        cancelTimer();
        stopMuxer();
    }

    private String generateFileName() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        currentStubName = "REC_" + sdf.format(new Date()) +".mp4";
       String DCIM =       Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File localpath = GenetekApp.ins().getExternalFilesDir("");
        //return GenetekApp.ins().getExternalFilesDir("DCIM") + "/REC_" + sdf.format(new Date());
        return localpath + "/" +currentStubName;
    }

    private void createEncode(){
        if(audioEncoder == null){
            audioEncoder = new AudioEncoder();
            audioEncoder.initAudioEncoder(16000, AudioFormat.ENCODING_PCM_16BIT,AudioFormat.CHANNEL_IN_MONO);
            audioEncoder.setCallback(new AudioEncodeCallback() {
                @Override
                public void outMediaFormat(int trackIndex, MediaFormat mediaFormat) {
                    audioFormat = mediaFormat;
                }

                @Override
                public void outputAudioFrame(int trackIndex, byte[] outBuf, MediaCodec.BufferInfo bufferInfo) {
                    if(outBuf != null){
                        sampleAudio(ByteBuffer.wrap(outBuf), bufferInfo);
                    }
                }
            });
        }

        if(videoEncoder == null){
            videoEncoder = new MediaEncoder();
            videoEncoder.createEncoder(this, 24, 1024);
        }
    }

    private void newMuxer(String path) {
        try {
            createEncode();
            int waitTime = 0;
            while(audioFormat == null || videoFormat == null){
                waitTime +=1;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(waitTime > 30){
                    return;
                }
            }
            muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            audioTrack = muxer.addTrack(audioFormat);
            videoTrack = muxer.addTrack(videoFormat);
            muxer.start();
            //mTtsManager.TaskSpeak(mContext.getString(R.string.recordworking));
            muxerWorking = true;
            POCENV.ins().playTone(30,100);
            if (rcb != null) {
                rcb.onStateChange(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopEncoder(){
        if(audioEncoder != null){
            audioEncoder.destroyEncoder();
            audioEncoder = null;
        }

        if(videoEncoder != null){
            videoEncoder.destroyEncoder();
            videoEncoder = null;
        }
    }

    private void stopMuxer() {
        synchronized (sync) {
            if (muxer == null){
                return;
            }
            videoFormat = null;
            audioFormat = null;

            POCENV.ins().playTone(30,100);
            if (rcb != null) {
                rcb.onStateChange(false);
            }
            if (muxerWorking) {
                muxerWorking = false;
                audioTrack = -1;
                videoTrack = -1;
                try {
                    muxer.stop();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }

                StubFile newStub = new StubFile();
                newStub.setNumber(VCUtil.getPTS());
                newStub.setDeviceid(AppCache.getIns().terminalResult.getDeviceid());
                newStub.setStation(AppCache.getIns().terminalResult.getStation());
                newStub.setUser(AppCache.getIns().terminalResult.getName());
                newStub.setCreatetime(VCUtil.getTime());
                newStub.setAddress(AppCache.getIns().terminalHB.getAddress());
                newStub.setLongitude(AppCache.getIns().terminalHB.getLongitude());
                newStub.setLatitude(AppCache.getIns().terminalHB.getLatitude());
                newStub.setPath(currentStubPath);
                newStub.setFiletype("mp4");
                newStub.setFilename(currentStubName);
                UploadStubService.getIns().push(newStub);
            }
            muxer.release();
            muxer = null;
            stopEncoder();
        }
    }

    public void encodeAudioData(byte[] dataPCM){
        if(audioEncoder != null){
            audioEncoder.encodeAudioData(dataPCM);
        }
    }

    public void encodeVideoData(byte[] dataYUV){
        if(videoEncoder != null){
            videoEncoder.encode(dataYUV);
        }
    }

    public void sampleVideo(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        synchronized (sync) {
            if (muxerWorking == true) {
                muxer.writeSampleData(videoTrack, buffer, info);
            }
        }
    }

    public void sampleAudio(ByteBuffer buffer, MediaCodec.BufferInfo info) {
        synchronized (sync) {
            if (muxerWorking == true) {
                muxer.writeSampleData(audioTrack, buffer, info);
            }
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timerTask.cancel();
            timer.cancel();
        }
    }

    @Override
    public void outMediaFormat(MediaFormat mediaFormat) {
        videoFormat = mediaFormat;
    }

    @Override
    public int onEncodeData(byte[] data, MediaCodec.BufferInfo bufferInfo) {
        for(int i = 0; i < data.length - 4; i++)
        {
            if(data[i] == 0 && data[i + 1] == 0 && data[i + 2] == 0 && data[i + 3] == 1) {
                //android.util.Log.d("p2p", "onEncodeData: "+ data.length + "," + (data[4] & 0x1F));
            }
        }

        sampleVideo(ByteBuffer.wrap(data), bufferInfo);
        return 0;
    }

    public interface RecordCallback {
        boolean onDiskIsFull();
        void onDiskIsReady();
        void onStateChange(boolean bRecording);
    }
}
