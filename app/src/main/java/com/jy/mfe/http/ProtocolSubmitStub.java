package com.jy.mfe.http;

import android.content.Context;

import com.jy.mfe.bean.StubFile;
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
 * @author kunpn
 */
public class ProtocolSubmitStub {
    private int gid = hashCode();
    private Context context;
    private ResultListener listener;

    public ProtocolSubmitStub(Context context, StubFile sf, ResultListener listener) {
        this.context = context;
        this.listener = listener;
        EventBusUtil.register(this);
        EventBusUtil.sendEvent(new Req(sf).setGroupId(gid));
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEA(final Req request) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).build();

        String strStub = com.alibaba.fastjson.JSONObject.toJSONString(request.stub);

        final MediaType mt = MediaType.parse("application/json");

        final RequestBody body = RequestBody.create(mt, strStub);

        Request r = new Request.Builder().url(HK.SubmitStub_HOST).post(body).build();
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
            listener.onSuccess();
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

        void onSuccess();

        /**
         * @param msg It is the error result message.
         */
        void onError(String msg);
    }

    class Req extends BaseRequest {
        private StubFile stub;

        Req(StubFile sf) {
            this.stub = sf;
        }
    }

    class Rep extends BaseResponse {

        Rep(Req req, String s) throws Exception {
            super(req);
            JSONObject jo = new JSONObject(s);
            int code = jo.optInt("code");
            String sReq = jo.optString("request");
            if (code != 0) {
                throw new IllegalProtocolException(sReq);
            }else {

            }

        }
    }

    class RepError extends BaseResponse {
        RepError(BaseRequest req) {
            super(req);
        }
    }
}
