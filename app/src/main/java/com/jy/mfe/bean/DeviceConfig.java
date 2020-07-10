package com.jy.mfe.bean;

import java.io.Serializable;

/**
 * @author kunpn
 */
public class DeviceConfig implements Serializable {
    private int locateInterval;
    private int autoRecord;
    private int videoWidth;
    private int videoHeight;
    private int videoBitRate;

    public int getLocateInterval() {
        return locateInterval;
    }
    public void setLocateInterval(int locateInterval) {
        this.locateInterval = locateInterval;
    }

    public int getAutoRecord() {
        return autoRecord;
    }
    public void setAutoRecord(int autoRecord) {
        this.autoRecord = autoRecord;
    }

    public int getVideoWidth() {
        return videoWidth;
    }
    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }
    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getVideoBitRate() {
        return videoBitRate;
    }
    public void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

}
