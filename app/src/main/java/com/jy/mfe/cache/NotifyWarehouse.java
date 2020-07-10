package com.jy.mfe.cache;

import android.content.Context;
import android.text.TextUtils;

import com.jy.mfe.bean.NotifyInfo;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class NotifyWarehouse {
    private static final String SP_NAME_NW = "NotifyWareHouse";
    private static final String SP_KEY_READED = "notify";
    private static final String SP_KEY_NEW = "newnotify";

    private static List<NotifyInfo> notifys;
    private static List<NotifyInfo> newNotifys;

    public static List<NotifyInfo> loadReaded(Context context) {
        if (notifys == null) {
            notifys = new ArrayList<>();
            try {
                final String jas = context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).
                        getString(SP_KEY_READED, null);
                JSONArray ja = new JSONArray(jas);
                for (int i = 0; i < ja.length(); i++) {
                    notifys.add(NotifyInfo.ParseFromString(ja.getString(i)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return notifys;
    }

    public static List<NotifyInfo> loadNew(Context context) {
        if (newNotifys == null) {
            newNotifys = new ArrayList<>();
            try {
                final String jas = context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).
                        getString(SP_KEY_NEW, "");
                JSONArray ja = new JSONArray(jas);
                for (int i = 0; i < ja.length(); i++) {
                    newNotifys.add(NotifyInfo.ParseFromString(ja.getString(i)));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return newNotifys;
    }

    public static boolean push(Context context, NotifyInfo info, boolean bNewFlag) {
        if (info == null || context == null){
            return false;
        }

        List<NotifyInfo> notifyInfos = null;
        if(bNewFlag == true){
            notifyInfos = loadNew(context);
        }else{
            notifyInfos = loadReaded(context);
        }

        for (NotifyInfo p : notifyInfos) {
            if (TextUtils.equals(p.number, info.number)) {
                return false;
            }
        }
        notifyInfos.add(info);
        save(context, notifyInfos, bNewFlag);
        return true;
    }

    public static boolean update(Context context, NotifyInfo info, boolean bNewFlag) {
        List<NotifyInfo> notifyInfos = null;
        if(bNewFlag == true){
            notifyInfos = loadNew(context);
        }else{
            notifyInfos = loadReaded(context);
        }
        for (NotifyInfo p : notifyInfos) {
            if (TextUtils.equals(p.number, info.number)) {
                NotifyInfo.set(p, info);
                save(context, notifyInfos,bNewFlag );
                return true;
            }
        }
        return false;
    }

    public static boolean read(Context context, NotifyInfo info) {
        List<NotifyInfo> lstNew = loadNew(context);;
        List<NotifyInfo> lstReaded = loadReaded(context);

        for (NotifyInfo p : lstNew) {
            if (TextUtils.equals(p.number, info.number)) {
                lstNew.remove(p);
                lstReaded.add(0, p);
                save(context, lstNew,true );
                save(context, lstReaded,false);
                return true;
            }
        }
        return false;
    }

    public static void clear(Context context, boolean bNewFlag) {
        if(bNewFlag == true){
            context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).edit().remove(SP_KEY_NEW).apply();
            if(newNotifys != null) {
                newNotifys.clear();
            }
        }else{
            context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).edit().remove(SP_KEY_READED).apply();
            if(notifys != null) {
                notifys.clear();
            }
        }

    }

    private static void save(Context context, List<NotifyInfo> taskInfos, boolean bNewFlag) {
        JSONArray ja = new JSONArray();
        for (NotifyInfo p1 : taskInfos) {
            ja.put(NotifyInfo.toJSON(p1));
        }
        if(bNewFlag == true){
            context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).edit().putString(SP_KEY_NEW, ja.toString()).apply();
        }else{
            context.getSharedPreferences(SP_NAME_NW, Context.MODE_PRIVATE).edit().putString(SP_KEY_READED, ja.toString()).apply();
        }

    }
}
