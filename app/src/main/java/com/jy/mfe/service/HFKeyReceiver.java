package com.jy.mfe.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.jy.mfe.GenetekApp;
import com.jy.mfe.R;
import com.jy.mfe.RecordController;
import com.jy.mfe.tts.TTSController;

import static com.weivoice.srv.Global.POC;

/*
电源  :                           26
  拍照  :                           27
  音量减  :                         25
  音量加  :                         24
  呼叫  :                           5
  SOS  :                           264
 */

public class HFKeyReceiver extends BroadcastReceiver {
    public static Object btnLock = new Object();
    @Override
    public void onReceive(Context context, Intent intent){

        synchronized (btnLock){
            if ("android.intent.action.ACTION_HF_KEYCODE".equals(intent.getAction())) {
                int ptt_key = intent.getIntExtra("hf_keycode", -1);
                boolean keyDown = intent.getBooleanExtra("hf_down", false);
                if (ptt_key == 5) {
                    try {
                        if(keyDown == true){
                            POC.handlePTT(true);
                        }else{
                            POC.handlePTT(false);
                        }
                    }
                    catch (Exception e) {
                    }
                }else if (ptt_key == 27){
                    if(keyDown == true){
                        RecordController rcIns = RecordController.getIns();
                        if(rcIns != null){
                            rcIns.onRecordKey();
                        }
                    }else{
                    }
                }else if (ptt_key == 264){
                    if(keyDown == true){
                        TTSController mTtsManager = TTSController.getInstance(context);
                        if(GenetekApp.ins().mRecordState == true){
                           // mTtsManager.TaskSpeak(context.getString(R.string.recordworking));
                            mTtsManager.playSound(R.raw.recordworking);
                        }else{
                            //mTtsManager.TaskSpeak(context.getString(R.string.recordstoped));
                            mTtsManager.playSound(R.raw.recordstop);
                        }
                    }else{
                    }
                }
            }else if(Intent.ACTION_MEDIA_BUTTON.equalsIgnoreCase(intent.getAction())){
                Log.d("PTT", "onReceive: " + intent.toString());
                KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if(event==null) {
                    return;
                }

                boolean isActionUp = (event.getAction()==KeyEvent.ACTION_UP);
                if(!isActionUp){
                    try {
                        POC.handlePTT(true);
                    }
                    catch (Exception e) {
                    }
                }else{
                    try {
                        POC.handlePTT(false);
                    }
                    catch (Exception e) {
                    }
                }
            }else if(Intent.ACTION_HEADSET_PLUG.equalsIgnoreCase(intent.getAction())){
                Log.d("PTT", "onReceive: " + intent.toString());
               // KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
               // if(event==null) {
               //     return;
               // }


            }
        }
    }
}
