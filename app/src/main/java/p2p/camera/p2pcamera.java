package p2p.camera;


public class p2pcamera
{
    static public final int P2P_AUDIOTYPE_PCM = 0;
    static public final int P2P_AUDIOTYPE_G711 = 1;
    static public final int P2P_AUDIOTYPE_AAC = 2;

    static public p2pcamera.P2PObserver m_observer = null;
    static public p2pcamera m_p2pcamera = new p2pcamera();
    static private int m_bLoaded = 0;
    static
    {
        try
        {
            System.loadLibrary("p2pcamera");
            m_bLoaded = 1;
        }
        catch(UnsatisfiedLinkError e)
        {
            e.printStackTrace(System.out);
            m_bLoaded = 0;
        }
    }

    static public String m_szLocalIP = "";
    static public String m_szP2PServer = "";
    static public String m_szDeviceID = "";
    static public int m_nAudioType = 0;
    static public int m_nSampleRate = 8000;
    static public int m_nBitRate = 1024 * 2;
    static public int m_nLiveCount = 0;
    static public int m_nSpeakCount = 0;

    public interface P2PObserver
    {
        public abstract int OnSpeakData(int nSessionID, byte nAudioType, byte nSampleRate, byte nChannels, byte[] data, int offset, int len);
        public abstract int OnCmdResponse(byte[] data, int len);
    }

    public int OnSvrSessionData(int nSessionID, byte[] data, int len)
    {
        if(nSessionID != 2)
        {
            return 0;
        }
        return m_observer.OnSpeakData(2, (byte)P2PSvrGetSpeakType(), (byte)0, (byte)0, data, 20, len - 20);
    }

    public int OnSvrSessionStart(int nSessionID)
    {
        switch (nSessionID)
        {
        case 1:
            m_nLiveCount++;
            break;
        case 2:
            m_nSpeakCount++;
            break;
        }
        return 0;
    }

    public int OnSvrSessionStop(int nSessionID)
    {
        switch (nSessionID)
        {
            case 1:
                m_nLiveCount--;
                break;
            case 2:

                m_nSpeakCount--;
                break;
        }
        return 0;
    }

    int GetEncodeType(byte nEncodeType)
    {
        switch (nEncodeType)
        {
            case 0:
                return p2pcamera.P2P_AUDIOTYPE_PCM;
            case 2:
                return p2pcamera.P2P_AUDIOTYPE_G711;
            case 3:
                return p2pcamera.P2P_AUDIOTYPE_AAC;
        }
        return -1;
    }

    public int OnClientSessionData(int nSessionID, byte[] data, int len)
    {
        if(m_nSpeakCount <= 0 && nSessionID == 4)
        {

        }
        if(nSessionID == 0)
        {
            return m_observer.OnCmdResponse(data, len);
        }
        return 0;
    }

    public int OnClientSessionOpen(int nSessionID)
    {

        return  0;
    }

    public int OnClientSessionClose(int nSessionID)
    {

        return  0;
    }

    static public int P2PInit(String strDeviceID, p2pcamera.P2PObserver observer)
    {
        m_observer = observer;
        P2PInitial(strDeviceID, m_p2pcamera);
        P2PSvrStart();
        P2PCliStart();
        return 0;
    }

    static public int P2PDeInit()
    {
        P2PCliStop();
        P2PSvrStop();
        P2PClean();
        return 0;
    }

    static public boolean IsPreviewing()
    {
        return m_nLiveCount > 0;
    }
    static public boolean IsSpeaking()
    {
        return m_nSpeakCount > 0;
    }

    static public native int P2PInitial(String strDeviceID, p2pcamera p2pcamera);
    static public native int P2PClean();
    static public native int P2PSetDeviceID(String strDeviceID);

    static public native int P2PCliStart();
    static public native int P2PCliStop();
    static public native int P2PCliStartTalk(String strRelayID, String strRoomID, int family, String strServerIP, int nServerPort, int nAudioType);
    static public native int P2PCliStopTalk();
    static public native int P2PCliStartSpeak(int level);
    static public native int P2PCliStopSpeak();
    static public native int P2PCliSetLocalAddr(String strIP, int nPort);
    static public native int P2PCliSendAudio(byte[] data, int offset, int length, int type, int nSampleRate, int nChannels);

    static public native int P2PSvrStart();
    static public native int P2PSvrStop();
    static public native int P2PSvrSetLocalAddr(String strIP, int nPort);
    static public native int P2PSvrSetServerAddr(int nIndex, String strServerAddr);
    static public native int P2PSvrGetSpeakType();
    static public native int P2PSvrSetSpeakType(int nSpeakAudioType);
    static public native int P2PSvrSendVideo(byte[] data, int offset, int length, int width, int height);
    static public native int P2PSvrSendAudio(byte[] data, int offset, int length, int type, int nSampleRate, int nChannels);
    static public native int P2PSvrIsServerOnline(int nIndex);
    static public native int P2PSvrGetLostCount();
    static public native int P2PSvrGetFrameRate();
    static public native int P2PSvrGetBitRate();
    static public native int P2PSvrGetAudioFrameRate();
    static public native int P2PSvrGetAudioBitRate();

    static public native long FontCreateByFile(String szTTFPath, int nMaxCharWidth, int nMaxCharHeigh, int nMaxCharCount);
    static public native long FontCreateByMemory(byte[] ttf, int size, int nMaxCharWidth, int nMaxCharHeigh, int nMaxCharCount);
    static public native int FontDelete(long hFont);
    static public native int FontGetImageWidth(long hFont);
    static public native int FontGetImageHeight(long hFont);
    static public native int FontGetImageData(long hFont, byte[] pData);
    static public native int FontLoadImage(long hFont, String szText, float fAngle, int nCharWidth, int nCharHeight);
    static public native int FontDrawImage(long hFont, byte[] imageData, int width, int height, int xScale, int yScale, int mode);

}
