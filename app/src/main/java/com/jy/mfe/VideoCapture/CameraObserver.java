package com.jy.mfe.VideoCapture;


public interface CameraObserver
{
    abstract public int onData(byte[] data, int width, int height);
}
