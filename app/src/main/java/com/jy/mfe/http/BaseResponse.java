package com.jy.mfe.http;

/**
 * It is the base response.
 *
 * @author CHEN JIAN WEN
 */
public class BaseResponse extends BaseEvent {

    public BaseRequest request;
    public String msg;

    public BaseResponse(BaseRequest request) {
        this.request = request;
        setGroupId(request.getGroupId());
    }

    public BaseRequest getRequest() {
        return request;
    }

    public BaseResponse setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public BaseResponse setMsgCode(int nCode) {
        if (nCode == 0) {
            msg = "请求成功";
        }
        else if(nCode == -1){
            msg = "请求失败";
        }
        else if(nCode == -2){
            msg = "网络异常，请稍后重试";
        }
        else if(nCode == -3){
            msg = "错误的请求命令";
        }
        else if(nCode == -4){
            msg = "信息不存在";
        }
        else if(nCode == -5){
            msg = "错误的请求";
        }
        else if(nCode == -6){
            msg = "请求异常";
        }
        else if(nCode == -7){
            msg = "信息已存在";
        }else {
            msg = "设备已绑定";
        }
        return this;
    }
}
