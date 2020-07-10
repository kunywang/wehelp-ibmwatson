package com.jy.mfe;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * It is the permission center of the main activity
 *
 * @author chenjianwen
 */
public class PermissionCenter {
    private static final int MSG_GRANT = 1000;

    private static Handler handler;
    private static boolean init;
    private static boolean lock;
    private static Map<String, CharSequence> permDic = new HashMap<>();
    private static List<String> ungranted = new ArrayList<>();

    private static void init(Context context) {
        if (!init && context != null) {
            init = true;
            handler = new Handler(Looper.getMainLooper());
            permDic.put(Manifest.permission.RECORD_AUDIO, context.getText(R.string.pop_permission_record_audio));
            permDic.put(Manifest.permission.CAMERA, context.getText(R.string.pop_permission_camera));
            permDic.put(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    context.getText(R.string.pop_permission_write_exstorage));
            permDic.put(Manifest.permission.READ_PHONE_STATE, "phone");
            permDic.put(Manifest.permission.ACCESS_FINE_LOCATION, context.getText(R.string.pop_permission_location));
            permDic.put(Manifest.permission.ACCESS_COARSE_LOCATION, context.getText(R.string.pop_permission_location));
        }
    }


    public static boolean check(Activity activity) {
        if (activity == null){
            return false;
        }

        init(activity);

        // Check permissions.
        final Set<String> set = permDic.keySet();
        for (String p : set) {
            final int result = ContextCompat.checkSelfPermission(activity, p);

            if (result != PackageManager.PERMISSION_GRANTED) {
                if (!ungranted.contains(p)) {
                    ungranted.add(p);
                }
            } else {
                ungranted.remove(p);
            }
        }

        return ungranted.size() == 0;
    }

    public static CharSequence getUngrantedPermissions() {
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        for (String p : ungranted) {
            if (ssb.length() > 0) {
                ssb.append("\n");
            }
            ssb.append(permDic.get(p));
        }
        return ssb;
    }

    public static void grant(Activity activity, int requestCode) {
        if (activity == null || ungranted.size() == 0 || lock) return;

        final String[] per = new String[ungranted.size()];
        for (int i = 0; i < per.length; i++) {
            per[i] = ungranted.get(i);
        }

        lock = true;
        handler.removeMessages(MSG_GRANT);
        final Message msg = Message.obtain(handler, () -> {
            ActivityCompat.requestPermissions(activity, per, requestCode);
            lock = false;
        });
        msg.what = MSG_GRANT;
        handler.sendMessageDelayed(msg,500);

    }
}
