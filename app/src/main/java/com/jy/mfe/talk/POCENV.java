package com.jy.mfe.talk;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.jy.mfe.GenetekApp;
import com.jy.mfe.MainActivity;
import com.jy.mfe.R;
import com.weivoice.srv.AudioInterface;
import com.weivoice.srv.Global;
import com.weivoice.srv.entity.Channel;
import com.weivoice.srv.entity.Contact;

import java.util.ArrayList;

public class POCENV implements POCBase, Global {
    private static final int MSG_TOAST = 99987;

    public static String  ACT_CHANNEL_SPEAKING 	= "android.intent.action.talk.speaking";
    public static String  ACT_Hytera_Key 	= "android.intent.action.HyteraKey";
    private static POCENV _this = null;
    public  Context mContext;
    public static POCENV ins() {
        if(_this == null){
            _this = new POCENV();
        }
        return _this;
    }

    public String mLastChannelID = "";

    private static class ObjectInfo {
        final MsgHandler handler;
        final Object obj;
        ObjectInfo(MsgHandler handler, Object obj) {
            this.handler = handler;
            this.obj = obj;
        }
    }
    private ToneGenerator mTone = null;
    private KeyguardManager.KeyguardLock mKeyguard;
    private HandlerThread mThread;
    private Handler mHandler;
    private Handler mHandlerBG;
    private int mIdx = -1;
    private final int audioSampleRate = 16000;
    private final int audioChannel = AudioFormat.CHANNEL_OUT_MONO;
    private final int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    public AudioTrack audioTrack = null;
    public int mATSizeInBytes = 0;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            String act = i.getAction();
            Log.i(act + " - " + i.getExtras());
            switch (act) {
                case ACT_SET_UP:
                    switch (i.getIntExtra(ERR, 0)) {
                        case 0:
                            showToast("登录成功");
                            break;
                        case error_code_password_mismatch:
                            showToast("密码不匹配！");
                            break;
                        case error_code_user_not_exist:
                            showToast("账号不存在或密码不匹配！");
                            break;
                        case error_code_force_out:
                            showToast("用户被强制离线！");
                            break;
                        default:
                            showToast("登录失败");
                            break;
                    }
                    break;
                case ACT_FLOOR:
                    if (i.hasExtra(ERR)) {
                        // TODO:
                        switch (i.getIntExtra(ERR, 0)) {
                            case error_code_limited_resource:
                                break;
                            case error_code_permission_deny:
                                break;
                            case error_code_room_not_exist:
                                break;
                            case error_code_blacklist_blocked:
                                break;
                            case error_code_local_room_not_exist:
                                break;
                            case error_code_invalid_timestamp:
                                break;
                            case error_code_network_timeout:
                                break;
                        }
                    } else {
                        try {
                            handleFloor(i.getStringExtra(RID), i.getStringExtra(UID));
                        } catch (Exception e) {
                        }

                    }
                    break;
                case ACT_STATUS_UPDATE:
                    showToast(POC.getState() == STATE_ONLINE ? "上线" : "离线");
                    break;
                case ACT_CONTACTS_UPDATE:
                    break;
                case ACT_CHANNEL_UPDATE:
                    launchCall(i.getStringExtra(RID), i.getIntExtra(ERR, 0));
                    break;
                case "android.intent.action.GLOBAL_BUTTON":
                    handleKey((KeyEvent)i.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
                    break;
            }
        }
    };

    public void init(Context cn) {
        mContext = cn;
        _this = this;
        mTone = new ToneGenerator(AudioManager.STREAM_DTMF, 80);
        mKeyguard = ((KeyguardManager) GenetekApp.ins().getSystemService(Context.KEYGUARD_SERVICE)).newKeyguardLock("KeyguardLock");
        mThread = new HandlerThread("background-handler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mThread.start();
        mHandlerBG = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                dispatch(msg.what, msg.obj);
            }
        };
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                dispatch(msg.what, msg.obj);
            }
        };

        IntentFilter IF = new IntentFilter();
        IF.addAction(ACT_SET_UP);
        IF.addAction(ACT_FLOOR);
        IF.addAction(ACT_STATUS_UPDATE);
        IF.addAction(ACT_CONTACTS_UPDATE);
        IF.addAction(ACT_CHANNEL_UPDATE);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, IF);

       // IF = new IntentFilter();
       // IF.addAction("android.intent.action.GLOBAL_BUTTON");
        //registerReceiver(mReceiver, IF);
        mATSizeInBytes = AudioTrack.getMinBufferSize(audioSampleRate, audioChannel, audioFormat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(audioSampleRate)
                            .setChannelMask(audioChannel)
                            .build())
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(mATSizeInBytes)
                    .build();
        } else {
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, audioChannel, audioFormat,
                    mATSizeInBytes, AudioTrack.MODE_STREAM);
        }

        MyAudioIO  audioIO = new MyAudioIO();

        POC.init(mContext
                , null // 服务器地址，null为默认地址
                , audioIO // 可以重写AudioInterface， null默认
                , 45000 // PTT话权超时时长（ms）
                , 45000 // 单呼、临时组呼话权释放后超时时长（ms）
                , 8000); // 监听组话权释放后超时时长（ms）
        POC.setMonitorDefault(true);
        Log.setLogLevel(0);
    }

    public AudioInterface.RecordListener mRecordListen = null;
    public AudioInterface.PlayListener mPlayListener = null;
    public boolean needTalk = false;
    public Thread mPlayThread = null;
    public class MyAudioIO implements AudioInterface{
        @Override
        public int sampleRate() {
            return audioSampleRate;
        }

        @Override
        public void record(RecordListener recordListener, boolean b) {
            if(b == true){
                mRecordListen = recordListener;
                needTalk = true;
            }else{
                needTalk = false;
            }
        }

        @Override
        public void play(PlayListener playListener, boolean start) {
            mPlayListener = playListener;
            if(start == true){
               if(mPlayThread == null){
                   mPlayThread = new Thread(){
                       @Override
                       public void run(){
                           android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                           play(mPlayListener);
                       }
                   };
                   mPlayThread.start();
               }
            }else if(mPlayThread != null){
                mPlayThread = null;
            }
        }

        @Override
        public void mutePlay(boolean b) {
        }

        @Override
        public void destroy(Context context) {
        }

        private void play(AudioInterface.PlayListener listener){
            int sz = 0;
            byte[] pcm;

            listener.read(true);
            audioTrack.play();
            while (mPlayThread != null){
                pcm = listener.read(false);
                if(pcm != null && pcm.length > 0){
                    android.util.Log.d("POC", "play: " + pcm.length);
                    sz += audioTrack.write(pcm, 0, pcm.length)/2;
                }else{
                    sleep(20);
                }
            }
        }

    }



    private void dispatch(int what, Object obj) {
        if (what == MSG_TOAST) {
            Toast.makeText(mContext, (String)obj, Toast.LENGTH_SHORT).show();
        } else if (obj instanceof MsgHandler) {
            ((MsgHandler)obj).handleMessage(what, null);
        } else if (obj instanceof ObjectInfo) {
            ObjectInfo info = (ObjectInfo)obj;
            info.handler.handleMessage(what, info.obj);
        }
    }

    public void showToast(String text) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_TOAST, text));
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
        }
    }

    public boolean hasMessage(int what) {
        return mHandler.hasMessages(what);
    }

    public void post(MsgHandler handler, int what, long delay) {
        post(handler, what, null, delay);
    }

    public void post(MsgHandler handler, int what, Object obj, long delay) {
        mHandler.removeMessages(what);
        if (delay >= 0) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(what, obj == null ? handler : new ObjectInfo(handler, obj)), delay);
        }
    }

    public void postBG(MsgHandler handler, int what, long delay) {
        postBG(handler, what, null, delay);
    }

    public void postBG(MsgHandler handler, int what, Object obj, long delay) {
        mHandlerBG.removeMessages(what);
        if (delay >= 0) {
            mHandlerBG.sendMessageDelayed(mHandlerBG.obtainMessage(what, obj == null ? handler : new ObjectInfo(handler, obj)), delay);
        }
    }

    public void postDelayed(Runnable r, int delayMs) {
        mHandlerBG.removeCallbacks(r);
        if (delayMs >= 0) {
            mHandlerBG.postDelayed(r, delayMs);
        }
    }

    public boolean hasBG(int what) {
        return mHandlerBG.hasMessages(what);
    }

    public void keyguard(boolean reenable) {
        if (reenable) {
          //  mKeyguard.reenableKeyguard();
        } else {
          //  mKeyguard.disableKeyguard();
        }
    }

    public void playTone(int id, final int ms) {
        if (id > -1) {
            Log.d(id + " - " + ms);
            mTone.startTone(id, ms);
        } else {
            mTone.stopTone();
        }
    }

    private void launchCall(String cid, int err) {
        if (err != 0) {
            if (err == error_code_usr_offline) {
                showToast("用户不在线！");
            } else if (err == error_code_limited_resource) {
            } else if (err == error_code_permission_deny) {
            } else if (err == error_code_room_not_exist) {
            } else if (err == error_code_blacklist_blocked) {
            } else if (err == error_code_local_room_not_exist) {
            } else if (err == error_code_network_timeout) {
            } else if (err == error_code_invalid_timestamp) {
            } else if (err != error_none) {
            }
            return;
        } else if (cid == null) {
            if (TextUtils.isEmpty(POC.getChannelId())) {
                nextChannel(0);
            }
            return;
        } else if (!cid.equals(POC.getChannelId())) {
            return;
        }
        /*
        ActCall act = ActCall.findActivity(ActCall.class);
        Log.d(act);
        if (act == null && !cid.isEmpty()) {
            startActivity(new Intent(this, ActCall.class).putExtra(CID, cid).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } else if (cid.isEmpty() && act != null) {
            act.finish();
        }
        */
    }

    private void handleFloor(String cid, String uid) {
        if (cid == null) {
            // call failed
            playTone(42, 150);
            return;
        }
        playTone(uid != null ? 25 : 16, 150);

        if (mSpeakingChannel.getSpeaker() != null && !cid.equals(POC.getChannelId()) && mSpeakingChannel.equals(POC.getChannelId())) {
            // current channel is high prior than monitor.
            return;
        } else if (uid == null) {
            Channel ch = Channel.findSpeaking();
            if (ch != null) {
                cid = ch.getRid();
            }
        }
        setCid(cid);
        POCENV.ins().post(speakingHandler, MSG_TICK, 100);
    }

    private void handleKey(KeyEvent evt) {
        int keyCode = evt.getKeyCode();
        switch (keyCode) {
            case 261:
            case 262:
            case 265:
            case 266:
                POC.handlePTT(evt.getAction() == KeyEvent.ACTION_DOWN);
                break;
        }
    }

    public Channel getSavedChannel() {
        ArrayList<Channel> all = Channel.getChannels();
        if (all.size() == 0) {
            return null;
        }
        String cid = POC.getSavedChannelId();
        if (cid != null) {
            for (int i = all.size() - 1; i >= 0; i--) {
                if (cid.equals(all.get(i).getRid())) {
                    mIdx = i;
                    break;
                }
            }
        }

        if(mIdx == -1){
            mIdx = 0;
        }
        Channel ch = all.get(mIdx);
        return ch;
    }

    public boolean isSavedChannel(String cid) {
        String cidSaved = POC.getSavedChannelId();
        if(TextUtils.equals(cidSaved, cid)){
            return true;
        }
        return false;
    }
    public Channel updataChannel(){
        try {
            ArrayList<Channel> all = Channel.getChannels();
            if (all.size() == 0) {
                return null;
            }
            mIdx = -1;
            String cid = POC.getSavedChannelId();
            // String cid = POC.getChannelId();
            if (cid != null) {
                for (int i = all.size() - 1; i >= 0; i--) {
                    if (cid.equals(all.get(i).getRid())) {
                        mIdx = i;
                        break;
                    }
                }
            }
            if (mIdx == -1) {
                mIdx = 0;
            }

            mIdx = (mIdx  + all.size()) % all.size();
            Channel ch = all.get(mIdx);
            POC.join(ch.getRid());
            return ch;
        }catch (Exception e){
            return null;
        }
    }
    public Channel nextChannel(int inc) {
        ArrayList<Channel> all = Channel.getChannels();
        if (all.size() == 0) {
            return null;
        }
        if (mIdx == -1) {
            String cid = POC.getSavedChannelId();
            if (cid != null) {
                for (int i = all.size() - 1; i >= 0; i--) {
                    if (cid.equals(all.get(i).getRid())) {
                        mIdx = i;
                        break;
                    }
                }
            }
        }
        mIdx = (mIdx + inc + all.size()) % all.size();
        Channel ch = all.get(mIdx);
        POC.join(ch.getRid());
        return ch;
    }

    public Channel changeChannel(String channelRid) {
        try {
            android.util.Log.e("POC", "changeChannel: " + channelRid);
            ArrayList<Channel> all = Channel.getChannels();
            if (all.size() == 0) {
                return null;
            }
            int targetChannelIndex = -1;
            for (int i = all.size() - 1; i >= 0; i--) {
                if (TextUtils.equals( channelRid, all.get(i).getRid())) {
                    targetChannelIndex = i;
                    break;
                }
            }
            if (targetChannelIndex != -1) {
                mIdx = targetChannelIndex;
                mIdx = (mIdx  + all.size()) % all.size();
                Channel ch = all.get(mIdx);
                POC.join(ch.getRid());
                android.util.Log.e("POC", "changeChannel: finded" + channelRid);
                return ch;
            }else{
                return null;
            }

        }catch (Exception e){
            return null;
        }
    }


    public Channel getLastChannel(String lastCID){
        ArrayList<Channel> all = Channel.getChannels();
        if (all.size() == 0) {
            return null;
        }
        if (lastCID != null) {
            for (int i = all.size() - 1; i >= 0; i--) {
                if (lastCID.equals(all.get(i).getRid())) {
                    Channel ch = all.get(mIdx);
                    POC.join(ch.getRid());
                    return ch;
                }
            }
        }

        Channel ch_default = all.get(0);
        POC.join(ch_default.getRid());
        mLastChannelID = ch_default.getRid();
        return ch_default;
    }

    //process channel speaking information
    private static final int MSG_BASE_CALL = 20;
    protected static final int MSG_TICK = MSG_BASE_CALL;
    protected static final int MSG_HIDE = MSG_BASE_CALL + 1;
    protected static final int MSG_UPDATE = MSG_BASE_CALL + 2;
    protected static final int MSG_SPEAK = MSG_BASE_CALL + 3;
    private Channel mSpeakingChannel;
    private String lastSpeaker = "";
    private SpeakingHandler speakingHandler = new SpeakingHandler();
    public class SpeakingHandler implements POCBase.MsgHandler{
        @Override
        public void handleMessage(int what, Object obj) {
            switch (what) {
                case MSG_TICK:
                    updateSpeakingTick();
                    break;
                case MSG_UPDATE:
                    String cid = POC.getChannelId();
                    if (cid == null) {
                        return;
                    } else if (mSpeakingChannel != null) {
                        if (cid != null && mSpeakingChannel.equals(cid)) {
                            setCid(cid);
                        }
                    }
                    break;
                case MSG_SPEAK:
                    cid = (String) obj;
                    if (mSpeakingChannel == null || !mSpeakingChannel.equals(cid)) {
                        setCid(cid);
                        handleMessage(MSG_HIDE, mSpeakingChannel == null || !mSpeakingChannel.isDialog() ? null : POC.getUid());
                    }
                    String uid = mSpeakingChannel == null ? null : mSpeakingChannel.getSpeaker();
                    POCENV.ins().post(this, MSG_TICK, uid == null ? 10 : 200);
                    break;
            }
        }
    }
    private void updateSpeakingTick() {
        String talker;
        boolean spk = mSpeakingChannel != null && mSpeakingChannel.getSpeaker() != null;
        if (spk) {
            if(lastSpeaker.length() <= 0){
                talker= mSpeakingChannel.getName() + ":"+  getSpeakerName(mSpeakingChannel);
                lastSpeaker = getSpeakerName(mSpeakingChannel);

                POCENV.ins().post(speakingHandler, MSG_TICK, 1000);
            }else{
                talker= mSpeakingChannel.getName() + ":"+  getSpeakerName(mSpeakingChannel);
                lastSpeaker = getSpeakerName(mSpeakingChannel);
                POCENV.ins().post(speakingHandler, MSG_TICK, 1000);
            }

            //int sec = (int)((System.currentTimeMillis() - mSpeakingChannel.getStamp()) / 1000) % 60;
        } else {
            talker = GenetekApp.ins().getString(R.string.talk_idle);
            lastSpeaker = "";
        }
        Intent msgIntent = new Intent(ACT_CHANNEL_SPEAKING);
        msgIntent.putExtra(MainActivity.KEY_MESSAGE, talker);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(msgIntent);
    }

    private String getSpeakerName(Channel ch) {
        Contact c = ch == null ? null : Contact.find(ch.getSpeaker());
        return c == null ? "" : c.getName();
    }

    public boolean setCid(String cid) {
        try {
            if (cid == null) {
                return false;
            } else if (mSpeakingChannel != null && mSpeakingChannel.equals(cid)) {
                return true;
            }
            mSpeakingChannel = Channel.find(cid);
            POCENV.ins().post(speakingHandler, MSG_TICK, 100);
            if (mSpeakingChannel == null) {
                mSpeakingChannel = new Channel(cid, "临时组呼");
            }
        }catch (Exception e){

        }


        return true;
    }
}
