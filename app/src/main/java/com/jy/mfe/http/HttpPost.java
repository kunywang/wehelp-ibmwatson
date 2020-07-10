package com.jy.mfe.http;


import com.jy.mfe.util.EventBusUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
 * @author kunpn
 */
public class HttpPost {
    private int gid = hashCode();
    private ResultListener listener;

    public HttpPost( String sURL, String sParam, ResultListener listener) {

        this.listener = listener;
        EventBusUtil.register(this);
        EventBusUtil.sendEvent(new Req(sURL,sParam).setGroupId(gid));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEA(final Req request) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();
        final MediaType mt = MediaType.parse("application/json");
        final RequestBody body = RequestBody.create(mt, request.sParam);

        Request r = new Request.Builder().url(request.sURL).addHeader("authorization","07709708a67bf8ccd3baac5914b897ac").post(body).build();
        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                EventBusUtil.sendEvent(new RepError(request).setMsg(
                        "network_access_error"));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
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
            listener.onSuccess(event.sResult);
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

        void onSuccess(String sRest);

        /**
         * @param msg It is the error result message.
         */
        void onError(String msg);
    }

    class Req extends BaseRequest {
        private String sURL;
        private String sParam;
        Req(String url, String sparam) {
            this.sURL = url;
            this.sParam = sparam;
        }
    }

    class Rep extends BaseResponse {
        private String sResult;
        Rep(Req req, String s) throws Exception {
            super(req);
            sResult = s;
        }
    }

    class RepError extends BaseResponse {
        RepError(BaseRequest req) {
            super(req);
        }
    }
}