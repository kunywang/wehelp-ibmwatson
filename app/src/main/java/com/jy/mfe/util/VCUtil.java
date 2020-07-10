package com.jy.mfe.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * It is contains view method for view components.
 *
 * @author CHEN JIAN WEN
 */
public class VCUtil {

    private static float densityDpi = 0;
    private static float scaledDensity = 0;

    /**
     * It enables / disables a view.
     *
     * @param view It is the view to enable or disable.
     * @param en   It is the enable switch in positive logic.
     * @return
     */
    public static boolean enableView(View view, boolean en) {
        if (view != null) {
            if (en) {
                if (!view.isEnabled()) {
                    view.setEnabled(en);
                    return true;
                }
            } else {
                if (view.isEnabled()) {
                    view.setEnabled(en);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * It shows or hides a view
     *
     * @param view It is the view to show or hide.
     * @param show If it is true, the view shows. If it is false , the view hides.
     * @return It returns true if the visibility is changed.
     */
    public static boolean showView(View view, boolean show) {
        if (view != null) {
            if (show) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                    return true;
                }
            } else {
                if (view.getVisibility() != View.GONE) {
                    view.setVisibility(View.GONE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * It shows or hides a view
     *
     * @param view It is the view to show or hide.
     * @param show If it is true, the view shows. If it is false , the view hides.
     */
    public static boolean showViewLite(View view, boolean show) {
        if (view != null) {
            if (show) {
                if (view.getVisibility() != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                    return true;
                }
            } else {
                if (view.getVisibility() != View.INVISIBLE) {
                    view.setVisibility(View.INVISIBLE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * It make a view selected or unselected.
     *
     * @param view   It is the view to select.
     * @param select When true it set the selected state of the view. And when false it clear the
     *               selected state of the view.
     */
    public static void selectView(View view, boolean select) {
        if (view != null) {
            if (select != view.isSelected()) {
                view.setSelected(select);
            }
        }
    }


    /**
     * It makes a check on view if it is visible.
     *
     * @param view It is the view to check.
     * @return It returns true if the specified view is visible. It returns false otherwise.
     */
    public static boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * It is the compatible api for setting background drawable.
     *
     * @param view     It is the view to set the drawable.
     * @param drawable It is the drawable to set.
     */
    public static void setBackground(View view, Drawable drawable) {
        if (view == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    /**
     * It is the compatible api for setting transaction z drawable.
     *
     * @param view It is the view to set the drawable.
     * @param z    It is the z position to set.
     */
    public static void setTranslationZ(View view, float z) {
        if (view == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setTranslationZ(z);
        }
    }

    public static void setOnClickListener(View view, View.OnClickListener listener) {
        if (view == null) return;
        view.setOnClickListener(listener);
        if (listener == null) {
            view.setClickable(false);
        }
    }

    public static void setOnLongClickListener(View view, View.OnLongClickListener listener) {
        if (view == null) return;
        view.setOnLongClickListener(listener);
        if (listener == null) {
            view.setClickable(false);
        }
    }

    public static void setHint(TextView view, int text) {
        if (view == null) return;
        view.setHint(text);
    }

    /**
     * It finds a view with specified id.
     *
     * @param view It is the parent view to start finding.
     * @param id   It is the id of the view to find.
     * @return It returns a view if the specified view is found. And it returns null otherwise.
     */
    public static <T extends View> T findView(View view, int id) {
        if (view == null) return null;
        return (T) view.findViewById(id);
    }

    /**
     * It sets the image drawable to a image.
     *
     * @param view     It is the image view to set.
     * @param drawable It is the drawable to set with.
     */
    public static void setImage(ImageView view, Drawable drawable) {
        if (view != null) {
            view.setImageDrawable(drawable);
        }
    }

    /**
     * It sets the image drawable to a image.
     *
     * @param view  It is the image view to set.
     * @param resId It is the image resource id to set with.
     */
    public static void setImage(ImageView view, int resId) {
        if (view != null) {
            view.setImageResource(resId);
        }
    }

    /**
     * It checks if a view is selected.
     *
     * @param view It is the given view to check.
     * @return It returns true if the given view is selected. It returns false otherwise.
     */
    public static boolean isSelected(View view) {
        if (view == null) return false;
        return view.isSelected();
    }

    /**
     * It checks if the given flags are set.
     *
     * @param value It is the value to check.
     * @param flags It is the flags to check.
     * @return It returns true if the given flags are set. Otherwise, it returns false.
     */
    public static boolean cfs(int value, int flags) {
        return (value & flags) == flags;
    }


    private static float getDensityDpi(Context context) {
        if (densityDpi <= 0) {
            densityDpi = context.getResources().getDisplayMetrics().densityDpi;
        }
        return densityDpi;
    }

    private static float getScaledDensity(Context context) {
        if (scaledDensity <= 0) {
            scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        }
        return scaledDensity;
    }

    public static float dp2px(Context context, float value) {
        return value * (getDensityDpi(context) / 160f);
    }

    public static float sp2px(Context context, float sp) {
        final float scale = getScaledDensity(context);
        return sp * scale;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * It opens the camera to take photo.
     *
     * @param activity It is the activity who call the camera application.
     * @param rc It is the request code.
     * @param filePath It is the photo path to save.
     **/
    public static void openCamera(Activity activity, int rc, String filePath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getUriForFile(activity, new File(filePath));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        activity.startActivityForResult(intent, rc);
    }


    public static Uri getUriForFile(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "alydev.FileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    public static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String getFileTime() {
        return new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
    }
    public static String getDate() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    public static String getPTS() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static void playRingtone(Context context){
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();
    }


}

