package com.jy.mfe.cache;

import android.content.Context;
import android.text.TextUtils;

import com.jy.mfe.bean.QuestInfo;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kunpn
 */
public class QuestWarehouse {
    private static final String SP_NAME_QW = "QuestWareHouse";
    private static final String SP_KEY_FinisedQuest = "quest";
    private static final String SP_KEY_NewQuest = "newquest";
    private static List<QuestInfo> questsFinished;
    private static List<QuestInfo> questsNew;

    public static List<QuestInfo> load(Context context) {
        if (questsFinished == null) {
            questsFinished = new ArrayList<>();
            try {
                final String jas = context.getSharedPreferences(SP_NAME_QW, Context.MODE_PRIVATE).
                        getString(SP_KEY_FinisedQuest, null);
                JSONArray ja = new JSONArray(jas);
                for (int i = 0; i < ja.length(); i++) {
                    questsFinished.add(QuestInfo.ParseFromString(ja.getString(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return questsFinished;
    }

    public static List<QuestInfo> loadNew(Context context) {
        if (questsNew == null) {
            questsNew = new ArrayList<>();
            try {
                final String jas = context.getSharedPreferences(SP_NAME_QW, Context.MODE_PRIVATE).
                        getString(SP_KEY_NewQuest, "");
                JSONArray ja = new JSONArray(jas);
                for (int i = 0; i < ja.length(); i++) {
                    questsNew.add(QuestInfo.ParseFromString(ja.getString(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return questsNew;
    }

    public static boolean push(Context context, QuestInfo info, boolean bNewFlag) {
        if (info == null || context == null){
            return false;
        }

        List<QuestInfo> Infos;
        if(bNewFlag == true){
            Infos = loadNew(context);
        }else{
            Infos = load(context);
        }
        for (QuestInfo p : Infos) {
            if (TextUtils.equals(p.sQuestID, info.sQuestID)) {
                return false;
            }
        }
        Infos.add(0, info);
        save(context, Infos, bNewFlag);
        return true;
    }

    public static boolean update(Context context, QuestInfo info, boolean bNewFlag) {
        List<QuestInfo> lst;
        if(bNewFlag == true){
            lst = loadNew(context);
        }else{
            lst = load(context);
        }
        for (QuestInfo p : lst) {
            if (TextUtils.equals(p.sQuestID, info.sQuestID)) {
                QuestInfo.set(p, info);
                save(context, lst, bNewFlag);
                return true;
            }
        }
        return false;
    }

    public static boolean move(Context context, QuestInfo info) {
        List<QuestInfo> lstNew = loadNew(context);;
        List<QuestInfo> lstReaded = load(context);

        for (QuestInfo p : lstNew) {
            if (TextUtils.equals(p.sQuestID, info.sQuestID)) {
                lstNew.remove(p);
                lstReaded.add(0, p);
                save(context, lstNew,true );
                save(context, lstReaded,false);
                return true;
            }
        }
        return false;
    }

    public static void clear(Context context) {
        context.getSharedPreferences(SP_NAME_QW, Context.MODE_PRIVATE).edit().remove(SP_KEY_FinisedQuest).apply();
        if(questsFinished != null) {
            questsFinished.clear();
        }
    }

    private static void save(Context context, List<QuestInfo> Infos, boolean bNewFlag) {
        JSONArray ja = new JSONArray();
        for (QuestInfo p1 : Infos) {
            ja.put(QuestInfo.toJSON(p1));
        }
        if(bNewFlag == true){
            context.getSharedPreferences(SP_NAME_QW, Context.MODE_PRIVATE).edit().putString(SP_KEY_NewQuest, ja.toString()).apply();
        }else{
            context.getSharedPreferences(SP_NAME_QW, Context.MODE_PRIVATE).edit().putString(SP_KEY_FinisedQuest, ja.toString()).apply();
        }

    }


    public static int CheckUnprocessed(Context context)
    {
        List<QuestInfo> lst = loadNew(context);
        return lst.size();
    }

    public static String getNewQuest(Context context){
        JSONArray ja = new JSONArray();
        List<QuestInfo> lst = loadNew(context);
        for (QuestInfo p : lst) {
            if (TextUtils.equals(p.sStatus, "0")) {
                ja.put(p.sQuestID);
            }
        }
        return ja.toString();
    }

    public static String getProcessingQuest(Context context){
        JSONArray ja = new JSONArray();
        List<QuestInfo> lst = loadNew(context);
        for (QuestInfo p : lst) {
            if (TextUtils.equals(p.sStatus, "1")) {
                ja.put(p.sQuestID);
            }
        }
        return ja.toString();
    }
}
