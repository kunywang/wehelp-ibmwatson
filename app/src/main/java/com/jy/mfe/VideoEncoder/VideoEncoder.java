package com.jy.mfe.VideoEncoder;


public interface VideoEncoder
{
    abstract public int createEncoder(VideoEncoderObserver observer, int nFrameRate, int nBitRate);
    abstract public int destroyEncoder();
    abstract public int encode(byte[] data);
    abstract public int getBitrate();
}
