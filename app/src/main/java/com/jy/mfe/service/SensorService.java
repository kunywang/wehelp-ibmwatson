package com.jy.mfe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.android.serial.SerialLib;
import com.android.serial.SerialLib.OnPortDataListener;
import com.jy.mfe.bean.SensorInfo;
import com.jy.mfe.cache.AppCache;

public class SensorService extends Service implements OnPortDataListener{
    public static final String TAG = SensorService.class.getSimpleName();
    private final SerialLib mSerial = new SerialLib();
    private static final int DEFAULT_BUADRATE = 9600;
    private static final String DEFAULT_DEVICE = "/dev/ttyS1";

    public static SensorInfo sensorData = new SensorInfo();
    public static void startSensorService(Context context){
        Intent serviceIntent = new Intent(context, SensorService.class);
        context.startService(serviceIntent);
    }


    public static void stopSensorService(Context context){
        Intent serviceIntent = new Intent(context, SensorService.class);
        context.stopService(serviceIntent);
    }

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        InitSeriPort();
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean InitSeriPort()
    {
        try
        {
            mSerial.setPortDataListener(this);
            boolean bret =OpenPort();
            return bret;
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            return false;
        }

    }

    public boolean OpenPort()
    {
        //  open port
        try {
            //initPortModeCtrl();
            mSerial.openPort(DEFAULT_DEVICE, DEFAULT_BUADRATE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // make sure
        if (mSerial.isOpened()) {
            return true;
        } else {
        }
        return false;
    }

    String final_data = null;

    private byte[] tmpBuf = new byte[1024];
    private byte[] RecBuff = new byte[1024];
    private int m_nSeriRecCount = 0;
    private int m_nCmdLength = 0;
    private char m_nCmdHead = 0x63;
    private char m_cCmdEnd1 = 0xa8;

    @Override
    public void onReceived(byte[] buffer, int size) {
        for(int iChar = 0; iChar < size; iChar++)
        {
            if(buffer[iChar] == (byte)m_nCmdHead)//new cmd
            {
                if(m_nSeriRecCount == 0)
                {
                    tmpBuf[m_nSeriRecCount] = buffer[iChar];
                    m_nSeriRecCount ++;
                }else
                {
                    m_nSeriRecCount = 0;
                    tmpBuf[m_nSeriRecCount] = buffer[iChar];
                    m_nSeriRecCount ++;
                }
            }else if(buffer[iChar] == (byte)m_cCmdEnd1){
                tmpBuf[m_nSeriRecCount] = buffer[iChar];
                m_nSeriRecCount ++;
                System.arraycopy(tmpBuf, 0, RecBuff, 0, m_nSeriRecCount);
                m_nCmdLength = m_nSeriRecCount;
                ParseSeriCmd(RecBuff, m_nCmdLength);
            }
            else
            {
                if(m_nSeriRecCount != 0)
                {
                    tmpBuf[m_nSeriRecCount] = buffer[iChar];
                    m_nSeriRecCount ++;
                    if(m_nSeriRecCount > 256){
                        m_nSeriRecCount = 0;
                    }
                }
            }
        }


    }

    private boolean SendData(byte buf[], int mDataSize)
    {
        try {
            if (mSerial.write(buf, mDataSize)) {
                return true;
            }else{
                return false;
            }

        }  catch (Exception e) {
        }

        return false;
    }

    private int ParseSeriCmd(byte buf[], int nBufLen)
    {
        try {
            if(nBufLen != 7){
                return  0;
            }

            int nwater = buf[1];
            int ntemperature = buf[2];
            int npower = buf[3];
            int nchemical = buf[4];

            sensorData.setWater(nwater);
            sensorData.setTemperature(ntemperature);
            sensorData.setPower(npower);
            sensorData.setChemical(nchemical);

            if(AppCache.getIns().terminalHB != null){
                AppCache.getIns().terminalHB.setPower((double) npower);
                AppCache.getIns().terminalHB.setArg0(nwater);
                AppCache.getIns().terminalHB.setArg1(nchemical);
                AppCache.getIns().terminalHB.setArg2(ntemperature);
            }

        }  catch (Exception e) {
            int aa= 0;
        }

        return 0;
    }
}
