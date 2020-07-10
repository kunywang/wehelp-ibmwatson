package com.jy.mfe.bean;

import java.io.Serializable;

public class TTSNotify implements Serializable {
    private static final long serialVersionUID = -8017969918897400526L;

    private String text;
    private int sleep;
    private int loop;

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public int getSleep() {
        return sleep;
    }
    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getLoop() {
        return loop;
    }
    public void setLoop(int loop) {
        this.loop = loop;
    }
}
