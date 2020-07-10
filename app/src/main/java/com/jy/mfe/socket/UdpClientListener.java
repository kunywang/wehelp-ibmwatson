package com.jy.mfe.socket;

public interface UdpClientListener {

    public void OnUDPPackageRecive(String buffer);
    public void OnUDPPackageReciveBytes(byte []data,int len);
}