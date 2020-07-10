package com.jy.mfe.talk;

public interface POCBase {
    public static final String CID = "cid";

    public static final POCENV ENV = POCENV.ins();

    public static interface MsgHandler {
        public void handleMessage(int what, Object obj);
    }
}
