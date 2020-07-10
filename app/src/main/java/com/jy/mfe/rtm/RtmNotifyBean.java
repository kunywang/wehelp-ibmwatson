package com.jy.mfe.rtm;

import java.io.Serializable;
import java.util.List;

/**
 * @author kunpn
 */
public class RtmNotifyBean implements Serializable {

    private static final long serialVersionUID = -3779175665298668126L;

    public static final String SCOPE_BRAGIDE = "brigade";
    public static final String SCOPE_BOROUGH = "borough";
    public static final String SCOPE_DETACHMENT = "detachment";
    public static final String SCOPE_STATION = "station";

    public static final String RTM_TITLE_LOC = "location";
    public static final String RTM_TITLE_TerminalAlarm = "alarm.terminal";
    public static final String RTM_TITLE_TEXT = "text";
    public static final String RTM_TITLE_VIDEOCALL = "videocall";
    public static final String RTM_TITLE_VIDEOTALK = "videotalk";
    public static final String RTM_TITLE_VIDEOCLOSE = "videoclose";
    public static final String RTM_TITLE_JOINMEETING = "join";
    public static final String RTM_TITLE_LEAVEMEETING = "leave";
    public static final String RTM_TITLE_NOTIFY = "notify";
    public static final String RTM_TITLE_QUEST = "quest";
    public static final String RTM_TITLE_TTS = "tts";
    public static final String RTM_TITLE_HB = "videowatching";
    public static final String RTM_TITLE_QueryInfo = "queryinfomation";
    public static final String RTM_TITLE_AppUpdate = "appupdate";
    public static final String RTM_TITLE_TalkChannelChange = "talkchannel";
    public static final String RTM_TITLE_PasswordModify = "passwordmodify";

    public static final String RTM_TITLE_SCHEDULE_SET = "scheduleset";
    public static final String RTM_TITLE_SCHEDULE_GET = "scheduleget";
    public static final String RTM_TITLE_SCHEDULE_DELETE = "scheduledelete";
    public static final String RTM_TITLE_RC_VOLUME_INC = "rc.volume.inc";
    public static final String RTM_TITLE_RC_VOLUME_DEC = "rc.volume.dec";
    public static final String RTM_TITLE_RC_Record_Start = "rc.record.start";
    public static final String RTM_TITLE_RC_Record_Stop = "rc.record.stop";
    public static final String RTM_TITLE_RC_SNAPSHOT = "rc.snapshot";

    private String title;
    private String name;
    private String sender;
    private String department;
    private String scope;
    private List<String> receiver;
    private String video;
    private String rtm;
    private String data;
    private String userScope;
    private String userBragide;
    private String userDetachchment;
    private String userBorough;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDepartment() {
        return department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }

    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getReceiver() {
        return receiver;
    }
    public void setReceiver(List<String> receiver) {
        this.receiver = receiver;
    }

    public String getVideo() {
        return video;
    }
    public void setVideo(String video) {
        this.video = video;
    }

    public String getRtm() {
        return rtm;
    }
    public void setRtm(String rtm) {
        this.rtm = rtm;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public String getUserScope() {
        return userScope;
    }
    public void setUserScope(String userScope) {
        this.userScope = userScope;
    }
    public String getUserBragidee() {
        return userBragide;
    }
    public void setUserBragide(String userBragide) {
        this.userBragide = userBragide;
    }
    public String getUserDetachchment() {
        return userDetachchment;
    }
    public void setUserDetachchment(String userDetachchment) {
        this.userDetachchment = userDetachchment;
    }
    public String getUserBorough() {
        return userBorough;
    }
    public void setUserBorough(String userBorough) {
        this.userBorough = userBorough;
    }
}
