package com.jy.mfe.http;

/**
 * It is the base event.
 */
public class BaseEvent {
    private int groupId;

    public BaseEvent setGroupId(int id) {
        groupId = id;
        return this;
    }

    public int getGroupId() {
        return groupId;
    }
}
