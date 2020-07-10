package com.jy.mfe.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author kunpn
 */
public class QuestInfo {
    public String sQuestID = "";
    public String sTitle = "";
    public String sContent = "";
    public String sTime = "";
    public String sPTS = "";
    public String sSender = "";
    public String sStatus = "";
    public String sType = "";
    public String sAddress = "";
    public double longitude;
    public double latitude;

    public static  QuestInfo ParseFromString(String s)
    {
        QuestInfo info = new QuestInfo();
        if (s == null){
            return info;
        }

        try {
            JSONObject jo = new JSONObject(s);
            info.sQuestID = jo.optString("number");
            info.sTitle = jo.optString("title");
            info.sContent = jo.optString("content");
            info.sTime = jo.optString("time");
            info.sPTS = jo.optString("pts");
            info.sSender = jo.optString("sender");
            info.sType = jo.optString("type");
            info.sAddress = jo.optString("addr");
            info.sStatus  = jo.optString("status");
            String sLon = jo.optString("longitude");
            String sLat = jo.optString("latitude");
            if(sLon != null && sLon.length() > 0){
                info.longitude = Double.parseDouble(sLon);
            }
            if(sLat != null&& sLat.length() > 0){
                info.latitude= Double.parseDouble(sLat);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return info;
    }
    public static void putJo(JSONObject jo, String key, Object value) {
        try {
            jo.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject toJSON(QuestInfo info) {
        JSONObject jo = new JSONObject();
        if (info != null) {
            putJo(jo, "number", info.sQuestID);
            putJo(jo, "title", info.sTitle);
            putJo(jo, "content", info.sContent);
            putJo(jo, "time", info.sTime);
            putJo(jo, "pts", info.sPTS);
            putJo(jo, "sender", info.sSender);
            putJo(jo, "status", info.sStatus);
            putJo(jo, "type", info.sType);
            putJo(jo, "addr", info.sAddress);
            putJo(jo, "longitude", String.valueOf(info.longitude));
            putJo(jo, "latitude", String.valueOf(info.latitude));
        }
        return jo;
    }

    public static QuestInfo set(QuestInfo dst, QuestInfo src) {
        if (dst == null || src == null){
            return src;
        }

        dst.sQuestID = src.sQuestID;
        dst.sTitle = src.sTitle;
        dst.sContent = src.sContent;
        dst.sTime = src.sTime;
        dst.sPTS = src.sPTS;
        dst.sSender = src.sSender;
        dst.sStatus = src.sStatus;
        dst.sAddress = src.sAddress;
        dst.sType = src.sType;
        dst.longitude = src.longitude;
        dst.latitude = src.latitude;
        return dst;
    }
}
