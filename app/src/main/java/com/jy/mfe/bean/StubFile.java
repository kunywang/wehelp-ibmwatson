package com.jy.mfe.bean;

import java.io.Serializable;

/**
 * @author kunpn
 */
public class StubFile implements Serializable {

    private static final long serialVersionUID = -6059543913231543948L;

    private static final String STUB_FILE_TYPE_VIDEO = "mp4";
    private static final String STUB_FILE_TYPE_JPG = "jpg";

    private String number;
    private String path;
    private String url;
    private String bucket;
    private String filename;
    private String filetype;
    private String deviceid;
    private String station;
    private String user;
    private String createtime;
    private String uploadtime;
    private String address;
    private double longitude;
    private double latitude;

    public StubFile() {
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFilename() {
        return filename;
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    public String getFiletype() {
        return filetype;
    }
    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getCreatetime() {
        return createtime;
    }
    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }
    public String getUploadtime() {
        return uploadtime;
    }
    public void setUploadtime(String uploadtime) {
        this.uploadtime = uploadtime;
    }
    public String getDeviceid() {
        return deviceid;
    }
    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getStation() {
        return station;
    }
    public void setStation(String station) {
        this.station = station;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
