package com.jy.mfe.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author kunpn
 */
public class NotifyInfo {
    public String number = "";
    public String title = "";
    public String content = "";
    public String time = "";
    public String pts = "";
    public String sender = "";
    public String status = "";

    public static  NotifyInfo ParseFromString(String s)
    {
        NotifyInfo info = new NotifyInfo();
        if (s == null) return info;

        try {
            JSONObject jo = new JSONObject(s);
            info.number = jo.optString("number");
            info.title = jo.optString("title");
            info.content = jo.optString("content");
            info.time = jo.optString("time");
            info.pts = jo.optString("pts");
            info.sender = jo.optString("sender");

            info.status  = jo.optString("status");
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

    public static JSONObject toJSON(NotifyInfo info) {
        JSONObject jo = new JSONObject();
        if (info != null) {
            putJo(jo, "number", info.number);
            putJo(jo, "title", info.title);
            putJo(jo, "content", info.content);
            putJo(jo, "time", info.time);
            putJo(jo, "pts", info.pts);
            putJo(jo, "sender", info.sender);
            putJo(jo, "status", info.status);
        }
        return jo;
    }

    public static NotifyInfo set(NotifyInfo dst, NotifyInfo src) {
        if (dst == null || src == null) return src;

        dst.number = src.number;
        dst.title = src.title;
        dst.content = src.content;
        dst.time = src.time;
        dst.pts = src.pts;
        dst.sender = src.sender;
        dst.status = src.status;
        return dst;
    }
}
