package com.jy.mfe;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.media.MediaFormat;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.baidubce.BceClientException;
import com.baidubce.BceServiceException;
import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.GetObjectRequest;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.ibm.cloud.sdk.core.security.Authenticator;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneHelper;
import com.ibm.watson.developer_cloud.android.library.audio.MicrophoneInputStream;
import com.ibm.watson.developer_cloud.android.library.audio.utils.ContentType;
import com.ibm.watson.speech_to_text.v1.SpeechToText;
import com.ibm.watson.speech_to_text.v1.model.RecognizeOptions;
import com.ibm.watson.speech_to_text.v1.model.SpeechRecognitionResults;
import com.ibm.watson.speech_to_text.v1.websocket.BaseRecognizeCallback;
import com.ibm.watson.speech_to_text.v1.websocket.RecognizeCallback;
import com.jy.mfe.VideoCapture.Camera1Source;
import com.jy.mfe.VideoCapture.CameraObserver;
import com.jy.mfe.VideoCapture.CameraSource;
import com.jy.mfe.bean.NotifyInfo;
import com.jy.mfe.bean.QuestInfo;
import com.jy.mfe.bean.ScheduleBase;
import com.jy.mfe.bean.ScheduleEvent;
import com.jy.mfe.bean.SensorInfo;
import com.jy.mfe.bean.StubFile;
import com.jy.mfe.bean.TTSNotify;
import com.jy.mfe.bean.TerminalAlarm;
import com.jy.mfe.bean.TerminalInfo;
import com.jy.mfe.cache.AppCache;
import com.jy.mfe.cache.NotifyWarehouse;
import com.jy.mfe.cache.QuestWarehouse;
import com.jy.mfe.fragment.FragmentPermissionNotice;
import com.jy.mfe.http.HK;
import com.jy.mfe.http.HttpPost;
import com.jy.mfe.http.ProtocolGetTerminalInfo;
import com.jy.mfe.rtm.ChatManager;
import com.jy.mfe.rtm.RtmNotifyBean;
import com.jy.mfe.service.LocationService;
import com.jy.mfe.service.ScheduleService;
import com.jy.mfe.service.SensorService;
import com.jy.mfe.service.UploadStubService;
import com.jy.mfe.socket.SmartDevProtcalUDP;
import com.jy.mfe.talk.POCENV;
import com.jy.mfe.tts.TTSController;
import com.jy.mfe.util.CommonUtil;
import com.jy.mfe.util.NetworkUtils;
import com.jy.mfe.util.VCUtil;
import com.weivoice.srv.Global;
import com.weivoice.srv.entity.Channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoEncoderConfiguration;
import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmStatusCode;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements Global, SurfaceHolder.Callback, CameraObserver {
    public static final int GRANT_PERMISSION_REQUEST_CODE = 7;
    public static final int MSG_AUDIO_DATA = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler handler = new Handler();
    public static TTSController mTtsManager;
    CameraSource m_cameraSource = new Camera1Source();

    RecordController cRecord;

   // private AudioIOManager aiom;

    private SpeechToText speechService;
    private MicrophoneHelper microphoneHelper;
    private MicrophoneInputStream capture;
    private boolean listening = false;
    private String[] keywordHelp;

    private byte[] aBuffer;
    private byte[] audioAgoraBuffer;
    private byte[] audioEncodeBuffer;
    private SurfaceView surfaceView = null;
    private byte[] m_fft = null;
    private long m_hFont = 0;

    MediaFormat videoFormat = null;
    MediaFormat audioFormat = null;

    private int snapshotEvent = 0;
    private String videoLiveRtmID = null;

    private MapView mapView;
    private AMap aMap;

    private final int mTicker = 100000;
    private final int mTickDuration = 1000 * 60;
    private final int mRelogin = 100001;
    private final int mSensorTicher = 100002;
    private final int SVR_EVENT_TTSNOTIFY = 100003;

    private final int mRecordStateChangeEvent = 100004;
    private final int mStartRecordEvent = 100005;
    private final int mStopRecordEvent = 100006;

    private final int mGetServerInfoEvent = 100007;
    private final int mGetServerInfoSecceedEvent = 100008;
    private final int mWatsonResult = 100009;

    private boolean autoRecord = false;

    private boolean deviceServerConfiged = false;
    private boolean loginState = false;
    private Context mContext;
    private ChatManager mChatManager;
    private RtmClient mRtmClient;
    private RtmClientListener mRtmListener;
    private RtcEngine mRtcEngine;
    private boolean isJoinedChannel = false;
    private int rmtChannelMember = 0;
    private int remoteWatcher = 0;
    public TTSNotify ttsNotify = null;
    private boolean isRemoteWatching = false;
    TextView txCurrentChannel = null;
    TextView txCurrentSpeakingUser = null;

    private SensorInfo sensorData = new SensorInfo();
    protected ImageView imgWater = null;
    protected ImageView imgTemperature = null;
    protected ImageView imgPower = null;
    protected ImageView imgChemical = null;
    protected TextView txWater = null;
    protected TextView txTemperature = null;
    protected TextView txPower = null;
    protected TextView txChemical = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_fullscreen);
        mContext = this;
        surfaceView = findViewById(R.id.main_videoview);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setFixedSize(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);
        //surfaceView.setOnTouchListener(this);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifiManager.createMulticastLock("multicast.test");
        multicastLock.acquire();
        registerMessageReceiver();

        mapView = (MapView) findViewById(R.id.main_gdmap);
        mapView.onCreate(savedInstanceState);
        InitMap();

        txWater = findViewById(R.id.sensor_water_value);
        txTemperature = findViewById(R.id.sensor_temperature_value);
        txPower = findViewById(R.id.sensor_power_value);
        txChemical = findViewById(R.id.sensor_chemical_value);
        imgWater = findViewById(R.id.img_water);
        imgTemperature = findViewById(R.id.img_temperature);
        imgPower = findViewById(R.id.img_power);
        imgChemical = findViewById(R.id.img_chemical);

        txCurrentChannel = findViewById(R.id.main_channel_name);
        txCurrentSpeakingUser = findViewById(R.id.main_speaking_name);
        txCurrentChannel.setText(getString(R.string.offline));

        AppCache.getIns().terminalHB.setStatus(getString(R.string.stoprecord));


        //AppCache.getIns().terminalResult.setDeviceid("50100001");
        mTtsManager = TTSController.getInstance(getApplicationContext());
        mTtsManager.init();

        mChatManager = GenetekApp.ins().getChatManager();
        mRtmClient = mChatManager.getRtmClient();
        mRtmListener = new EmcRtmClientListener();
        mChatManager.registerListener(mRtmListener);

        UploadStubService.startUploadService(getApplication());
        getDeviceManagerInfo();
        loginTalkRoom();
        // loginTerminal();
        initMainButton();

        mainHandler.sendEmptyMessage(mTicker);
        //mainHandler.sendEmptyMessage(mSensorTicher);

        initializeAgoraEngine();

        //   m_fft = getFromAssets("GB2312_48.ttf");
        //   if(m_fft != null)
        //   {
        //   m_hFont = p2pcamera.FontCreateByMemory(m_fft, m_fft.length,64, 128, 32);
        //  }

    }

    public byte[] getFromAssets(String fileName) {
        byte[] buffer = null;
        try {
            InputStream in = getResources().getAssets().open(fileName);
            int lenght = in.available();
            buffer = new byte[lenght];
            in.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public void setVehicelNumber() {
        TextView txNumber = findViewById(R.id.main_mfe_name);
        txNumber.setText(AppCache.getIns().terminalResult.getName());
        TextView txStation = findViewById(R.id.main_mfe_station);
        txStation.setText(AppCache.getIns().terminalResult.getStation());
    }

    public void InitMap() {
        if (aMap == null) {
            aMap = mapView.getMap();

            UiSettings uiSettings = aMap.getUiSettings();
            uiSettings.setZoomControlsEnabled(false);
            uiSettings.setMyLocationButtonEnabled(true);
            uiSettings.setLogoBottomMargin(-50);
            aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            aMap.showIndoorMap(true);
            aMap.setMyLocationEnabled(true);
            aMap.setTrafficEnabled(true);
            aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
                @Override
                public void onMapLoaded() {
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                }
            });

        }
    }

    private void initMainButton() {
        ImageView iv_nextchannel = findViewById(R.id.main_channel_next);
        iv_nextchannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (POC.getState() == Global.STATE_ONLINE) {
                    Channel ch = POCENV.ins().nextChannel(1);
                    if (txCurrentChannel != null && ch != null) {
                        txCurrentChannel.setText(ch.getName());
                    }
                    POC.join(ch.getRid());
                    POCENV.ins().setCid(ch.getRid());
                }

            }
        });

        ImageView iv_prechannel = findViewById(R.id.main_channel_previous);
        iv_prechannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (POC.getState() == Global.STATE_ONLINE) {
                    Channel ch = POCENV.ins().nextChannel(-1);
                    if (txCurrentChannel != null && ch != null) {
                        txCurrentChannel.setText(ch.getName());
                    }
                    POC.join(ch.getRid());
                    POCENV.ins().setCid(ch.getRid());
                }

            }
        });
        ImageView iv_auto = findViewById(R.id.main_gdauto);
        iv_auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    String pkgName = "com.autonavi.amapauto";
                    Intent launchIntent = new Intent();
                    launchIntent.setComponent(new ComponentName(pkgName, "com.autonavi.auto.remote.fill.UsbFillActivity"));
                    startActivity(launchIntent);
                } catch (Exception e) {
                }
            }
        });
        ImageView iv_history = findViewById(R.id.main_history);
        iv_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(mContext, HistoryActivity.class);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                }

            }
        });
        ImageView iv_setting = findViewById(R.id.main_setting);
        iv_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(mContext, SettingActivity.class);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                }

            }
        });
        ImageView btnSpeaking = findViewById(R.id.main_talk);
        btnSpeaking.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        if (POC.getState() == Global.STATE_ONLINE) {
                            POC.handlePTT(true);
                        }

                    } catch (Exception e) {
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    try {
                        POC.handlePTT(false);
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        });
    }

    private void initMM() {

        cRecord = RecordController.ins(new RecordController.RecordCallback() {
            @Override
            public boolean onDiskIsFull() {
                //addPopFragment(new FragmentNoDiskSpaceNotice(), "no disk space");
                POCENV.ins().playTone(31, 200);
                return true;
            }

            @Override
            public void onDiskIsReady() {
                // removePopFragment("no disk space");
            }

            @Override
            public void onStateChange(boolean bRecording) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setVideoRecordingFlag();
                        // mainHandler.sendEmptyMessage(mRecordStateChangeEvent);
                    }
                });
            }
        }, mContext);


    }

    private SpeechToText initSpeechToTextService() {
        Authenticator authenticator = new IamAuthenticator(getString(R.string.speech_text_apikey));
        SpeechToText service = new SpeechToText(authenticator);
        service.setServiceUrl(getString(R.string.speech_text_url));
        return service;
    }

    //zh-CN_NarrowbandModel en-GB_NarrowbandModel
    private RecognizeOptions getRecognizeOptions(InputStream captureStream) {
        return new RecognizeOptions.Builder()
                .audio(captureStream)
                .contentType(ContentType.RAW.toString())
                .model("zh-CN_NarrowbandModel")
                .interimResults(true)
                .inactivityTimeout(2000)
                .build();
    }

    private class MicrophoneRecognizeDelegate extends BaseRecognizeCallback implements RecognizeCallback {
        @Override
        public void onTranscription(SpeechRecognitionResults speechResults) {
            System.out.println(speechResults);
           // android.util.Log.e("Watson", "onTranscription: " +  speechResults);
            if (speechResults.getResults() != null && !speechResults.getResults().isEmpty()) {
                String text = speechResults.getResults().get(0).getAlternatives().get(0).getTranscript();
                //showMicText(text);
                android.util.Log.e("Watson", "getResults: " +  speechResults);
                Message msg = new Message();
                msg.what = mWatsonResult;
                msg.obj = speechResults.getResults().toString();
                mainHandler.sendMessage(msg);
            }
        }

        @Override
        public void onError(Exception e) {
            try {
                // This is critical to avoid hangs
                // (see https://github.com/watson-developer-cloud/android-sdk/issues/59)
                capture.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            listening = false;
        }

        @Override
        public void onDisconnected() {
           // enableMicButton();
            listening = false;
        }
    }

    private void startWatson(){
        if (!listening) {
            listening = true;
            capture = microphoneHelper.getInputStream(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        speechService.recognizeUsingWebSocket(getRecognizeOptions(capture),
                                new MicrophoneRecognizeDelegate());
                    } catch (Exception e) {

                    }
                }
            }).start();


        } else {
            // Update the icon background

            microphoneHelper.closeInputStream();
            listening = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check permission.
        if (!PermissionCenter.check(this)) {
            // Add work fragment.
            addHPPopFragment(new FragmentPermissionNotice(), "permission");
            return;
        }

        initMM();
        //showNewNotify();
        //showNewQuest();

        if(speechService == null ){
            microphoneHelper = new MicrophoneHelper(this);
            speechService = initSpeechToTextService();
            keywordHelp= GenetekApp.ins().getResources().getStringArray(R.array.helpkeyword);
        }
        /*
        if (aiom == null) {
            aiom = new AudioIOManager(16000,
                    AudioFormat.CHANNEL_IN_MONO,
                    (buffer, size, tick) -> {
                        if (isRemoteWatching == true) {
                            System.arraycopy(buffer, 0, audioAgoraBuffer, 0, size);
                            pushAudio(audioAgoraBuffer, tick);
                        }
                        if (cRecord.autoRecord == true) {
                            System.arraycopy(buffer, 0, audioEncodeBuffer, 0, size);
                            if (cRecord != null) {
                                cRecord.encodeAudioData(audioEncodeBuffer);
                            }
                        }
                        if (talkLoginSecceed == true) {
                            if (POCENV.ins().needTalk == true && POCENV.ins().mRecordListen != null) {
                                for (int sendpoc = 0; sendpoc < 4; sendpoc++) {
                                    System.arraycopy(buffer, 0 + sendpoc * size / 4, aBuffer, 0, size / 4);
                                    POCENV.ins().mRecordListen.onRecord(aBuffer, aBuffer.length);
                                }
                            }
                        }
                    },
                    (buffer, size, tick) -> {
                        // TODO: copy data to send buffer.
                    });
            aBuffer = aiom.createPOCBuffer();
            audioAgoraBuffer = aiom.createExternalBuffer();
            audioEncodeBuffer = aiom.createExternalBuffer();
            aiom.start();
        }

         */

    }

    private void addHPPopFragment(Fragment fragment, String tag) {
        final FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().setCustomAnimations(R.anim.pop_in, 0).
                add(R.id.main_gdmap, fragment, tag).commit();
    }

    @Override
    protected void onDestroy() {
        if (cRecord != null) {
            cRecord.onDestory();
        }
        //if (aiom != null) {
        //    aiom.stop();
        //}

        leaveChannel();
        RtcEngine.destroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSpeakingReceiver);

        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        // finish();
        return;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        m_cameraSource.createCamera(this, surfaceView.getHolder());
        m_cameraSource.startCamera(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        m_cameraSource.stopCamera();
        m_cameraSource.destroyCamera();
    }

    private MessageReceiver mMessageReceiver;
    private static final String KEY_ACC = "k_acc";
    private static final String KEY_PWD = "k_pwd";
    public boolean talkLoginSecceed = false;

    public void loginTalkRoom() {
        // String acc = "912341381";
        //  String pwd = "381888";
        String simICCID = "898600123456" + AppCache.getIns().terminalResult.getDeviceid();
        if (POC.login(simICCID)) {
            //  POC.setPref(KEY_ACC, acc);
            //  POC.setPref(KEY_PWD, pwd);
            //  POC.set

        } else {
            talkLoginSecceed = false;
        }
    }

    public static final String MESSAFE_RECEIVED_ACTION = "JPUSH.MESSAGE_RECEIVED_ACTION";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TITLE = "title";
    public static final String MSG_TYPE_NOTIFY = "notify";
    public static final String MSG_TYPE_TASK = "task";
    public static final String MSG_TYPE_IOT = "iot";
    public static final String MSG_TYPE_ALART = "alart";
    public static final String MSG_TYPE_SMOKE = "smoke";
    public static final String MSG_TYPE_QUEST = "quest";
    public static final String MSG_TYPE_APPUPDATE = "AppUpdate";
    public static final String MSG_TYPE_UPDATELIST = "UpdateList";
    public static final String MSG_TYPE_ROLLCALL = "RollCall";
    public static final String MSG_TYPE_VIDEOCALL = "VideoCall";
    public static final String MSG_TYPE_VIDEOCLOSE = "VideoClose";
    public static final String TALK_RECEIVED_ACTION = "talkAction";
    public static final String TALK_ChannelUpdate = "channelUpdate";
    public static final String MESSAFE_SCHEDULE_ACTION = "jy.schedule.event";
    public void registerMessageReceiver() {
        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);

        filter.addAction(Global.ACT_STATUS_UPDATE);
        filter.addAction(Global.ACT_CONTACTS_UPDATE);
        filter.addAction(Global.ACT_CHANNEL_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        IntentFilter ifSpeaking = new IntentFilter();
        ifSpeaking.addAction(POCENV.ACT_CHANNEL_SPEAKING);
        LocalBroadcastManager.getInstance(this).registerReceiver(mSpeakingReceiver, ifSpeaking);

        IntentFilter headsetFilter = new IntentFilter();
        headsetFilter.addAction("android.intent.action.HEADSET_PLUG");
        headsetFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(new HeadsetDetectReceiver(), headsetFilter);

        IntentFilter netstateFilter = new IntentFilter();
        netstateFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new NetworkChangedReceiver(), netstateFilter);

        LocalBroadcastManager.getInstance(this).registerReceiver(update_receiver, new IntentFilter(MESSAFE_SCHEDULE_ACTION));
    }

    public class HeadsetDetectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase("android.intent.action.HEADSET_PLUG")) {
                if (intent.hasExtra("state")) {
                    if (intent.getIntExtra("state", 0) == 0) {
                        android.util.Log.d("HEAD", "onReceive: " + "headset out");
                    } else if (intent.getIntExtra("state", 0) == 1) {
                        android.util.Log.d("HEAD", "onReceive: " + "headset in");
                    }
                }
            } else if (Intent.ACTION_BATTERY_CHANGED.equalsIgnoreCase(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int powerLever = ((level * 100) / scale);
                AppCache.getIns().terminalHB.setPower((double) powerLever);
            }

        }
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (Global.ACT_STATUS_UPDATE.equals(intent.getAction())) {
                    if (POC.getState() == Global.STATE_ONLINE) {
                        talkLoginSecceed = true;
                        Channel ch = POCENV.ins().getSavedChannel();
                        if (txCurrentChannel != null && ch != null) {
                            txCurrentChannel.setText(ch.getName());
                            POC.join(ch.getRid());
                            POCENV.ins().setCid(ch.getRid());
                        }
                    } else {
                        talkLoginSecceed = false;
                        if (txCurrentChannel != null) {
                            txCurrentChannel.setText(getString(R.string.offline));
                        }
                    }
                } else if (Global.ACT_CHANNEL_UPDATE.equals(intent.getAction())) {
                    if (intent.hasExtra(RID)) {
                        onTalkChannelUpdate(intent.getStringExtra(RID), intent.getIntExtra(ERR, 0));
                    } else {
                        android.util.Log.e("TALK", "onTalkChannelUpdate");
                       // mTtsManager.TaskSpeak(getString(R.string.tts_talkchannel_updata));
                        setChannelText();
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];

        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }

        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    public boolean pushVideo(byte[] data, int width, int height) {

        // byte[] sendVideo = rotateYUV420Degree90(data,  width,  height);
        if (isRemoteWatching == false) {
            return false;
        }
        AgoraVideoFrame vf = new AgoraVideoFrame();
        vf.format = AgoraVideoFrame.FORMAT_NV21;
        vf.timeStamp = System.currentTimeMillis();
        vf.stride = width;
        vf.height = height;
        vf.syncMode = false;
        vf.buf = data;
        boolean result = mRtcEngine.pushExternalVideoFrame(vf);

        return result;
    }

    public int pushAudio(byte[] data, long timestamp) {
        if (isRemoteWatching == false) {
            return 0;
        }
        int result = mRtcEngine.pushExternalAudioFrame(data, timestamp);
        return result;
    }

    @Override
    public int onData(byte[] data, int width, int height) {
        /*
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String strText = AppCache.getIns().terminalResult.getDeviceid()+ "  " + formatter.format(curDate);
        p2pcamera.FontLoadImage(m_hFont, strText, 0.0f, 16, 32);
        p2pcamera.FontDrawImage(m_hFont, data, width, height, 1, 99, 2);
        */

        if (snapshotEvent == 1) {

            saveYUVtoPicture(data, width, height);
        }

        pushVideo(data, width, height);

        if (cRecord.autoRecord == true) {
            cRecord.encodeVideoData(data);
        }


        return 0;
    }

    private Camera.PictureCallback pc = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // dataΪ��������
            String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
            String imgName = "IMG_" + sdf.format(new Date()) + ".jpg";
            String imgPath = DCIM + "/" + imgName;
            FileOutputStream outStream = null;
            // ʹ�������ж�д
            try {
                FileOutputStream fos = new FileOutputStream(imgPath);
                try {
                    fos.write(data);
                    fos.close();

                    StubFile newStub = new StubFile();
                    newStub.setNumber(VCUtil.getPTS());
                    newStub.setDeviceid(AppCache.getIns().terminalResult.getDeviceid());
                    newStub.setStation(AppCache.getIns().terminalResult.getStation());
                    newStub.setUser(AppCache.getIns().terminalResult.getName());
                    newStub.setCreatetime(VCUtil.getTime());
                    newStub.setAddress(AppCache.getIns().terminalHB.getAddress());
                    newStub.setLongitude(AppCache.getIns().terminalHB.getLongitude());
                    newStub.setLatitude(AppCache.getIns().terminalHB.getLatitude());
                    newStub.setPath(imgPath);
                    newStub.setFiletype("jpg");
                    newStub.setFilename(imgName);
                    UploadStubService.getIns().push(newStub);


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            m_cameraSource.resumePreview();
            snapshotEvent = 0;
        }
    };

    public void saveYUVtoPicture(byte[] data, int width, int height) {

        new Thread() {
            @Override
            public void run() {
                try {
                    if (m_cameraSource != null) {
                        android.util.Log.e("snapshot", "saveYUVtoPicture: ");
                        m_cameraSource.snapshot(pc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


/*

        String DCIM =       Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
        String imgName = "IMG_" + sdf.format(new Date())+".jpg";
        String imgPath = DCIM + "/" + imgName;
        FileOutputStream outStream = null;

        try {
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0,width, height), 100, baos);
            Bitmap bmp = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length);
            outStream = new FileOutputStream(imgPath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.write(baos.toByteArray());
            outStream.close();

            StubFile newStub = new StubFile();
            newStub.setNumber(VCUtil.getPTS());
            newStub.setDeviceid(AppCache.getIns().terminalResult.getDeviceid());
            newStub.setStation(AppCache.getIns().terminalResult.getStation());
            newStub.setUser(AppCache.getIns().terminalResult.getName());
            newStub.setCreatetime(VCUtil.getTime());
            newStub.setAddress(AppCache.getIns().terminalHB.getAddress());
            newStub.setLongitude(AppCache.getIns().terminalHB.getLongitude());
            newStub.setLatitude(AppCache.getIns().terminalHB.getLatitude());
            newStub.setPath(imgPath);
            newStub.setFiletype("jpg");
            newStub.setFilename(imgName);
            UploadStubService.getIns().push(newStub);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

 */

    }


    private void checkVideoNeedToClose() {
        if (remoteWatcher > 1) {
            remoteWatcher -= 1;
        }

        if (remoteWatcher == 1) {
            stopSendVideo(true);
            remoteWatcher = 0;
        }
    }

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            int nMsg = msg.what;
            switch (nMsg) {
                case mTicker: {
                    if (talkLoginSecceed == false) {
                        loginTalkRoom();
                    }
                    if (mChatManager.mIsInRtmChat == false) {
                        loginRtmChat();
                        //mChatManager.loginRtmChat(AppCache.getIns().terminalResult.getDeviceid());
                    }
                    mainHandler.sendEmptyMessageDelayed(mTicker, mTickDuration);
                    checkVideoNeedToClose();
                    if (isRemoteWatching == true) {
                        mChatManager.sendChannelMessage(videoLiveRtmID, JSONObject.toJSONString(AppCache.getIns().terminalHB));
                    }
                    if(listening == false){
                        startWatson();
                    }
                }
                break;
                case mRelogin: {
                    if (loginState == false) {
                        loginTerminal();
                    }
                }
                break;
                case mGetServerInfoEvent: {
                    getDeviceManagerInfo();
                }
                break;
                case mGetServerInfoSecceedEvent: {
                    loginTerminal();
                    doDNS();
                }
                break;
                case mSensorTicher: {
                    updateSensor();
                    mainHandler.sendEmptyMessageDelayed(mSensorTicher, 500);
                }
                break;
                case SVR_EVENT_TTSNOTIFY: {
                    if (ttsNotify == null) {
                        return;
                    }
                    mTtsManager.TaskSpeak(ttsNotify.getText());
                    int loop = ttsNotify.getLoop();
                    if (loop > 0) {
                        ttsNotify.setLoop(loop - 1);
                    }
                    if (ttsNotify.getLoop() > 0) {
                        mainHandler.sendEmptyMessageDelayed(SVR_EVENT_TTSNOTIFY, ttsNotify.getSleep() * 60000);
                    } else {
                        ttsNotify = null;
                    }
                }
                break;
                case mRecordStateChangeEvent: {
                    setVideoRecordingFlag();
                }
                break;
                case mWatsonResult:{
                    String sResult = (String)msg.obj;
                    pickWatsonKeyword(sResult);
                }break;
                default:
                    break;
            }
        }
    };

    private void getDeviceManagerInfo() {
        deviceServerConfiged = true;
        //HK.SetServerIPAddress("www.emc-station.com");
        HK.SetServerIPAddress("www.genetek.cc");
        mainHandler.sendEmptyMessage(mGetServerInfoSecceedEvent);

       // TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        String mICCIDString = "non";//manager.getSimSerialNumber();

        String imei = "null" ;//Build.getSerial();
        AppCache.getIns().terminalHB.setInfo(mICCIDString + "," + imei);
        /*
        String userName = AppCache.getIns().terminalResult.getDeviceid();
        String getDeviceInfoRoot = "https://www.genetek.cc:8050/mdev/device/getdeviceinfo";
        org.json.JSONObject jo = new org.json.JSONObject();
        HK.putJo(jo, "deviceid", userName);
        new HttpPost(getDeviceInfoRoot, jo.toString(),  new HttpPost.ResultListener() {
            @Override
            public void onSuccess( String msg) {
                try {
                    org.json.JSONObject jo = new org.json.JSONObject(msg);

                    int nCode = jo.optInt("code");
                    if(nCode == 0){
                        String sData = jo.optString("data");
                        DeviceServerInfo dsi = JSON.parseObject(sData, DeviceServerInfo.class);
                        if(dsi != null){
                            deviceServerConfiged = true;
                            HK.SetServerIPAddress(dsi.getServer());
                            mainHandler.sendEmptyMessage(mGetServerInfoSecceedEvent);
                        }
                    }
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                    deviceServerConfiged = false;
                    mainHandler.sendEmptyMessageDelayed(mGetServerInfoEvent,3000);
                }
            }
            @Override
            public void onError(String msg) {
                deviceServerConfiged = false;
                mainHandler.sendEmptyMessageDelayed(mGetServerInfoEvent,3000);
            }
        });

         */
    }

    private void doDNS(){
        new Thread(){
            @Override
            public void run() {
                try{
                    InetAddress udp_svr = InetAddress.getByName("118.190.86.237");//HK.mServerName
                    HK.mServerIPAddress = udp_svr;
                    WifiManager manager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                    SmartDevProtcalUDP.getInstance().init_udp(manager);
                    SmartDevProtcalUDP.getInstance().init(mContext);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void loginTerminal(){
        String userName = AppCache.getIns().terminalResult.getDeviceid();

        new ProtocolGetTerminalInfo(mContext, userName, new ProtocolGetTerminalInfo.ResultListener() {
            @Override
            public void onSuccess(TerminalInfo ti) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(ti != null){
                            loginState = true;
                            AppCache.getIns().setLoginState(true);
                           // UploadStubService.getIns().setLoginState(loginState);
                            AppCache.getIns().terminalResult = ti;

                            AppCache.getIns().terminalHB.setDeviceid(ti.getDeviceid());
                            AppCache.getIns().terminalHB.setStation(ti.getStation());
                            AppCache.getIns().terminalHB.setLatitude(ti.getSetuplatitude());
                            AppCache.getIns().terminalHB.setLongitude(ti.getSetuplongitude());
                            LocationService.startLocationService(getApplication());
                            //SensorService.startSensorService(getApplication());
                            ScheduleService.startService(getApplication());
                            setVehicelNumber();
                            downloadSchedules();
                        }else{
                            loginState = false;
                        }

                    }
                });

            }

            @Override
            public void onError(String msg) {
                loginState = false;
                mainHandler.sendEmptyMessageDelayed(mRelogin,5000);
            }
        });
    }

    private void loginRtmChat() {
        mChatManager.mIsInRtmChat = true;
        videoLiveRtmID = "vlive"+ AppCache.getIns().terminalResult.getDeviceid();
        mRtmClient.login(null, AppCache.getIns().terminalResult.getDeviceid() , new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                mChatManager.createAndJoinChannel(videoLiveRtmID, new EmcChannelListener() );
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChatManager.mIsInRtmChat = false;
                    }
                });
            }
        });
    }

    class EmcRtmClientListener implements RtmClientListener {
        @Override
        public void onConnectionStateChanged(final int state, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (state) {
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING:
                            break;
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED:
                            break;
                    }
                }
            });
        }
        @Override
        public void onMessageReceived(final RtmMessage message, final String peerId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                       // String content = message.getText();
                        String msg = message.getText();
                        RtmNotifyBean rtmNotify = JSON.parseObject(msg,RtmNotifyBean.class);
                        if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_QueryInfo, rtmNotify.getTitle())){
                            ackRtmQueryTerminal(peerId);
                        }else{
                            processRtmPeerMsg(rtmNotify);
                        }
                        //processRtmPeerMsg(rtmNotify);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @Override
        public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) {

        }

        @Override
        public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

        }

        @Override
        public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

        }

        @Override
        public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

        }

        @Override
        public void onTokenExpired() {
        }

        @Override
        public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

        }
    }

    private void ackRtmQueryTerminal(final String peerId){
        if(mChatManager.mIsInRtmChat == false){
            return;
        }

        RtmNotifyBean rtmLoc = new RtmNotifyBean();
        rtmLoc.setTitle(RtmNotifyBean.RTM_TITLE_QueryInfo);
        rtmLoc.setSender(AppCache.getIns().terminalResult.getDeviceid());
        rtmLoc.setDepartment(AppCache.getIns().terminalResult.getStation());
        rtmLoc.setData(com.alibaba.fastjson.JSONObject.toJSONString(AppCache.getIns().terminalResult));
        rtmLoc.setVideo(com.alibaba.fastjson.JSONObject.toJSONString(AppCache.getIns().terminalHB));
        String content = com.alibaba.fastjson.JSONObject.toJSONString(rtmLoc);
        mChatManager.sendPeerMessage(content,peerId );
    }

    private void processRtmPeerMsg(RtmNotifyBean rtmNotify){
        if(rtmNotify == null){
            return;
        }

        if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_VIDEOCALL, rtmNotify.getTitle())){
            //startAgoraVideocall(rtmNotify.getVideo());
            String sCaller = rtmNotify.getSender();
            String sRoom = rtmNotify.getVideo();
            //joinChannel();
            remoteWatcher = 5;
            stopSendVideo(false);
        }else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_VIDEOCLOSE, rtmNotify.getTitle())){
            //sendVideoClose();
            //leaveChannel();
            if(rmtChannelMember <= 1){
                remoteWatcher = 0;
                stopSendVideo(true);
            }
        }else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_TTS, rtmNotify.getTitle())){
           // processTTSNotify(rtmNotify);
            //TTSNotify  ttsNotify = JSONObject.parseObject(rtmNotify.getData(), TTSNotify.class);
            //mTtsManager.TaskSpeak(ttsNotify.getText());

            ttsNotify = JSONObject.parseObject(rtmNotify.getData(), TTSNotify.class);
            if(ttsNotify != null){
                if(mainHandler!=null) {
                    mainHandler.sendEmptyMessage(SVR_EVENT_TTSNOTIFY);
                }
            }
        }
        else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_NOTIFY, rtmNotify.getTitle())){

            NotifyInfo ntf = NotifyInfo.ParseFromString(rtmNotify.getData());
            mTtsManager.TaskSpeak(ntf.content);
           // ntf.status = "0";
           // NotifyWarehouse.push(mContext, ntf, true);
           // showNewNotify();
        }else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_QUEST, rtmNotify.getTitle())){
            mTtsManager.TaskSpeak(getString(R.string.tts_quest_received));
            QuestInfo nquest = QuestInfo.ParseFromString(rtmNotify.getData());
            nquest.sStatus = "0";
            //QuestWarehouse.push(mContext, nquest, true);
            //showNewQuest();
        } else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_HB, rtmNotify.getTitle())){
            remoteWatcher = 5;
        }
        else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_TalkChannelChange, rtmNotify.getTitle())){
            changeTalkChannel(rtmNotify);
        }else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_SNAPSHOT, rtmNotify.getTitle())){
            snapshotEvent = 1;
        }
        else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_Record_Start, rtmNotify.getTitle())){
            RecordController rcIns = RecordController.getIns();
            if(rcIns != null){
                rcIns.onRecordStart();
            }
        }
        else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_Record_Stop, rtmNotify.getTitle())){
            RecordController rcIns = RecordController.getIns();
            if(rcIns != null){
                rcIns.onRecordStop();
            }
        }
        else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_SCHEDULE_SET, rtmNotify.getTitle())){
            String se = rtmNotify.getData();
            ScheduleEvent newSe = JSON.parseObject(se, ScheduleEvent.class);
            if(newSe != null){
                android.util.Log.e("RTM", "RTM_TITLE_SCHEDULE_SET: "+ se );
                ScheduleService.getIns().insertSchedule(newSe);
            }
        } else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_SCHEDULE_DELETE, rtmNotify.getTitle())){
            String se = rtmNotify.getData();
            ScheduleEvent newSe = JSON.parseObject(se, ScheduleEvent.class);
            if(newSe != null){
                ScheduleService.getIns().deleteSchedule(newSe);
            }
        }
        else{
        }

    }

    public void changeTalkChannel(RtmNotifyBean rtNotify){
        String sRid = rtNotify.getData();
        if(sRid == null){
            return;
        }
        POCENV.ins().changeChannel(sRid);
        setChannelText();
    }

    private void onTalkChannelUpdate(String cid, int err) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (err != 0) {
                    android.util.Log.e("TALK", "onTalkChannelUpdate:  "  +  err);
                    if (err == error_code_usr_offline) {
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

                    return;
                } else if (!cid.equals(POC.getChannelId())) {
                    android.util.Log.e("TALK", "onTalkChannelUpdate: channel change" );
                    return;
                }
            }
        });

    }


    private void setChannelText(){
        if(POC.getState() == Global.STATE_ONLINE){
            Channel ch = POCENV.ins().updataChannel();
            if(txCurrentChannel != null && ch != null){
                txCurrentChannel.setText(ch.getName());
                POC.join(ch.getRid());
                POCENV.ins().setCid(ch.getRid());
            }else if(txCurrentChannel != null && ch == null){
                txCurrentChannel.setText(getString(R.string.offline));
            }
        }else{
            if(txCurrentChannel != null){
                txCurrentChannel.setText(getString(R.string.offline));
            }
        }
    }

    private BroadcastReceiver update_receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            if(intent.getAction().equals(MESSAFE_SCHEDULE_ACTION)){
                String strEvent = intent.getStringExtra(KEY_MESSAGE);
                ScheduleEvent se = JSON.parseObject(strEvent, ScheduleEvent.class);
                if(se != null){
                    switch (se.getEventType()) {
                        case ScheduleBase.SE_EVENT_TYPE_TTSPLAY:{
                            mTtsManager.TaskSpeak(se.getEventData());
                        }break;
                        case ScheduleBase.SE_EVENT_TYPE_AUDIOPLAY:{
                            String audioFile = ScheduleService.getIns().getAudioSourcePath(se.getScheduleId());
                            if(audioFile != null){
                                mTtsManager.playSound(audioFile);
                            }
                        }break;
                        case ScheduleBase.SE_EVENT_TYPE_TEXT:{

                        }break;
                        case ScheduleBase.SE_EVENT_TYPE_ACTION:{
                            String sAction = se.getEventData();
                            if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_SNAPSHOT, sAction)){
                                snapshotEvent = 1;
                            }
                            else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_Record_Start, sAction)){
                                RecordController rcIns = RecordController.getIns();
                                if(rcIns != null){
                                    rcIns.onRecordStart();
                                }
                            }
                            else if(TextUtils.equals(RtmNotifyBean.RTM_TITLE_RC_Record_Stop, sAction)){
                                RecordController rcIns = RecordController.getIns();
                                if(rcIns != null){
                                    rcIns.onRecordStop();
                                }
                            }
                        }break;
                    }
                }
            }
        }
    };

    private void stopSendVideo(boolean needStop){
        //mRtcEngine.muteLocalVideoStream(needStop);
        setVideoWatching(needStop);
        if(needStop == true){
            isRemoteWatching = false;
            leaveChannel();
        }else{
            isRemoteWatching = true;
            joinChannel();
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" );
        }
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        //setupVideoProfile();
        //
       // setupLocalVideo();

    }

    private void setupVideoProfile() {
        mRtcEngine.enableVideo();
        mRtcEngine.enableAudio();
        mRtcEngine.setExternalVideoSource(true, true, true);
        mRtcEngine.setExternalAudioSource(true, 16000, 1);

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_1280x720,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE));

         /*

        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(AppCache.getIns().softConfig.getVideoWidth(),
                AppCache.getIns().softConfig.getVideoHeight(),
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
                1500,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_LANDSCAPE));
*/
        mRtcEngine.enableWebSdkInteroperability(true);

    }

    /*
    private void setupLocalVideo() {
       // mLocalContainer = findViewById(R.id.main_videoview);
        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderMediaOverlay(true);
        mLocalContainer.addView(mLocalView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }
     */

    private void joinChannel() {
        setupVideoProfile();
        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        int res = mRtcEngine.joinChannel(null, AppCache.getIns().terminalResult.getDeviceid(), "Extra Optional Data", 0);
        if(res == 0){
            isJoinedChannel = true;
        }else{
            isJoinedChannel = false;
        }
    }

    private void leaveChannel() {
        mRtcEngine.leaveChannel();
        isJoinedChannel = false;
    }

    private void updateSensor(){
        if(!sensorData.isEquals(SensorService.sensorData) ){
            if(txWater != null){
                txWater.setText(SensorService.sensorData.getWater() + "%");
            }
            if(txTemperature != null){
                txTemperature.setText(SensorService.sensorData.getTemperature() + "d");
            }
            if(txPower != null){
                txPower.setText(SensorService.sensorData.getPower() + "%");
            }
            if(txChemical != null){
                txChemical.setText(SensorService.sensorData.getChemical() + "%");
            }
            sensorData.setValue(SensorService.sensorData);
            updateSensorImg();
        }
    }

    private void updateSensorImg(){
        String packageName = mContext.getPackageName();
        int percent = 0;
        int resID = R.drawable.ic_notify;
        if(imgWater != null){
            percent = (sensorData.getWater()-1)/20;
            if(percent < 0){
                percent = 0;
            }
            if(percent > 4){
                percent = 4;
            }
            resID =  mContext.getResources().getIdentifier("water"+ percent, "drawable", packageName);
            imgWater.setImageResource(resID);
        }
        if(imgTemperature != null){
            if(sensorData.getTemperature() > 70){
                percent = 0;
            }else{
                percent = 1;
            }
            resID =  mContext.getResources().getIdentifier("temperature"+ percent, "drawable", packageName);
            imgTemperature.setImageResource(resID);
        }
        if(imgPower != null){
            percent = (sensorData.getPower()-1)/20;
            if(percent < 0){
                percent = 0;
            }
            if(percent > 4){
                percent = 4;
            }
            resID =  mContext.getResources().getIdentifier("power"+ percent, "drawable", packageName);
            imgPower.setImageResource(resID);
        }
        if(imgChemical != null){
            percent = (sensorData.getChemical()-1)/20;
            if(percent < 0){
                percent = 0;
            }
            if(percent > 4){
                percent = 4;
            }
            resID =  mContext.getResources().getIdentifier("chemical"+ percent, "drawable", packageName);
            imgChemical.setImageResource(resID);
        }
    }

    private final BroadcastReceiver mSpeakingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent i) {
            if(POCENV.ACT_CHANNEL_SPEAKING.equals(i.getAction())){
                String messge = i.getStringExtra(KEY_MESSAGE);
                if(txCurrentSpeakingUser != null){
                    txCurrentSpeakingUser.setText(messge);
                }
            }
        }
    };

    protected void showNewNotify(){
        View ntfView = findViewById(R.id.main_notify_frame);
        TextView tvNotfiyTitle = findViewById(R.id.main_notify_title);
        TextView tvNotfiyTime = findViewById(R.id.main_notify_time);
        TextView tvNotfiySender = findViewById(R.id.main_notify_sender);
        TextView tvNotfiyContent = findViewById(R.id.main_notify_content);

        List<NotifyInfo> newNtfList = NotifyWarehouse.loadNew(mContext);
        if(newNtfList.size() <= 0){
            tvNotfiyTitle.setText("");
            tvNotfiyTime.setText("");
            tvNotfiySender.setText("");
            tvNotfiyContent.setText("");
            ntfView.setVisibility(View.GONE);
        }else{
            ntfView.setVisibility(View.VISIBLE);
            NotifyInfo newNtf = newNtfList.get(0);
            tvNotfiyTitle.setText(newNtf.title);
            tvNotfiyTime.setText(newNtf.time);
            tvNotfiySender.setText(newNtf.sender);
            tvNotfiyContent.setText(newNtf.content);

            ImageView ivRead = findViewById(R.id.main_notify_read);
            ivRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    POCENV.ins().playTone(30,100);
                    //readNotify(newNtf, v);
                    NotifyWarehouse.read(mContext, newNtf);
                    showNewNotify();
                }
            });
        }
    }

    protected void showNewQuest(){
        View qstView = findViewById(R.id.main_quest_frame);
        TextView tvQuestTitle = findViewById(R.id.main_quest_title);
        TextView tvQuestTime = findViewById(R.id.main_quest_time);
        TextView tvQuestSender = findViewById(R.id.main_quest_sender);
        TextView tvQuestContent = findViewById(R.id.main_quest_content);
        TextView tvQuestAddress = findViewById(R.id.main_quest_address);

        List<QuestInfo> newQuestList = QuestWarehouse.loadNew(mContext);
        if(newQuestList.size() <= 0){
            qstView.setVisibility(View.GONE);
        }else{
            qstView.setVisibility(View.VISIBLE);
            QuestInfo newQuest = newQuestList.get(0);
            tvQuestTitle.setText(newQuest.sTitle);
            tvQuestTime.setText(newQuest.sTime);
            tvQuestSender.setText(newQuest.sSender);
            tvQuestContent.setText(newQuest.sContent);
            tvQuestAddress.setText(newQuest.sAddress);

            ImageView ivOperate = findViewById(R.id.main_quest_operate);
            ivOperate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<QuestInfo> questLst = QuestWarehouse.loadNew(mContext);
                    QuestInfo nq = questLst.get(0);
                    POCENV.ins().playTone(30,100);
                    Intent intent = new Intent();
                    intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
                    intent.putExtra("KEY_TYPE", 10038);
                    intent.putExtra("POINAME",nq.sAddress);
                    intent.putExtra("LAT",nq.latitude);
                    intent.putExtra("LON",nq.longitude);
                    intent.putExtra("DEV",0);
                    intent.putExtra("STYLE",0);
                    intent.putExtra("SOURCE_APP","com.jy.mfe");
                    sendBroadcast(intent);
                }
            });

            ImageView ivIgnore = findViewById(R.id.main_quest_ignore);
            ivIgnore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    POCENV.ins().playTone(30,100);
                    List<QuestInfo> questLst = QuestWarehouse.loadNew(mContext);
                    QuestInfo nq = questLst.get(0);
                    nq.sStatus = "2";
                    QuestWarehouse.update(mContext, nq, true);
                    QuestWarehouse.move(mContext, newQuest);
                    showNewQuest();
                }
            });
        }
    }

    void setVideoRecordingFlag(){
        ImageView ivRecording = findViewById(R.id.main_state_recording);
        if(cRecord.autoRecord == true){
            ivRecording.setVisibility(View.VISIBLE);
            AppCache.getIns().terminalHB.setStatus(getString(R.string.startrecord));

        }else{
            ivRecording.setVisibility(View.INVISIBLE);
            AppCache.getIns().terminalHB.setStatus(getString(R.string.stoprecord));
        }
        if(isRemoteWatching == true){
            mChatManager.sendChannelMessage(videoLiveRtmID, JSONObject.toJSONString(AppCache.getIns().terminalHB));
        }
    }

    void setVideoWatching(boolean bWatchingStop){
        ImageView ivWatching = findViewById(R.id.main_state_watching);
        if(bWatchingStop == true){
            ivWatching.setVisibility(View.INVISIBLE);
        }else{
            ivWatching.setVisibility(View.VISIBLE);
        }
    }

    public void startRecord(){

    }

    public void stopRecord(){

    }


    public class NetworkChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int netWorkStates = NetworkUtils.getNetWorkState(context);

            switch (netWorkStates) {
                case NetworkUtils.TYPE_NONE:

                    break;
                case NetworkUtils.TYPE_MOBILE:

                    break;
                case NetworkUtils.TYPE_WIFI:

                    break;
                default:
                    break;
            }


        }
    }

    public boolean deleteFile() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/lvr.apk");
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    };
    @SuppressLint("NewApi")
    private void installFile(long id, Context context) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        Uri downloadFileUri;
        //String  img_url = Environment.getExternalStorageDirectory()+ "/download"
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS +"/lvr.apk");
        if (file != null) {
            String path = file.getAbsolutePath();
            downloadFileUri = Uri.parse("file://" + path);
            install.setDataAndType(downloadFileUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        }
    }
    public void updataApk(final String bucketName, final String apkPath) {
        new Thread(){
            @Override
            public void run() {
                try{
                    deleteFile();
                    BosClientConfiguration config = new BosClientConfiguration();
                    config.setCredentials(new DefaultBceCredentials("1df16ec2f3dd4f60955892cdc19718b3", "c495e80444b348a18e79413f5583ef92"));
                    config.setEndpoint("http://su.bcebos.com");
                    BosClient client = new BosClient(config);
                    GetObjectRequest getObjectRequest1 = new GetObjectRequest(bucketName, apkPath);
                    String  img_url = Environment.getExternalStorageDirectory()+ "/download";
                    ObjectMetadata apkFile = client.getObject(getObjectRequest1, new File(img_url,"lvr.apk"));

                    installFile(0, getApplicationContext());
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


    public class EmcChannelListener implements RtmChannelListener {
        @Override
        public void onMemberCountUpdated(int i) {
            android.util.Log.e("RTM", "onMemberCountUpdated: " + i);
            rmtChannelMember = i;
        }

        @Override
        public void onAttributesUpdated(List<RtmChannelAttribute> list) {
        }

        @Override
        public void onMessageReceived(final RtmMessage message, final RtmChannelMember fromMember) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onImageMessageReceived(RtmImageMessage rtmImageMessage, RtmChannelMember rtmChannelMember) {
        }

        @Override
        public void onFileMessageReceived(RtmFileMessage rtmFileMessage, RtmChannelMember rtmChannelMember) {
        }

        @Override
        public void onMemberJoined(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //android.util.Log.e("RTM", "onMemberJoined: " + member.getUserId());
                    if(isRemoteWatching == true){
                        mChatManager.sendChannelMessage(videoLiveRtmID, JSONObject.toJSONString(AppCache.getIns().terminalHB));
                    }
                }
            });
        }
        @Override
        public void onMemberLeft(RtmChannelMember member) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    android.util.Log.e("RTM", "onMemberLeft: " + member.getUserId());
                }
            });
        }
    }

    private void downloadSchedules(){
        org.json.JSONObject jo = new org.json.JSONObject();
        HK.putJo(jo, "deviceid", AppCache.getIns().terminalResult.getDeviceid());
        HK.putJo(jo, "type", ScheduleBase.PropertySchedule);
        new HttpPost(HK.FIRECTRI_PropertyGet, jo.toString(),  new HttpPost.ResultListener() {
            @Override
            public void onSuccess( String msg) {
                try {
                    org.json.JSONObject jo = new org.json.JSONObject(msg);
                    android.util.Log.e("RTM", "downloadSchedules: " + msg );
                    ScheduleService.getIns().parseFromServer(jo.getString("data"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String msg) {
                android.util.Log.e("RTM", "downloadSchedules: " + msg );
            }
        });
    }

    private void pickWatsonKeyword(String watsonResult){
        if(watsonResult ==null){
            return;
        }

        String filtedWaston = watsonResult.replaceAll(" ","");
        android.util.Log.e("watson", "pickWatsonKeyword: " + filtedWaston );
        boolean bFindKey = false;
        for (String sKey:keywordHelp) {
            if(filtedWaston.contains(sKey)){
                bFindKey = true;
                break;
            }
        }

        if(bFindKey == true){
            android.util.Log.e("watson", "pickWatsonKeyword: find help"  );

            TerminalAlarm terAlarm = new TerminalAlarm(AppCache.getIns().terminalHB);
            terAlarm.setMajor("emc");
            terAlarm.setMinor("help");
            terAlarm.setInfo(watsonResult);
            sendAlarmToStation(terAlarm);

        }

    }

    private void sendAlarmToStation(TerminalAlarm terAlarm){
        RtmNotifyBean rtmLoc = new RtmNotifyBean();
        rtmLoc.setTitle(RtmNotifyBean.RTM_TITLE_TerminalAlarm);
        rtmLoc.setSender(AppCache.getIns().terminalResult.getName());
        rtmLoc.setDepartment(AppCache.getIns().terminalResult.getStation());
        rtmLoc.setData(JSONObject.toJSONString(terAlarm));
        String peerID = CommonUtil.getStringMD5(AppCache.getIns().terminalResult.getStation());
        String content = JSONObject.toJSONString(rtmLoc);
        mChatManager.sendPeerMessage(content,peerID );
    }
}
