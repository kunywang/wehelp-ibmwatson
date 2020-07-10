package com.jy.mfe.bean;


import com.alibaba.fastjson.annotation.JSONField;
import com.jy.mfe.GenetekApp;
import com.jy.mfe.R;

import java.io.Serializable;
import java.util.List;

/**
 * @author kunpn
 */
public class ScheduleEvent implements Serializable {

    private static final long serialVersionUID = -465693854795867935L;

    private String scheduleId;
    private int type;
    private int loopInterval;
    private int loopOffset;
    private int loopCount;
    private int timeScheduleType;
    private int timeScheduleYear;
    private int timeScheduleMonth;
    private List<Integer> timeScheduleWeek;
    private int timeScheduleDay;
    private int timeScheduleHour;
    private int timeScheduleMinute;
    private int eventType;
    private String eventData;
    private String executer;
    private String executerStation;
    private String createUser;
    private String createTime;
    private String label;

    public String getScheduleId() {
        return scheduleId;
    }
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    public int getLoopInterval() {
        return loopInterval;
    }
    public void setLoopInterval(int loopInterval) {
        this.loopInterval = loopInterval;
    }
    public int getLoopOffset() {
        return loopOffset;
    }
    public void setLoopOffset(int loopOffset) {
        this.loopOffset = loopOffset;
    }
    public int getLoopCount() {
        return loopCount;
    }
    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    public int getTimeScheduleType() {
        return timeScheduleType;
    }
    public void setTimeScheduleType(int timeScheduleType) {
        this.timeScheduleType = timeScheduleType;
    }

    public int getTimeScheduleYear() {
        return timeScheduleYear;
    }
    public void setTimeScheduleYear(int timeScheduleYear) {
        this.timeScheduleYear = timeScheduleYear;
    }

    public int getTimeScheduleMonth() {
        return timeScheduleMonth;
    }
    public void setTimeScheduleMonth(int timeScheduleMonth) {
        this.timeScheduleMonth = timeScheduleMonth;
    }

    public List<Integer> getTimeScheduleWeek() {
        return timeScheduleWeek;
    }
    public void setTimeScheduleWeek(List<Integer> timeScheduleWeek) {
        this.timeScheduleWeek = timeScheduleWeek;
    }

    public int getTimeScheduleDay() {
        return timeScheduleDay;
    }
    public void setTimeScheduleDay(int timeScheduleDay) {
        this.timeScheduleDay = timeScheduleDay;
    }
    public int getTimeScheduleHour() {
        return timeScheduleHour;
    }
    public void setTimeScheduleHour(int timeScheduleHour) {
        this.timeScheduleHour = timeScheduleHour;
    }
    public int getTimeScheduleMinute() {
        return timeScheduleMinute;
    }
    public void setTimeScheduleMinute(int timeScheduleMinute) {
        this.timeScheduleMinute = timeScheduleMinute;
    }

    public int getEventType() {
        return eventType;
    }
    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getEventData() {
        return eventData;
    }
    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public String getExecuter() {
        return executer;
    }
    public void setExecuter(String executer) {
        this.executer = executer;
    }

    public String getCreateUser() {
        return createUser;
    }
    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getCreateTime() {
        return createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getExecuterStation() {
        return executerStation;
    }
    public void setExecuterStation(String executerStation) {
        this.executerStation = executerStation;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }


    @JSONField(serialize = false)
    public void copyFrom(final ScheduleEvent se){
        this.scheduleId = se.getScheduleId();
        this.type = se.getType();
        this.loopInterval = se.getLoopInterval();
        this.loopOffset = se.getLoopOffset();
        this.loopCount = se.getLoopCount();
        this.timeScheduleType = se.getTimeScheduleType();
        this.timeScheduleYear = se.getTimeScheduleYear();
        this.timeScheduleMonth = se.getTimeScheduleMonth();
        this.timeScheduleWeek = se.getTimeScheduleWeek();
        this.timeScheduleDay = se.getTimeScheduleDay();
        this.timeScheduleHour = se.getTimeScheduleHour();
        this.timeScheduleMinute = se.getTimeScheduleMinute();
        this.eventType = se.getEventType();
        this.eventData = se.getEventData();
        this.executer = se.getExecuter();
        this.executerStation = se.getExecuterStation();
        this.createUser = se.getCreateUser();
        this.createTime = se.getCreateTime();
        this.label = se.getLabel();
    }

    @JSONField(serialize = false)
    public String getDaysOfWeek(){
        String sDays = "";
        if(timeScheduleWeek.size() <= 0) {
            return sDays;
        }
        String[] dayOfWeek= GenetekApp.ins().getResources().getStringArray(R.array.weekday);

        sDays += dayOfWeek[timeScheduleWeek.get(0)%6];
        for (int i = 1; i < this.timeScheduleWeek.size(); i++){
            sDays += (","+ dayOfWeek[timeScheduleWeek.get(i)%7]);
        }
        return sDays;
    }
}
