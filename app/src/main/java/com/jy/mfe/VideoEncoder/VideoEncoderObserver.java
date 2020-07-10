package com.jy.mfe.VideoEncoder;


import android.media.MediaCodec;
import android.media.MediaFormat;

public interface VideoEncoderObserver
{
    abstract public void outMediaFormat(MediaFormat mediaFormat);
    abstract public int onEncodeData(byte[] data, final MediaCodec.BufferInfo bufferInfo);
}
