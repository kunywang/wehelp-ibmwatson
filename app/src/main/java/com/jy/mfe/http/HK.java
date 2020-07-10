package com.jy.mfe.http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;

public class HK {
   // public static  String mServerIP = "118.190.158.181";
    public static  String mServerName = "www.genetek.cc";
    public static  String mServerPort = "8086";
    public static  int iUDPServerPort = 8017;
    public static InetAddress mServerIPAddress = null;

    /*
    public static  String GetTask_HOST = "http://118.190.158.181/fire/fireInspection.php";
    public static  String GetTerminalInfo_HOST = "http://118.190.158.181:8086/mdev/getdeviceinfo";
    public static  String SubmitStub_HOST = "http://118.190.158.181:8086/mdev/media/add";
     */
    public static  String GetTask_HOST = "https://www.genetek.cc/fire/fireInspection.php";
    public static  String GetTerminalInfo_HOST = "https://www.genetek.cc:8086/mdev/getdeviceinfo";
    public static  String SubmitStub_HOST = "https://www.genetek.cc:8086/mdev/media/add";
   public static  String FIRECTRI_PropertyGet = "https://www.genetek.cc:8086/mdev/property/get";
   public static  String FIRECTRI_PropertyDelete = "https://www.genetek.cc:8086/mdev/property/delete";
    public static void SetServerIPAddress(String sIP)
    {
        mServerName = sIP;
        GetTask_HOST = "https://" + mServerName+ "/fire/ResponseJson2.php";
        GetTerminalInfo_HOST = "https://" + mServerName+ ":8086/mdev/getdeviceinfo";
        SubmitStub_HOST = "https://" + mServerName+ ":8086/mdev/media/add";
     FIRECTRI_PropertyGet = "https://" + mServerName+ ":8086/mdev/property/get";
     FIRECTRI_PropertyDelete = "https://" + mServerName+ ":8086/mdev/property/delete";
    }

    public static final String MARK = "Mark";
    public static final String USER_NAME = "UserName";
    public static final String CID = "CID";
    public static final String SID = "SID";
    public static final String APKVersion = "APKVersion";

    public static final String CODE="code";
    public static final String DATA="data";

    public static final String RID = "RID";
    public static final String PTS = "PTS";
    public static final String TASK = "Task";
    public static final String PASSWORD = "Password";
    public static final String OLD_PW = "OldPW";
    public static final String NEW_PW = "NewPW";
    public static final String DEVICES = "Devices";
    public static final String DEVICE = "device";
    public static final String JYAPP = "MFS";
    public static final String TIME = "Time";
    public  static final String DEVICE_ID = "DeviceId";
    public static final String SIGNSTATE = "SIGN";
    public static final String USER_ADDR = "UserAddress";

    //http protocal cid define
    public static final String CID_HB = "2001";
    public static final String CID_Sign = "2002";
    public static final String CID_Alart = "2003";
    public static final String CID_GetBaseinfo = "2004";
    public static final String CID_GetEquipment = "2005";
    public static final String CID_GetMembers = "2006";
    public static final String CID_NotifyAck = "2007";
    public static final String CID_QuestAck = "2008";
    public static final String CID_QuestStart = "2009";
    public static final String CID_QuestEnd = "2010";
    public static final String CID_QuestSubmitImgs = "2011";
    public static final String CID_SetBaseinfo = "2012";
    public static final String CID_SetEquipment = "2013";
    public static final String CID_SetMembers = "2014";

    public static final String CID_AlartAck = "2015";
    public static final String CID_MsgResponse = "2018";
    public static final String CID_TranslateQuest = "2019";
    public static final String CID_JPUSHWithAlias = "2021";

    public static final int CIDI_GetBaseinfo = 2004;
    public static final int CIDI_SetBaseinfo = 2012;

    public static final int CIDI_GetEquipinfo = 2005;
    public static final int CIDI_SetEquipinfo = 2013;


    public static final int CIDI_GetMembers = 2006;
    public static final int CIDI_SetMembers = 2014;

    public static final int CIDI_GetVideoPath = 2015;
    public static final int CIDI_ResponseRollcall = 2020;
    public static final int CIDI_AddNewMember = 2021;
    public static final int CIDI_DeleteMember = 2022;
    public static final int CIDI_ModifyMember = 2023;
    public static final int CIDI_GetSensors = 2024;
    public static final int CIDI_ResetSmoke = 2025;
    public static final int CIDI_GetSmokeEvent = 2026;
    public static final int CIDI_GetPassageEvent = 2027;
    public static final int CIDI_ModifySmokeEvent = 2028;
    public static final int CIDI_GetStationBound = 2028;

    public static final String RID_HB = "6001";
    public static final String RID_Sign = "6002";
    public static final String RID_Alart = "6003";
    public static final String RID_GetBaseinfo = "6004";
    public static final String RID_GetEquipment = "6005";
    public static final String RID_GetMembers = "6006";
    public static final String RID_NotifyAck = "6007";
    public static final String RID_QuestAck = "6008";
    public static final String RID_QuestStart = "6009";
    public static final String RID_QuestEnd = "6010";
    public static final String RID_QuestSubmitImgs = "6011";
    public static final String RID_SetBaseinfo = "6012";
    public static final String RID_SetEquipment = "6013";
    public static final String RID_SetMembers = "6014";

    public static final String RID_AlartAck = "6015";
    public static final String RID_MsgResponse = "6018";
    public static final String RID_TranslateQuest = "6019";


    public static void putJo(JSONObject jo, String key, Object value) {
        try {
            jo.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
