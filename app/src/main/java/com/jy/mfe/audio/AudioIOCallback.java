package com.jy.mfe.audio;

public interface AudioIOCallback {
    void onData(byte[] buffer, int size, long tick);
}
