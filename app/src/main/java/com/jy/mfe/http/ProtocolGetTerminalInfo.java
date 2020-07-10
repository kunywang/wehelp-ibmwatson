package com.jy.mfe.http;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.jy.mfe.bean.TerminalInfo;
import com.jy.mfe.util.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * It is the get task protocol.
 *
 * @author kuny
 */
public class ProtocolGetTerminalInfo {
    private int gid = hashCode();
    private Context context;
    private ResultListener listener;

    public ProtocolGetTerminalInfo(Context context, String sid, ResultListener listener) {
        this.context = context;
        this.listener = listener;
        EventBusUtil.register(this);
        EventBusUtil.sendEvent(new Req(sid).setGroupId(gid));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEA(final Req request) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();

        final JSONObject jo = new JSONObject();
        HK.putJo(jo, "request", "getdevinfo");
        HK.putJo(jo, "deviceid", request.sid);

        final MediaType mt = MediaType.parse("application/json");

        final RequestBody body = RequestBody.create(mt, jo.toString());

        Request r = new Request.Builder().url(HK.GetTerminalInfo_HOST).post(body).build();
        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                EventBusUtil.sendEvent(new RepError(request).setMsg(
                        "network_access_error"));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    // Get devices information here.
                    // String sRep = response.body().string();
                    Rep rep = new Rep(request, response.body().string());

                    EventBusUtil.sendEvent(rep);
                } catch (Exception e) {
                    EventBusUtil.sendEvent(new RepError(request).setMsg(
                            "unknown_error"));
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEMT(Rep event) {
        if (event.getGroupId() != gid) {
            return;
        }

        EventBusUtil.unregister(this);
        if (listener != null) {
            listener.onSuccess(event.ti);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEMT(RepError event) {
        if (event.getGroupId() != gid){
            return;
        }

        EventBusUtil.unregister(this);

        if (listener != null) {
            listener.onError(event.msg);
        }

    }

    public interface ResultListener {
        /**
         * @param ti It is the task information.
         */
        void onSuccess(TerminalInfo ti);

        /**
         * @param msg It is the error result message.
         */
        void onError(String msg);
    }

    class Req extends BaseRequest {
        private String sid;

        Req(String sid) {
            this.sid = sid;
        }
    }

    class Rep extends BaseResponse {

        private TerminalInfo ti;

        Rep(Req req, String s) throws Exception {
            super(req);
            JSONObject jo = new JSONObject(s);
            int code = jo.optInt("code");
            String sReq = jo.optString("request");
            if (code != 0) {
                throw new IllegalProtocolException(sReq);
            }else {
                String data = jo.optString("data");
                ti = JSON.parseObject(data, TerminalInfo.class);
            }

        }
    }

    class RepError extends BaseResponse {
        RepError(BaseRequest req) {
            super(req);
        }
    }
}
