package com.jy.mfe.rtm;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtm.RtmMessage;

/**
 * @author kunpng
 */
public class RtmMessageListBean {
    private String accountOther;
    private List<RtmMessageBean> messageBeanList;

    public RtmMessageListBean(String account, List<RtmMessageBean> messageBeanList) {
        this.accountOther = account;
        this.messageBeanList = messageBeanList;
    }

    /**
     * Create message list bean from offline messages
     * @param account peer user id to find offline messages from
     * @param chatManager chat manager that managers offline message pool
     */
    public RtmMessageListBean(String account, ChatManager chatManager) {
        accountOther = account;
        messageBeanList = new ArrayList<>();

        List<RtmMessage> messageList = chatManager.getAllOfflineMessages(account);
        for (RtmMessage m : messageList) {
            // All offline messages are from peer users
            RtmMessageBean bean = new RtmMessageBean(account, m.getText(), false);
            messageBeanList.add(bean);
        }
    }

    public String getAccountOther() {
        return accountOther;
    }

    public void setAccountOther(String accountOther) {
        this.accountOther = accountOther;
    }

    public List<RtmMessageBean> getMessageBeanList() {
        return messageBeanList;
    }

    public void setMessageBeanList(List<RtmMessageBean> messageBeanList) {
        this.messageBeanList = messageBeanList;
    }
}
