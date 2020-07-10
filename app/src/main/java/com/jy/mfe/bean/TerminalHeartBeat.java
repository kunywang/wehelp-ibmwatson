package com.jy.mfe.bean;


import java.io.Serializable;
/**
 * @author kunpn
 */
public class TerminalHeartBeat implements Serializable {
    private static final long serialVersionUID = 2797586904981472498L;


    private String request;
    private String deviceid;
    private String devicetype;
    private String station;
    private double longitude;
    private double latitude;
    private double height;
    private double bearing;
    private double speed;
    private String address;
    private String locate;
    private String status;
    private double power;
    private int arg0;
    private int arg1;
    private int arg2;
    private double val0;
    private double val1;
    private double val2;
    private String info;

    public TerminalHeartBeat()
    {
        request = "heartbeat";
        devicetype = "me2";
        deviceid = "";
        longitude= 0;
        latitude= 0;
        height= 0;
        bearing= 0;
        speed= 0;
        locate= "";
        status= "";
        power= 0;
        arg0= 0;
        arg1= 0;
        arg2= 0;
        val0= 0;
        val1= 0;
        val2= 0;
        info= "";
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

    public String getDevicetype() {
        return devicetype;
    }
    public void setDevicetype(String devicetype) {
        this.devicetype = devicetype;
    }

    public String getStation() {
        return station;
    }
    public void setStation(String station) {
        this.station = station;
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

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocate() {
        return locate;
    }
    public void setLocate(String locate) {
        this.locate = locate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public double getPower() {
        return power;
    }
    public void setPower(double power) {
        this.power = power;
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

}

