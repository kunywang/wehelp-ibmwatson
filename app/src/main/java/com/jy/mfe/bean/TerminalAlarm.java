package com.jy.mfe.bean;


import com.jy.mfe.util.VCUtil;

import java.io.Serializable;

/**
 * @author kunpn
 */
public class TerminalAlarm implements Serializable {
    private static final long serialVersionUID = 8932572550481585792L;

    private String request;
    private String deviceid;
    private double longitude;
    private double latitude;
    private double height;
    private double bearing;
    private double speed;
    private String locate;
    private String major;
    private String minor;
    private int arg0;
    private int arg1;
    private int arg2;
    private double val0;
    private double val1;
    private double val2;
    private String info;
    private String time;

    public TerminalAlarm() {
        request = "alarm";
        deviceid = "";
        longitude= 0;
        latitude= 0;
        height= 0;
        bearing= 0;
        speed= 0;
        locate= "";
        major= "";
        minor= "";
        arg0= 0;
        arg1= 0;
        arg2= 0;
        val0= 0;
        val1= 0;
        val2= 0;
        info= "";
    }

    public TerminalAlarm(final TerminalHeartBeat thb) {
        request = "alarm";
        deviceid = thb.getDeviceid();
        longitude= thb.getLongitude();
        latitude= thb.getLatitude();
        height= thb.getHeight();
        bearing= thb.getBearing();
        speed= thb.getSpeed();
        locate= thb.getAddress();
        arg0= thb.getArg0();
        arg1= thb.getArg1();
        arg2= thb.getArg2();
        val0= thb.getVal0();
        val1= thb.getVal1();
        val2= thb.getVal2();

        time  = VCUtil.getTime();
    }

    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }

    public String getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getHeight() {
        return height;
    }
    public void setHeight(double height) {
        this.height = height;
    }

    public double getBearing() {
        return bearing;
    }
    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getLocate() {
        return locate;
    }
    public void setLocate(String locate) {
        this.locate = locate;
    }

    public String getMajor() {
        return major;
    }
    public void setMajor(String major) {
        this.major = major;
    }

    public String getMinor() {
        return minor;
    }
    public void setMinor(String minor) {
        this.minor = minor;
    }

    public int getArg0() {
        return arg0;
    }
    public void setArg0(int arg0) {
        this.arg0 = arg0;
    }

    public int getArg1() {
        return arg1;
    }
    public void setArg1(int arg1) {
        this.arg1 = arg1;
    }

    public int getArg2() {
        return arg2;
    }
    public void setArg2(int arg2) {
        this.arg2 = arg2;
    }

    public double getVal0() {
        return val0;
    }
    public void setVal0(double val0) {
        this.val0 = val0;
    }

    public double getVal1() {
        return val1;
    }
    public void setVal1(double val1) {
        this.val1 = val1;
    }

    public double getVal2() {
        return val2;
    }
    public void setVal2(double val2) {
        this.val2 = val2;
    }

    public String getInfo() {
        return info;
    }
    public void setInfo(String info) {
        this.info = info;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
}

