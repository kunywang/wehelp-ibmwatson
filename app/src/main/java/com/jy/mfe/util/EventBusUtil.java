package com.jy.mfe.util;

import org.greenrobot.eventbus.EventBus;

/**
 * @author kunpn
 */
public class EventBusUtil {
    public static void register(Object context) {
        if (!EventBus.getDefault().isRegistered(context)) {
            EventBus.getDefault().register(context);
        }
    }

    public static void unregister(Object context) {
        if (EventBus.getDefault().isRegistered(context)) {
            EventBus.getDefault().unregister(context);
        }
    }

    public static void sendEvent(Object object) {
        EventBus.getDefault().post(object);
    }

    public static void clearStickyEvents() {
        EventBus.getDefault().removeAllStickyEvents();
    }
}
