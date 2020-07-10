package com.jy.mfe.rtm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RtmMessageUtil {
    public static final int MAX_INPUT_NAME_LENGTH = 64;

    public static final int ACTIVITY_RESULT_CONN_ABORTED = 1;

    public static final String INTENT_EXTRA_IS_PEER_MODE = "chatMode";
    public static final String INTENT_EXTRA_USER_ID = "userId";
    public static final String INTENT_EXTRA_TARGET_NAME = "targetName";

    public static Random RANDOM = new Random();

    /*
    public static final int[] COLOR_ARRAY = new int[] {
            R.drawable.shape_circle_black,
            R.drawable.shape_circle_blue,
            R.drawable.shape_circle_pink,
            R.drawable.shape_circle_pink_dark,
            R.drawable.shape_circle_yellow,
            R.drawable.shape_circle_red
    };
    */

    private static List<RtmMessageListBean> messageListBeanList = new ArrayList<>();

    public static void addMessageListBeanList(RtmMessageListBean messageListBean) {
        messageListBeanList.add(messageListBean);
    }

    // clean up list on logout
    public static void cleanMessageListBeanList() {
        messageListBeanList.clear();
    }

    public static RtmMessageListBean getExistMessageListBean(String accountOther) {
        int ret = existMessageListBean(accountOther);
        if (ret > -1) {
            return messageListBeanList.remove(ret);
        }
        return null;
    }

    // return existing list position
    private static int existMessageListBean(String userId) {
        int size = messageListBeanList.size();
        for (int i = 0; i < size; i++) {
            if (messageListBeanList.get(i).getAccountOther().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    public static void addMessageBean(String account, String msg) {
        RtmMessageBean messageBean = new RtmMessageBean(account, msg, false);
        int ret = existMessageListBean(account);
        if (ret == -1) {
            // account not exist new messagelistbean
           // messageBean.setBackground(RtmMessageUtil.COLOR_ARRAY[RANDOM.nextInt(RtmMessageUtil.COLOR_ARRAY.length)]);
            List<RtmMessageBean> messageBeanList = new ArrayList<>();
            messageBeanList.add(messageBean);
            messageListBeanList.add(new RtmMessageListBean(account, messageBeanList));

        } else {
            // account exist get messagelistbean
            RtmMessageListBean bean = messageListBeanList.remove(ret);
            List<RtmMessageBean> messageBeanList = bean.getMessageBeanList();
            if (messageBeanList.size() > 0) {
                messageBean.setBackground(messageBeanList.get(0).getBackground());
            } else {
               // messageBean.setBackground(RtmMessageUtil.COLOR_ARRAY[RANDOM.nextInt(RtmMessageUtil.COLOR_ARRAY.length)]);
            }
            messageBeanList.add(messageBean);
            bean.setMessageBeanList(messageBeanList);
            messageListBeanList.add(bean);
        }
    }
}
