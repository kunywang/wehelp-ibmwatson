package com.jy.mfe.VideoCapture;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;


public interface CameraSource
{
    abstract public int createCamera(Activity activity, SurfaceHolder holder);
    abstract public int destroyCamera();
    abstract public int startCamera(CameraObserver observer);
    abstract public int stopCamera();
    abstract public int autoFocus();
    abstract public boolean isCameraValid();
    abstract public int getFrameRate();
    abstract public int getWidth();
    abstract public int getHeight();
    abstract public int snapshot(Camera.PictureCallback pc);
    abstract public int resumePreview();
}
