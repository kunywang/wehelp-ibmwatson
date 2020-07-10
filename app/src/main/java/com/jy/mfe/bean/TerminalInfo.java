package com.jy.mfe.bean;


import java.io.Serializable;

/**
 * @author kuny on 2019/8/19.
 * Email： 3972933@qq.com
 */
public class TerminalInfo implements Serializable {

    private static final long serialVersionUID = -5155805773783798825L;
    private String number;
    private String deviceid;
    private String devicetype;
    private String name;
    private String showname;
    private String station;
    private String model;
    private String username;
    private String userphone;
    private String address;
    private double setuplongitude;
    private double setuplatitude;
    private String detail;
    private String createtime;
    private String action;
    private String status;
    private double longitude;
    private double latitude;

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
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

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getShowname() {
        return showname;
    }
    public void setShowname(String showname) {
        this.showname = showname;
    }

    public String getStation() {
        return station;
    }
    public void setStation(String station) {
        this.station = station;
    }

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserphone() {
        return userphone;
    }
    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public double getSetuplongitude() {
        return setuplongitude;
    }
    public void setSetuplongitude(double setuplongitude) {
        this.setuplongitude = setuplongitude;
    }

    public double getSetuplatitude() {
        return setuplatitude;
    }
    public void setSetuplatitude(double setuplatitude) {
        this.setuplatitude = setuplatitude;
    }

    public String getDetail() {
        return detail;
    }
    public void setDetaile(String detail) {
        this.detail = detail;
    }


    public String getCreatetime() {
        return createtime;
    }
    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
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
}

