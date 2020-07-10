package com.jy.mfe.cache;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author kunpn
 */
public class HardwareInfo {
    private static HardwareInfo instance ;

    public static HardwareInfo getInstance(){
        if(instance==null) {
            synchronized (HardwareInfo.class) {
                if(instance==null) {
                    instance = new HardwareInfo();
                }
            }
        }
        return instance;
    }

    private HardwareInfo() {
        HWIDFromHW();
        BoardIDFromHW();
    }
    private static String m_BoardID= "";
    private static String m_hardwareID= "";

    //125.74.48.82
   // public static String mServerIP= "118.190.158.181";
    public static  String mServerIP = "118.190.86.237";
    public static String mServerPort= "8082";

    public String GetHWID(){
        if(m_hardwareID.equals("")) {
            RereadHWID();
        }

        return m_hardwareID;
    }

    public String GetBoardID(){
        if(m_BoardID.equals("")) {
            RereadBoardID();
        }

        return m_BoardID;
    }

    public void RereadHWID()
    {
        HWIDFromHW();
    }
    public void RereadBoardID()
    {
        BoardIDFromHW();
    }

    @SuppressWarnings("deprecation")
    public void HWIDFromHW() {
        new Thread() {
            @Override
            public void run() {
                try {
                    String result = "";
                    Process process = null;
                    DataOutputStream os = null;
                    DataInputStream is = null;
                    try {
                        process = Runtime.getRuntime().exec("/system/xbin/su");

                        os = new DataOutputStream(process.getOutputStream());
                        is = new DataInputStream(process.getInputStream());
                        os.writeBytes("getprop persist.sys.jy.devid" + " \n");
                        os.flush();

                        os.writeBytes(" exit \n");
                        os.flush();

                        String line = null;
                        while ((line = is.readLine()) != null) {
                            Log.d("result", line);
                            result += line;
                        }
                        process.waitFor();
                    } catch (Exception e) {
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                            process.destroy();
                        } catch (Exception e) {
                        }
                    }
                    m_hardwareID = result;
                } catch (Exception e) {
                }
            }
        }.start();
    }


    public boolean SetHWIDIntoROM(String sHWID)
    {
        if(sHWID == null) {
            return false;
        }
        boolean bRet = false;

        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("/system/xbin/su");

            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            os.writeBytes("setprop persist.sys.jy.devid " + sHWID + " \n");
            os.flush();
            os.writeBytes(" exit \n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {

            }
        }

        RereadHWID();
        return bRet;
    }

    public void BoardIDFromHW() {

        new Thread() {
            @Override
            public void run() {
                try {
                    String result = "";
                    Process process = null;
                    DataOutputStream os = null;
                    DataInputStream is = null;
                    try {
                        process = Runtime.getRuntime().exec("/system/xbin/su");

                        os = new DataOutputStream(process.getOutputStream());
                        is = new DataInputStream(process.getInputStream());
                        os.writeBytes("getprop persist.sys.jy.boardid" + " \n");
                        os.flush();

                        os.writeBytes(" exit \n");
                        os.flush();

                        String line = null;
                        while ((line = is.readLine()) != null) {
                            Log.d("result", line);
                            result += line;
                        }
                        process.waitFor();

                    } catch (Exception e) {

                        //Log.e(TAG, "Unexpected error - Here is what I know:" + e.getMessage());
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                            process.destroy();
                        } catch (Exception e) {

                        }
                    }

                    m_BoardID = result;
                } catch (Exception e) {

                }
            }
        }.start();
    }

    public boolean SetBoardIDIntoROM(String sHWID)
    {
        if(sHWID == null)
        {
            return false;
        }
        boolean bRet = false;

        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("/system/xbin/su");

            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            os.writeBytes("setprop persist.sys.jy.boardid " + sHWID + " \n");
            os.flush();
            os.writeBytes(" exit \n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {

            }
        }

        RereadBoardID();
        return bRet;
    }

    public boolean setServerInfoIntoROM(String sIPAddress, String sPort)
    {
        if(sIPAddress == null || sPort == null)
        {
            return false;
        }
        boolean bRet = false;

        Process process = null;
        DataOutputStream os = null;
        DataInputStream is = null;
        try {
            process = Runtime.getRuntime().exec("/system/xbin/su");

            os = new DataOutputStream(process.getOutputStream());
            is = new DataInputStream(process.getInputStream());
            os.writeBytes("setprop persist.sys.jy.svrip " + sIPAddress + " \n");
            os.flush();
            os.writeBytes("setprop persist.sys.jy.svrport " + sPort + " \n");
            os.flush();
            os.writeBytes(" exit \n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }

        // RereadBoardID();
        return bRet;
    }

}
