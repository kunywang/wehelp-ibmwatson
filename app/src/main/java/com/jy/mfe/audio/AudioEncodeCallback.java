package com.jy.mfe.audio;

import android.media.MediaCodec;
import android.media.MediaFormat;

/**
 * @author kunpn
 */
public interface AudioEncodeCallback {
    void outMediaFormat(final int trackIndex, MediaFormat mediaFormat);
    void outputAudioFrame(final int trackIndex, final byte[] outBuf, final MediaCodec.BufferInfo bufferInfo);
}
