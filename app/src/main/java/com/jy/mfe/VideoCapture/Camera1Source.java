package com.jy.mfe.VideoCapture;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.jy.mfe.cache.AppCache;

import java.util.List;

/**
 * @author kunpn
 */
public class Camera1Source implements CameraSource, Camera.AutoFocusCallback
{
    private Activity m_activity = null;
    private Camera m_camera = null;
    private int m_nCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    public   int m_width = 640;
    public   int m_height = 480;
    private CameraObserver m_observer = null;

    Camera.PreviewCallback mJpegPreviewCallback = new Camera.PreviewCallback()
    {
        @Override
        public void onPreviewFrame ( byte[] data, Camera camera)
        {
            m_observer.onData(data, m_width, m_height);
            m_camera.addCallbackBuffer(data);
        }
    };

    @Override
    public void onAutoFocus(boolean success, Camera camera)
    {
        return;
    }

    private int getDgree()
    {
        int rotation = m_activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation)
        {
        case Surface.ROTATION_0:
            degrees = 0;
            break; // Natural orientation
        case Surface.ROTATION_90:
            degrees = 90;
            break; // Landscape left
        case Surface.ROTATION_180:
            degrees = 180;
            break;// Upside down
        case Surface.ROTATION_270:
            degrees = 270;
            break;// Landscape right
        }
        return degrees;
    }

    @Override
    public boolean isCameraValid()
    {
        boolean bValid = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
        }
        catch (Exception e) {
            bValid = false;
        }
        if (bValid) {
            mCamera.release();
            mCamera = null;
        }
        return bValid;
    }

    @Override
    public int autoFocus()
    {
        m_camera.autoFocus(this);
        return 0;
    }

    @Override
    public int createCamera(Activity activity, SurfaceHolder holder)
    {
        try {
            m_activity = activity;
            m_width = AppCache.getIns().softConfig.getVideoWidth();
            m_height = AppCache.getIns().softConfig.getVideoHeight();
            m_camera = Camera.open(m_nCameraID);
            List<Integer> nfots = m_camera.getParameters().getSupportedPreviewFormats();
            List<Camera.Size> vs= m_camera.getParameters().getSupportedPreviewSizes();
            Camera.Parameters parameters = m_camera.getParameters();
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(m_nCameraID, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.setPreviewSize(m_width, m_height);
            parameters.setPreviewFrameRate(24);
            //parameters.setPreviewFrameRate();
            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            m_camera.setParameters(parameters);
            //m_camera.autoFocus(null);
            //计算preview画面需要旋转的角度。目前木有做横竖屏切换的时候无缝旋转画面，后面再搞。
            int  displayRotation = (cameraRotationOffset - getDgree() + 360) % 360;
            //m_camera.setDisplayOrientation(displayRotation);
            m_camera.setPreviewDisplay(holder);

        }
        catch (Exception e) {
            return -1;
        }
        return 0;
    }

    @Override
    public int destroyCamera()
    {
        if (m_camera == null) {
            return -1;
        }
        m_camera.setPreviewCallback(null);
        m_camera.release();
        return 0;
    }

    @Override
    public int startCamera(CameraObserver observer)
    {
        if (m_camera == null) {
            return -1;
        }

        m_observer = observer;
        m_camera.startPreview();
        int previewFormat = m_camera.getParameters().getPreviewFormat();
        Camera.Size previewSize = m_camera.getParameters().getPreviewSize();
        int size = previewSize.width * previewSize.height
                * ImageFormat.getBitsPerPixel(previewFormat)
                / 8;
        m_camera.addCallbackBuffer(new byte[size]);
        m_camera.setPreviewCallbackWithBuffer(mJpegPreviewCallback);
        return 0;
    }
    @Override
    public int getFrameRate()
    {
        return 25;
    }
    @Override
    public int getWidth()
    {
        return m_width;
    }
    @Override
    public int getHeight()
    {
        return m_height;
    }
    @Override
    public int stopCamera() {
        if (m_camera == null) {
            return -1;
        }
        m_camera.stopPreview();
        return 0;
    }
    @Override
    public int snapshot(Camera.PictureCallback pc){
        if (m_camera == null) {
            return -1;
        }
        m_camera.takePicture(null, null, pc);
        return 0;
    }

    @Override
    public int resumePreview(){
        if (m_camera == null) {
            return -1;
        }
        m_camera.startPreview();
        return 0;
    }
}
