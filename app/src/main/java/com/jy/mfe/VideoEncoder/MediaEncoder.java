package com.jy.mfe.VideoEncoder;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;

import com.jy.mfe.cache.AppCache;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaEncoder implements VideoEncoder
{
    static final String VCODEC_MIME = "video/avc";
    private long presentationTimeUs;
    private boolean workingState = false;
    MediaFormat mediaFormat;
    private MediaCodecInfo getCodecInfo(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (mimeType.equalsIgnoreCase(type)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    @Override
    public int getBitrate()
    {
        return m_bitRate / 1024;
    }

    @Override
    public int createEncoder(VideoEncoderObserver observer, int nFrameRate, int nBitRate) {
        try {
            m_width = AppCache.getIns().softConfig.getVideoWidth();
            m_height = AppCache.getIns().softConfig.getVideoHeight();
            m_bitRate = nBitRate * 1024*3;
            m_nFrameRate = nFrameRate;
            intialConvert();
            m_observer = observer;
            MediaCodecInfo mediaCodecInfo = getCodecInfo(VCODEC_MIME);
            m_mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());

            mediaFormat = MediaFormat.createVideoFormat(VCODEC_MIME, m_width, m_height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, m_bitRate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_nFrameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
           // mediaFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
          //  mediaFormat.setInteger("level", MediaCodecInfo.CodecProfileLevel.AVCLevel41);
            m_mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            presentationTimeUs = System.currentTimeMillis() * 1000;
            m_mediaCodec.start();
            workingState = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int intialConvert() {
        m_convertor.setSize(m_width, m_height);
        m_convertor.setSliceHeigth(m_height);
        m_convertor.setStride(m_width);
        m_convertor.setYPadding(0);
        m_convertor.setEncoderColorFormat(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
        return 0;
    }

    @Override
    public int encode(byte[] data)
    {
        if(workingState == false){
            return 0;
        }
        ByteBuffer[] inputBuffers = m_mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = m_mediaCodec.getOutputBuffers();
        try
        {
            int bufferIndex = m_mediaCodec.dequeueInputBuffer(5000000);
            if (bufferIndex >= 0)
            {
                inputBuffers[bufferIndex].clear();
                m_convertor.convert(data, inputBuffers[bufferIndex]);
               // inputBuffers[bufferIndex].put(data, 0, data.length);

                if(presentationTimeUs == 0){
                    presentationTimeUs = System.currentTimeMillis()*1000;
                }
                long pts = System.currentTimeMillis()*1000 - presentationTimeUs;
                m_mediaCodec.queueInputBuffer(bufferIndex, 0,
                        inputBuffers[bufferIndex].position(),
                        pts, 0);
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = m_mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                    final MediaFormat newformat = m_mediaCodec.getOutputFormat();
                    m_observer.outMediaFormat(newformat);

                }
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
                    m_observer.onEncodeData(outData, bufferInfo);
                    m_mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = m_mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        }
        return 0;
    }

    public MediaFormat getVideoFormat(){
        if(m_mediaCodec != null){
            m_mediaCodec.getOutputFormat();
        }
        return null;
    }
    @Override
    public int destroyEncoder() {
        if(m_mediaCodec != null){
            m_mediaCodec.flush();
            m_mediaCodec.stop();
            m_mediaCodec.release();
            workingState = false;
        }
        return 0;
    }

    private int m_nFrameRate = 25;
    private int m_bitRate = 0;
    private int m_width = 1280;
    private int m_height = 720;
    private VideoEncoderObserver m_observer = null;
    private MediaCodec m_mediaCodec = null;
    private NV21Convertor m_convertor = new NV21Convertor();
}
