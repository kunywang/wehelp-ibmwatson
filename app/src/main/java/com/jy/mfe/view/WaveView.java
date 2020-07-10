package com.jy.mfe.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * It is a wave view to view pcm data.
 */
public class WaveView extends View {

    private Paint mainPaint;
    private int fillIndex;
    private int channel;
    private int sampleRate;
    private int bitDepth;
    private int bitMax, max;
    private byte[] editBuffer;
    private float[][] buffer1s;
    private float channelHeight;
    private float dealtWidth;

    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public WaveView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * <p>
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     * @see #WaveView(Context, AttributeSet, int)
     */
    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute. This constructor of View allows subclasses to use their
     * own base style when they are inflating. For example, a Button class's
     * constructor would call this version of the super class constructor and
     * supply <code>R.attr.buttonStyle</code> for <var>defStyleAttr</var>; this
     * allows the theme's button style to modify all of the base view attributes
     * (in particular its background) as well as the Button class's attributes.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     */
    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute or style resource. This constructor of View allows
     * subclasses to use their own base style when they are inflating.
     * <p>
     * When determining the final value of a particular attribute, there are
     * four inputs that come into play:
     * <ol>
     * <li>Any attribute values in the given AttributeSet.
     * <li>The style resource specified in the AttributeSet (named "style").
     * <li>The default style specified by <var>defStyleAttr</var>.
     * <li>The default style specified by <var>defStyleRes</var>.
     * <li>The base values in this theme.
     * </ol>
     * <p>
     * Each of these inputs is considered in-order, with the first listed taking
     * precedence over the following ones. In other words, if in the
     * AttributeSet you have supplied <code>&lt;Button * textColor="#ff000000"&gt;</code>
     * , then the button's text will <em>always</em> be black, regardless of
     * what is specified in any of the styles.
     *
     * @param context      The Context the view is running in, through which it can
     *                     access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag that is inflating the view.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param defStyleRes  A resource identifier of a style resource that
     *                     supplies default values for the view, used only if
     *                     defStyleAttr is 0 or can not be found in the theme. Can be 0
     *                     to not look for defaults.
     */
    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            float h = bottom - top;
            dealtWidth = (right - left) / (float) (sampleRate - 1);
            channelHeight = h / (float) channel;
            if (isInEditMode()) {
                fill(editBuffer);
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float dy;

        for (int i = 0; i < channel; i++) {
            dy = channelHeight * (i + 0.5f);
            for (int j = 0; j < sampleRate; j++) {
                if (j > 0) {
                    canvas.drawLine((j - 1) * dealtWidth,
                            buffer1s[i][j - 1] + dy,
                            j * dealtWidth,
                            buffer1s[i][j] + dy, mainPaint);
                }
            }


        }
    }

    private void init() {
        if (isInEditMode()) {
            setup(1, 15, 16);
        }
    }

    /**
     * If fill buffer into the display cache.
     *
     * @param buffer It is the pcm buffer to input.
     */
    public void fill(byte[] buffer) {
        final int byteNum = bitDepth / 8;
        int t, t1, index = 0, mi;
        mi = buffer1s[0].length;
        for (int i = 0; i < buffer.length; ) {
            for (int c = 0; c < channel; c++) {

                if (fillIndex + index >= mi) {
                    fillIndex = 0;
                    index = 0;
                }

                t = 0;
                for (int j = 0; j < byteNum; j++) {
                    try {
                        t1 = (0xff & buffer[i + j + c * byteNum]);
                    } catch (Exception e) {
                        // Prevent the data is not correct, fill 0.
                        t1 = 0;
                    }
                    t |= t1 << (j * 8);
                }
                if (t >= bitMax) t = t - max;


                buffer1s[c][fillIndex + index] = t / (float) bitMax * (channelHeight * 0.9f / 2);
            }
            index ++;
            i = i + channel * byteNum;
        }
        fillIndex = fillIndex + index;
        if (fillIndex >= mi) {
            fillIndex = 0;
        }

    }

    /**
     * It initializes the parameters of wave cache
     *
     * @param channel    It is the chanel number support.
     * @param sampleRate It is the sample rate.
     * @param bitDepth   It is the bit depth of each sample.
     */
    public void setup(int channel, int sampleRate, int bitDepth) {
        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(0xFF00FF00);
        mainPaint.setStrokeWidth(1);
        mainPaint.setStyle(Paint.Style.STROKE);

        int byteNum = Math.max(2, bitDepth / 8);
        this.bitDepth = byteNum * 8;
        bitMax = (int) Math.pow(2, this.bitDepth - 1);
        max = (int) Math.pow(2, this.bitDepth);
        buffer1s = new float[channel][sampleRate];
        if (channel <= 0) channel = 1;
        else if (channel > 7) channel = 7;
        this.channel = channel;
        this.sampleRate = sampleRate;


        editBuffer = new byte[]{ // 32 * 2
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, (byte)0x80, (byte)0xff, 0x7f,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00,
        };

    }
}
