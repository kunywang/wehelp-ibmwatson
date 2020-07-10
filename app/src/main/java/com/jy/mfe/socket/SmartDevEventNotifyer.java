package com.jy.mfe.socket;

public interface SmartDevEventNotifyer {
	public static final int SMART_DEV_EVENT_LOGIN_SUCCESS = 0X00010001;
	public static final int SMART_DEV_EVENT_LOGIN_FAIL = 0X00010002;
	public static final int SMART_DEV_EVENT_CONNECTED = 0X00010003;
	public static final int SMART_DEV_EVENT_DISCONNECTED = 0X00010004;
	public static final int SMART_DEV_EVENT_MESSAGE = 0X00010005;
	public static final int SMART_DEV_EVENT_CONTACT = 0X00010006;
	public static final int SMART_DEV_EVENT_STATION_INFO = 0X00010007;
	public static final int SMART_DEV_EVENT_SEND_DESCRIPTOR_SUCCESS = 0X00010008;
	public static final int SMART_DEV_EVENT_SEND_DESCRIPTOR_FAIL = 0X00010009;
	public static final int SMART_DEV_EVENT_HEART_BEAT_RESPONE = 0X0001000A;
	public static final int SMART_DEV_EVENT_DEVSTATUS_ECHO = 0X0001000B;
	public static final int SMART_DEV_EVENT_ALARM_PROCESS = 0X0001000C;
	public static final int SMART_DEV_EVENT_DEVICE_UNREGISTER = 0X0001000D;
	public static final int SMART_DEV_EVENT_SERVER_XML_CMD = 0X0001000E;
	public static final int SMART_DEV_EVENT_SEND_XML_CMD = 0X0001000F;
	public static final int SMART_DEV_EVENT_QUEST = 0X00010010;	
	
	public void notifyMessage( int event,int arg0,int arg1,Object obj);
}
