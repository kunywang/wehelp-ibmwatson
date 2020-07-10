package com.jy.mfe.audio;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author kunpn
 */
public class AudioEncoder {
    public static final String TAG = AudioEncoder.class.getSimpleName();
    private static final String AUDIO_MIME_TYPE = "audio/mp4a-latm";
    private MediaCodec aEncoder;

    private MediaCodecInfo audioCodecInfo;
    private MediaFormat audioFormat;
    private Thread audioEncoderThread;
    private volatile boolean audioEncoderLoop = false;
    private volatile boolean aEncoderEnd = false;
    private LinkedBlockingQueue<byte[]> audioQueue;
    private long presentationTimeUs;
    private final int TIMEOUT_USEC = 10000;
    private boolean workingState = false;

    private AudioEncodeCallback mCallback;
    final int PCM_BIT = AudioFormat.ENCODING_PCM_16BIT;
    private int mPcmFormat;
    private int mChanelCount;
    private int mSampleRate;
    public void initAudioEncoder(int sampleRate, int pcmFormat, int chanelCount){
        mSampleRate = sampleRate;
        mPcmFormat = pcmFormat;
        mChanelCount = chanelCount;

        audioQueue = new LinkedBlockingQueue<>();
        audioCodecInfo = selectCodec(AUDIO_MIME_TYPE);
        if (audioCodecInfo == null) {
            Log.e(TAG, "= =lgd= Unable to find an appropriate codec for " + AUDIO_MIME_TYPE);
            return;
        }
        Log.d(TAG, "==AAC===selected codec: " + audioCodecInfo.getName());
        audioFormat = MediaFormat.createAudioFormat(AUDIO_MIME_TYPE, sampleRate, 1);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        int bitRate = sampleRate * pcmFormat * chanelCount;
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);

        Log.d(TAG, " =AAC=====format: " + audioFormat.toString());

        if (aEncoder != null) {
            return;
        }
        try {
            aEncoder = MediaCodec.createEncoderByType(AUDIO_MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("====初始化音频编码器失败", e);
        }

         aEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        presentationTimeUs = System.currentTimeMillis() * 1000;
        aEncoder.start();
        workingState = true;
    }

    public MediaFormat getAudioFormat(){
        if(aEncoder != null){
            aEncoder.getOutputFormat();
        }
        return null;
    }

    public void setCallback(AudioEncodeCallback callback) {
        this.mCallback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    public int destroyEncoder() {
        if(aEncoder != null){
            aEncoder.flush();
            aEncoder.stop();
            aEncoder.release();
            aEncoder = null;
            workingState = false;
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void encodeAudioData(byte[] input){
        if(workingState == false){
            return;
        }
        try {

            ByteBuffer[] inputBuffers = aEncoder.getInputBuffers();
            //得到当前有效的输入缓冲区的索引
            int inputBufferIndex = aEncoder.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufferIndex >= 0) {

                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.put(input);

                //计算pts，这个值是一定要设置的
                if(presentationTimeUs == 0){
                    presentationTimeUs = System.currentTimeMillis()*1000;
                }
                long pts = System.currentTimeMillis()*1000 - presentationTimeUs;
                if (aEncoderEnd) {
                    aEncoder.queueInputBuffer(inputBufferIndex, 0, input.length,
                            pts, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    aEncoder.queueInputBuffer(inputBufferIndex, 0, input.length,
                            pts, 0);
                }
            }

            //拿到输出缓冲区,用于取到编码后的数据
            ByteBuffer[] outputBuffers = aEncoder.getOutputBuffers();
            MediaCodec.BufferInfo aBufferInfo = new MediaCodec.BufferInfo();;
            int outputBufferIndex = aEncoder.dequeueOutputBuffer(aBufferInfo, TIMEOUT_USEC);
            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                outputBuffers = aEncoder.getOutputBuffers();
            }else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
               // Log.d(TAG, "= =lgd= ==Audio===INFO_OUTPUT_FORMAT_CHANGED===");
                //加入音轨的时刻,一定要等编码器设置编码格式完成后，再将它加入到混合器中，
                // 编码器编码格式设置完成的标志是dequeueOutputBuffer得到返回值为MediaCodec.INFO_OUTPUT_FORMAT_CHANGED
                final MediaFormat newformat = aEncoder.getOutputFormat();
                if (null != mCallback && !aEncoderEnd) {
                    mCallback.outMediaFormat(1, newformat);
                }
            }
            while (outputBufferIndex >= 0) {
                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + outputBufferIndex +
                            " was null");
                }

                if ((aBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    aBufferInfo.size = 0;
                }

                if (aBufferInfo.size != 0) {
                    if (null != mCallback && !aEncoderEnd) {
                        byte[] outData = new byte[aBufferInfo.size];
                        outputBuffer.position(aBufferInfo.offset);
                        outputBuffer.get(outData);
                        mCallback.outputAudioFrame(1,outData, aBufferInfo);
                    }
                }
                //释放资源
                aEncoder.releaseOutputBuffer(outputBufferIndex, false);
                //拿到输出缓冲区的索引
                outputBufferIndex = aEncoder.dequeueOutputBuffer(aBufferInfo, 0);
                //编码结束的标志
                if ((aBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.e(TAG, "= =AAC= =Recv Audio Encoder===BUFFER_FLAG_END_OF_STREAM=====");
                   // audioEncoderLoop = false;
                   // audioEncoderThread.interrupt();
                   // return;
                }
            }
        } catch (Exception t) {
            Log.e(TAG, "= =AAC= =encodeAudioData=====error: " + t.toString());
        }
    }
}
