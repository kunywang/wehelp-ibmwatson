package com.jy.mfe.socket;

import android.net.wifi.WifiManager;
import android.util.Log;

import com.jy.mfe.cache.AppCache;
import com.jy.mfe.http.HK;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;


public class UdpClient implements Runnable{

	UdpClientListener mSocketEventListener = null;
	public    Boolean IsThreadDisable = false;
    private  WifiManager.MulticastLock lock;
    InetAddress mInetAddress;

    private int local_port = 51987;
    InetAddress udp_svr = null;

    private DatagramSocket datagramSocket;
    public UdpClient(WifiManager manager) {
         lock= manager.createMulticastLock("UDPwifi");
         try {
			datagramSocket = new DatagramSocket();
			local_port = datagramSocket.getLocalPort();
	        datagramSocket.setBroadcast(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         udp_svr = HK.mServerIPAddress;
    }
    
    
	public void setListener(UdpClientListener listener){
		mSocketEventListener = listener;
	}
    
    public void StartListen()  {

        byte[] message = new byte[8192];
        Arrays.fill(message,(byte)0);

            DatagramPacket datagramPacket = new DatagramPacket(message,  message.length);
            try {
                while (!IsThreadDisable) {

					lock.acquire();

					datagramSocket.receive(datagramPacket);
					byte[] data = datagramPacket.getData();
					int head = data[0];
					head &= 0xff;

					Log.d("UDP Rec BYTE", datagramPacket.getAddress().getHostAddress().toString() + ":" + data.length);
					mSocketEventListener.OnUDPPackageReciveBytes(data, data.length);

					Arrays.fill(message, (byte) 0);

                    this.lock.release();
                }
            } catch (IOException e) {//IOException
                e.printStackTrace();
            }


    }
    public  void send_udp(String message) {
        message = (message == null ? "Hello IdeasAndroid!" : message);

        Log.d("UDP ", "UDP send :"+message + " To : " + udp_svr);
   
        byte[] messageByte = message.getBytes();

         int msg_length = messageByte.length; 
         
        //byte[] bytesSend = new byte[1024*8];


		int sendlen = 0;
		int totallen = 0;
		String devid = AppCache.getIns().terminalHB.getDeviceid();

		byte[] ids = devid.getBytes();


		sendlen = 3;
		totallen = 3+devid.length()+messageByte.length;
		byte[] bytesSend = new byte[totallen];
		bytesSend[0] = (byte)0xab;
		bytesSend[1] = (byte)0xcd;
		bytesSend[2] = (byte)(devid.length());


		System.arraycopy(ids,0,bytesSend,sendlen,devid.length());
		sendlen += devid.length();
		System.arraycopy(messageByte,0,bytesSend,sendlen,messageByte.length);
		sendlen += messageByte.length;

		DatagramPacket p = new DatagramPacket(bytesSend, sendlen, udp_svr, HK.iUDPServerPort);
        try {
        	datagramSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void send_bytes(byte[]data){
		if(data==null||data.length<=0)
		{
			return;
		}

		Log.d("UDP ", "UDP send bytes :"+ data.length + " To : " + udp_svr);
		DatagramPacket p = new DatagramPacket(data, data.length, udp_svr, HK.iUDPServerPort);

		try {
			datagramSocket.send(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    public void run() {
            StartListen();
    }


	
}
