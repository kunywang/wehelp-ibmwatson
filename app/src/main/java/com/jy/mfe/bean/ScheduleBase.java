package com.jy.mfe.bean;

/**
 * @author kunpn
 */
public class ScheduleBase {

    public static final String PropertySchedule = "schedule";

    public static final int SE_TYPE_TIMER = 1;
    public static final int SE_TYPE_LOOP = 2;

    public static final int SE_TIME_LOOPER_NON = -1;
    public static final int SE_TIME_LOOPER_HOUR = 1;
    public static final int SE_TIME_LOOPER_DAY = 2;
    public static final int SE_TIME_LOOPER_WEEK = 3;
    public static final int SE_TIME_LOOPER_MONTH = 4;

    public static final int SE_EVENT_TYPE_TTSPLAY = 1;
    public static final int SE_EVENT_TYPE_AUDIOPLAY = 2;
    public static final int SE_EVENT_TYPE_TEXT = 3;
    public static final int SE_EVENT_TYPE_ACTION = 4;
}
