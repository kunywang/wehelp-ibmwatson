package com.jy.mfe.rtm;

import android.content.Context;
import android.util.Log;

import com.jy.mfe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMediaOperationProgress;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.RtmStatusCode;
import io.agora.rtm.SendMessageOptions;

/**
 * @author kunpn
 */
public class ChatManager {
    private static final String TAG = ChatManager.class.getSimpleName();

    private Context mContext;
    private RtmClient mRtmClient;
    private SendMessageOptions mSendMsgOptions;
    private List<RtmClientListener> mListenerList = new ArrayList<>();
    private RtmMessagePool mMessagePool = new RtmMessagePool();
    HashMap<String, RtmChannel> mRtmChannelList = new HashMap<String, RtmChannel>();
    public boolean mIsInRtmChat = false;
    public ChatManager(Context context) {
        mContext = context;
    }

    public void init() {
        String appID = mContext.getString(R.string.agora_app_id);

        try {
            mRtmClient = RtmClient.createInstance(mContext, appID, new RtmClientListener() {
                @Override
                public void onConnectionStateChanged(int state, int reason) {
                    for (RtmClientListener listener : mListenerList) {
                        listener.onConnectionStateChanged(state, reason);
                    }

                    switch (state) {
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_RECONNECTING:
                            break;
                        case RtmStatusCode.ConnectionState.CONNECTION_STATE_ABORTED:
                            mIsInRtmChat = false;
                            break;
                    }
                }

                @Override
                public void onMessageReceived(RtmMessage rtmMessage, String peerId) {
                    Log.i("RTM", "receive from " + peerId +" : " + rtmMessage.getText());
                    if (mListenerList.isEmpty()) {
                        // If currently there is no callback to handle this
                        // message, this message is unread yet. Here we also
                        // take it as an offline message.
                        mMessagePool.insertOfflineMessage(rtmMessage, peerId);
                    } else {
                        for (RtmClientListener listener : mListenerList) {
                            listener.onMessageReceived(rtmMessage, peerId);
                        }
                    }
                }

                @Override
                public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) {

                }

                @Override
                public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {

                }

                @Override
                public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }

                @Override
                public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {

                }

                @Override
                public void onTokenExpired() {

                }

                @Override
                public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

                }
            });

            //if (BuildConfig.DEBUG) {
            //    mRtmClient.setParameters("{\"rtm.log_filter\": 65535}");
            //}
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtm sdk init fatal error\n" + Log.getStackTraceString(e));
        }

        // Global option, mainly used to determine whether
        // to support offline messages now.
        mSendMsgOptions = new SendMessageOptions();
        mSendMsgOptions.enableOfflineMessaging = false;
    }

    public RtmClient getRtmClient() {
        return mRtmClient;
    }

    public void registerListener(RtmClientListener listener) {
        mListenerList.add(listener);
    }

    public void unregisterListener(RtmClientListener listener) {
        mListenerList.remove(listener);
    }

    public void enableOfflineMessage(boolean enabled) {
        mSendMsgOptions.enableOfflineMessaging = enabled;
    }

    public boolean isOfflineMessageEnabled() {
        return mSendMsgOptions.enableOfflineMessaging;
    }

    public SendMessageOptions getSendMessageOptions() {
        return mSendMsgOptions;
    }

    public List<RtmMessage> getAllOfflineMessages(String peerId) {
        return mMessagePool.getAllOfflineMessages(peerId);
    }

    public void removeAllOfflineMessages(String peerId) {
        mMessagePool.removeAllOfflineMessages(peerId);
    }

    /**
     * API CALL: login RTM server
     */
    public void loginRtmChat(String rtmID) {
        mIsInRtmChat = true;
        mRtmClient.login(null, rtmID, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void responseInfo) {
                Log.i("RTM", "login success");
                //mChatManager.createAndJoinChannel(userInfo.sBragide, new EmcChannelListener() );
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Log.i("RTM", "login failed: " + errorInfo.getErrorCode());
                mIsInRtmChat = false;

            }
        });
    }

    public void sendPeerMessage(String content, String peerID) {
        // step 1: create a message
        RtmMessage message = mRtmClient.createMessage();
        message.setText(content);

        // step 2: send message to peer
        mRtmClient.sendMessageToPeer(peerID, message, getSendMessageOptions(), new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // do nothing
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.PeerMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
            }
        });
    }

    public void createAndJoinChannel(String channelName, RtmChannelListener channelListener) {
        // step 1: create a channel instance
        String channelCode = channelName;
        if(mRtmChannelList.containsKey(channelName)){
            return;
        }
        try{
            RtmChannel mRtmChannel = mRtmClient.createChannel(channelCode, channelListener);
            if (mRtmChannel == null) {
                Log.e("RTM", "channel joun fail" + channelCode);
                return;
            }

            Log.e("RTM", "channel:" + channelCode);
            mRtmChannelList.put(channelName, mRtmChannel);
            // step 2: join the channel
            mRtmChannel.join(new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void responseInfo) {
                    Log.i("RTM", "join channel success");
                    //getChannelMemberList();
                }
                @Override
                public void onFailure(ErrorInfo errorInfo) {
                    Log.e("RTM", "join channel failed");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void leaveChannel(String channelName){
        if(!mRtmChannelList.containsKey(channelName)){
            return;
        }
        RtmChannel mRtmChannel= mRtmChannelList.get(channelName);
        mRtmChannel.leave(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("RTM", "leave channel success");

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                Log.e("RTM", "leave channel failed");
            }
        });
        mRtmChannel.release();
        mRtmChannelList.remove(channelName);
    }

    public void sendChannelMessage(String channelName, String content) {
        // step 1: create a message
        RtmMessage message = mRtmClient.createMessage();
        message.setText(content);

        if(!mRtmChannelList.containsKey(channelName)){
            return;
        }
        RtmChannel mRtmChannel= mRtmChannelList.get(channelName);
        // step 2: send message to channel
        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // refer to RtmStatusCode.ChannelMessageState for the message state
                final int errorCode = errorInfo.getErrorCode();
                Log.e("RTMS", "send message fail");
            }
        });
    }
}
