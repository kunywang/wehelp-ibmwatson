package com.jy.mfe.audio;

import android.media.AudioRecord;
import android.os.SystemClock;

import com.jy.mfe.BuildConfig;

import java.lang.ref.WeakReference;

public class AudioIOThread extends Thread {


    private WeakReference<AudioIOManager> rAIOM;

    AudioIOThread(AudioIOManager aiom) {
        super();
        rAIOM = new WeakReference<>(aiom);
//        setPriority(Thread.MAX_PRIORITY);
    }

    @Override
    public void start() {
        final AudioIOManager aiom = rAIOM.get();

        if (aiom != null &&
                aiom.bufferSize > 0 &&
                aiom.audioRecord != null &&
                aiom.audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            super.start();
        }
    }

    @Override
    public void run() {
        try {
            final byte[] buffer = new byte[rAIOM.get().bufferSize];

            //byte[] monobuffer = new byte[rAIOM.get().bufferSize/2];
            long tick;

            rAIOM.get().audioRecord.startRecording();
            AudioIOCallback[] c = rAIOM.get().callbacks;

            while (rAIOM.get().audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                int result = rAIOM.get().audioRecord.read(buffer, 0, buffer.length);
                tick = SystemClock.currentThreadTimeMillis();

               // for(int i = 0; i < monobuffer.length/2; i++){
                 //   System.arraycopy(buffer, 0 + i*4, monobuffer, 0+i*2, 2);
               // }

                for (AudioIOCallback p : c) {
                    // Make sure the callback goes as fast as possible,
                    // other wise the record will lost data.
                    try {
                        p.onData(buffer, result, tick);
                    } catch (Exception e) {
                        if (BuildConfig.DEBUG) {
                            e.printStackTrace();
                        }
                    }
                }
                //Thread.sleep(10);
                yield();
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                rAIOM.get().stop();
            } catch (Exception e2) {
                if (BuildConfig.DEBUG) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
