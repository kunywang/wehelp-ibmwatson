package com.jy.mfe.tts;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.util.LinkedList;

/**
 * 当前DEMO的播报方式是队列模式。其原理就是依次将需要播报的语音放入链表中，播报过程是从头开始依次往后播报。
 * <p>
 * 导航SDK原则上是不提供语音播报模块的，如果您觉得此种播报方式不能满足你的需求，请自行优化或改进。
 */
public class TTSController implements ICallBack {


    @Override
    public void onCompleted(int code) {
        if (handler != null) {
            handler.obtainMessage(1).sendToTarget();
        }
    }

    public static enum TTSType {
        /**
         * 讯飞语音
         */
        IFLYTTS,
        /**
         * 系统语音
         */
        SYSTEMTTS;
    }

    public static TTSController ttsManager;
    private Context mContext;
    private TTS tts = null;

    private IFlyTTS iflyTTS = null;
    private LinkedList<String> wordList = new LinkedList<String>();
    private final int TTS_PLAY = 1;
    private final int CHECK_TTS_PLAY = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TTS_PLAY:
                    if (tts != null && wordList.size() > 0) {
                        tts.playText(wordList.removeFirst());
                    }
                    break;
                case CHECK_TTS_PLAY:
                    if (!tts.isPlaying()) {
                        handler.obtainMessage(1).sendToTarget();
                    }
                    break;
            }

        }
    };

    public void setTTSType(TTSType type) {
        //if (type == TTSType.SYSTEMTTS) {
         //   tts = systemTTS;
       // } else {
            tts = iflyTTS;
        //}
        tts.setCallback(this);
    }

    private TTSController(Context context) {
        mContext = context.getApplicationContext();
        //systemTTS = SystemTTS.getInstance(mContext);
        iflyTTS = IFlyTTS.getInstance(mContext);
        tts = iflyTTS;
    }

    public void init() {
        if (iflyTTS != null) {
            iflyTTS.init();
        }
        tts.setCallback(this);
    }

    public static TTSController getInstance(Context context) {
        if (ttsManager == null) {
            ttsManager = new TTSController(context);
        }
        return ttsManager;
    }

    public void stopSpeaking() {
        if (iflyTTS != null) {
            iflyTTS.stopSpeak();
        }
        wordList.clear();
    }

    public void destroy() {
        if (iflyTTS != null) {
            iflyTTS.destroy();
        }
        ttsManager = null;
    }

    public void TaskSpeak(String sTaskContent)
    {
        if (wordList != null) {
            wordList.addLast(sTaskContent);
        }

        handler.obtainMessage(CHECK_TTS_PLAY).sendToTarget();
    }

    private MediaPlayer mSoundMediaPlayer = null;
    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mSoundMediaPlayer != null) {
                        mSoundMediaPlayer.stop();
                        mSoundMediaPlayer.release();
                        mSoundMediaPlayer = null;
                    }
                }
            };
    public void playSound(int id) {
        if (mSoundMediaPlayer != null) {
            mSoundMediaPlayer.stop();
            mSoundMediaPlayer.release();
            mSoundMediaPlayer = null;
        }
        mSoundMediaPlayer = MediaPlayer.create(mContext, id);

        if (mSoundMediaPlayer != null) {
            if (!mSoundMediaPlayer.isPlaying()) {
                mSoundMediaPlayer.setOnCompletionListener(mCompletionListener);
                mSoundMediaPlayer.setLooping(false);
                mSoundMediaPlayer.start();
            }
        }
    }

    public void playSound(String  audioFilePath) {
        if (mSoundMediaPlayer != null) {
            mSoundMediaPlayer.stop();
            mSoundMediaPlayer.release();
            mSoundMediaPlayer = null;
        }
        try {
            mSoundMediaPlayer = new MediaPlayer();
            mSoundMediaPlayer.setDataSource(audioFilePath);
            if (mSoundMediaPlayer != null) {
                if (!mSoundMediaPlayer.isPlaying()) {
                    mSoundMediaPlayer.setOnCompletionListener(mCompletionListener);
                    mSoundMediaPlayer.setLooping(false);
                    mSoundMediaPlayer.prepare();
                    mSoundMediaPlayer.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
