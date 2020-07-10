package com.jy.mfe.audio;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * It is the audio record manager.
 */
public class AudioIOManager {

    private static AudioIOManager ins;

    int bufferSize;
    AudioRecord audioRecord;
    AudioIOCallback[] callbacks;
    final int PCM_BIT = AudioFormat.ENCODING_PCM_16BIT;

    public AudioIOManager(int sampleRate, int channel, AudioIOCallback... callbacks) {
        //if (sampleRate != 8000 && sampleRate != 44100) {
         //   sampleRate = 44100;
       // }

        bufferSize = 1280;//AudioRecord.getMinBufferSize(sampleRate, channel, PCM_BIT);

        if (AudioRecord.ERROR == bufferSize ||
                AudioRecord.ERROR_BAD_VALUE == bufferSize) {
            // Failed to get buffer
            return;
        }

        this.callbacks = callbacks;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channel,
                PCM_BIT, bufferSize);

    }

    public void start() {
        new AudioIOThread(this).start();
    }

    public void stop() {
        if (audioRecord != null) {
            if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord.stop();
            }
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.release();
            }
        }
    }

    public byte[] createExternalBuffer() {
        return new byte[bufferSize];
    }
    public byte[] createPOCBuffer() {
        return new byte[bufferSize/4];
    }
}
