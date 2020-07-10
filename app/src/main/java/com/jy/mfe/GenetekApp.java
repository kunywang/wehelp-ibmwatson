package com.jy.mfe;

import androidx.multidex.MultiDexApplication;

import com.jy.mfe.cache.AppCache;
import com.jy.mfe.rtm.ChatManager;
import com.jy.mfe.talk.POCENV;

/**
 * @author kunpn
 */
public class GenetekApp extends MultiDexApplication {
    static GenetekApp ins;
    private ChatManager mChatManager;
    public boolean mRecordState = false;
    public static GenetekApp ins() {
        return ins;
    }
    @Override
    public void onCreate() {
        ins = this;super.onCreate();
        //HK.SetServerIPAddress("118.190.158.181", "8082");
        //HK.SetServerIPAddress("118.190.86.237", "8082");
        POCENV.ins().init(this);
        mChatManager = new ChatManager(this);
        mChatManager.init();
        AppCache.getIns().getDeviceId(this);
        AppCache.getIns().getDeviceConfig(this);
    }

    public ChatManager getChatManager() {
        return mChatManager;
    }
}
