
package com.jy.mfe.socket;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.jy.mfe.cache.AppCache;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kunpn
 */
public class SmartDevProtcalUDP {
	private static SmartDevProtcalUDP instance;
	public static SmartDevProtcalUDP getInstance(){
		synchronized (SmartDevProtcalUDP.class) {  
				if(instance==null){
					instance = new SmartDevProtcalUDP();
				}
			}
		return instance;
	}
	
	public boolean mDevUnRegister = false;
	private String deviceid = "10000";
	public String GetDevID(){
		return deviceid;
	}
	//message define
	private final static int MSG_TICK = 200;	
	private final static int MSG_RECIVE_UDP = 201;
	private final static int MSG_SEND_UDP = 202;
	private final static int MSG_SEND_ALARM = 203;	
	private final static int MSG_SEND_DESCRIPTOR= 204;
	private final static int MSG_SEND_CONFIRM= 205;
	private final static int MSG_SEND_CONTACT= 206;
	private final static int MSG_GET_CONTACT= 207;
	private final static int MSG_GET_STATIONINFO= 208;
	private final static int MSG_SEND_STATIONINFO= 209;
	private final static int MSG_GET_STATIONEXT= 210;
	private final static int MSG_SEND_STATIONEXT= 211;
	private final static int MSG_GET_EQUITMENT= 212;
	private final static int MSG_SEND_EQUIPMENT= 213;
	private final static int MSG_Quest_Got= 214;
	private final static int MSG_Quest_Start= 215;
	private final static int MSG_Quest_Arrive= 216;
	private final static int MSG_Quest_INFO= 217;
	private final static int MSG_Quest_Finish= 218;
	private final static int MSG_SEND_JSON_FILE = 219;
	
	private final static int MSG_AU_QueryVer= 220;
	private final static int MSG_AU_QueryList= 221;
	private final static int MSG_AU_Download = 222;

	private final static int MSG_AU_GetCurVer= 223;

	private final static int MSG_RECIVE_UDP_BYTES = 301;
	private final static int MSG_SERVER_XML_CMD = 302;
	private final static int MSG_SERVER_EVENT = 303;
	
	private boolean isInited = false;
	private Context mServiceContext;
	private Handler mHandler = null;
	private HandlerThread mThread;
	

	private Thread mUDPThread;
	private boolean mIsWorking;

	private int mMemberCount = 0;
	private SmartDevEventNotifyer mNotify = null;

	private Handler DLHandler = null;
	private UdpClient m_udp;

	public long mLastHeartBeatTime_IDP = 0;
	public long mHeartBeatDuraionBase_IDP = 40*1000;
	public long mHeartBeatDuraion_IDP = 10*1000;
	
	public long mLastServerEventTime = 0;
	public long mServerEventTimeout = 2*60*1000;
	
	private ByteBuffer msgBuffer = ByteBuffer.allocate(1024 * 1);
	public void init_udp(WifiManager manager)
	{
		if(isInited) {
			return;
		}
		m_udp = new UdpClient(manager);
		m_udp.setListener(new UDPListenServer());
		mUDPThread = new Thread(m_udp);		
		mUDPThread.start();
	}

	public void init(Context context){
		if(isInited)
		{
			mThread = new HandlerThread("updclient");
			mThread.start();
			mHandler = new EventHandler(mThread.getLooper());
			return;
		}
		deviceid = AppCache.getIns().terminalHB.getDeviceid();
		if(deviceid==null||deviceid.length()<=0) {
			deviceid = "20000";
		}
		isInited = true;
		mServiceContext = context;

		mThread = new HandlerThread("AutoUpdate");
		mThread.start();

		mHandler = new EventHandler(mThread.getLooper());
		mHandler.sendEmptyMessageDelayed(MSG_TICK, 30000);
	}
	
	class EventHandler extends Handler{
		public EventHandler(Looper loop){
			super(loop);
		}
		@Override
			public void handleMessage(Message msg) {
	        	switch(msg.what){
	        	case MSG_TICK: {
						mHandler.sendEmptyMessageDelayed(MSG_TICK, AppCache.getIns().softConfig.getLocateInterval()*1000);
						reportVIToSvr();
		        	}
	        		break;
	        	case MSG_RECIVE_UDP: {
	        		String sUDP = (String)msg.obj;
	        		ParseUDPJson(sUDP);
	        	}break;
				case MSG_RECIVE_UDP_BYTES: {
					byte [] data = (byte[]) msg.obj;
					if(data!=null){
						//BuildFrame(data);
						List<String> stringList =  new ArrayList<>();;
						SmartDevFrameParser.BuildFrame(data, 0, data.length,stringList );
						for (String sJson:stringList) {
							ParseUDPJson(sJson);
						}
					}
				}
				break;
	        	case MSG_SEND_JSON_FILE: {
	        		String sJo = (String)msg.obj;
	        		m_udp.send_bytes(SmartDevFrameParser.PackJSONFrame(sJo, 0, AppCache.getIns().terminalHB.getDeviceid(), AppCache.getIns().m_nDeviceType));
	        	}break;
	        	}
		}
	};

	public class UDPListenServer implements UdpClientListener {
		@Override
		public void OnUDPPackageRecive(String buffer) {
			if(buffer==null||buffer.length()<=0) {
				return;
			}
			 Message msg = new Message();
			 msg.what = MSG_RECIVE_UDP;
			 msg.obj = buffer;
			 mHandler.sendMessage(msg);
		}

		@Override
		public void OnUDPPackageReciveBytes(byte []data,int len) {
			if(data==null||len<=0||data.length<len) {
				return;
			}
			byte[] temp = new byte[len];
			System.arraycopy(data, 0, temp, 0, len);
			Message msg = new Message();
			msg.what = MSG_RECIVE_UDP_BYTES;
			msg.obj = temp;
			mHandler.sendMessage(msg);
		}
	}

	private void ParseUDPJson(String sUpd)
	{
		try {
			Log.d("UDP", "ParseUDPJson: " + sUpd);
			JSONObject jsonObject =new JSONObject(sUpd);
			int  nSTC = -1;
			if(jsonObject.has("code")) {
				nSTC = jsonObject.getInt("code");
			}
			
			if(jsonObject.has("MARK")) {
				String sMark = jsonObject.getString("MARK");
				if(sMark.equalsIgnoreCase("heartbeat")) {
					if(mNotify!=null) {
						mNotify.notifyMessage(SmartDevEventNotifyer.SMART_DEV_EVENT_HEART_BEAT_RESPONE, 0, 0, null);
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void SendJsonString(String json){
		Message msg = new Message();
		 msg.what = MSG_SEND_JSON_FILE;
		 msg.obj = json;
		 mHandler.sendMessage(msg);
		return ;
	}

	private void reportVIToSvr()
	{
		String js = JSON.toJSONString(AppCache.getIns().terminalHB);
		SendJsonString(js);
	}

	public  String getWorkstate()
	{
		String sWorkstate = "standby";

		return sWorkstate;
	}

}
