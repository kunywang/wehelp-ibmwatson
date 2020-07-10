package com.jy.mfe.crash;

import android.content.Context;

/**
 * @author anlonglong on 2018/9/26.
 * Email： 940752944@qq.com
 */
public class AppCrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;
    private  Thread.UncaughtExceptionHandler mDefaultHandler;


    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        mDefaultHandler.uncaughtException(t, e);
    }

    private static class CrashHolder {
         private  AppCrashHandler sAppCrashHandler;

         static AppCrashHandler getAppCrashHandler() {
             return new AppCrashHandler();
         }
    }

    public static AppCrashHandler getInstance() {
        return CrashHolder.getAppCrashHandler();
    }
}
