package com.jy.mfe.socket;


import java.util.List;

public class SmartDevFrameParser {

    private static int findhead(byte [] data,int from,int len)
    {
        int t=from;
        int d ;
        while(t<data.length&&t<from+len-4){
            int magic = 0;
            d = data[t];
            d &= 0xff;
            if(d!=0x5c) {
                t++;
                continue;
            }
            d = data[t+1];
            d &= 0xff;
            if(d!=0xce) {
                t++;
                continue;
            }
            d = data[t+2];
            d &= 0xff;
            if(d!=0xce) {
                t++;
                continue;
            }
            d = data[t+3];
            d &= 0xff;
            if(d!=0x5c) {
                t++;
                continue;
            }
            return t;
        }

        return -1;
    }

    private static int checkframe(byte []buff,int len,int from)
    {

        if(len<21)
            return 0;//not full
        int t = from;
        int d;
        //magic
        int magic = 0;
        d = buff[t++];
        d &= 0xff;
        magic = d;

        d = buff[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        d = buff[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        d = buff[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        if(magic!=0x5ccece5c)
            return -1;//error head

        int type = (buff[t]>>4)&0x0f;
        int enycrt = buff[t]&0x0f;
        t++;

        //version
        int version = buff[t++];
        version &= 0xff;
        d = buff[t++];
        d &= 0xff;
        version <<= 8;
        version |= d;

        //session id
        int session = buff[t++];
        session &= 0xff;
        d = buff[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        d = buff[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        d = buff[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        int checkmode =buff[t++];
        checkmode &= 0xff;

        int checkcode =buff[t++];
        checkcode &= 0xff;
        d = buff[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;
        d = buff[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;
        d = buff[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;

        int devicetype = buff[t++];
        devicetype &= 0xff;
        d = buff[t++];
        d &= 0xff;
        devicetype <<= 8;
        devicetype |= d;

        int idlen = buff[t++];
        idlen &= 0xff;

        if(idlen+t-from+2>len)
            return 0;

        t += idlen;
        int datalen = buff[t++];
        datalen &= 0xff;
        d = buff[t++];
        d &= 0xff;
        datalen <<= 8;
        datalen |= d;

        if(t+datalen-from>len)
            return 0;

        return t+datalen-from;
    }


    public static int BuildFrame( byte [] pack,int from,int len, List<String> sList){
        if(pack==null||pack.length<=0||from+len>pack.length)
            return 0;

        int processlen = 0;
        int head = 0;
        int t = from;
        do{
            head = findhead(pack,t,len-(t-from));

            if(head<0){
                if(len>=3)
                    return len-3;
                return 0;
            }else
            {
                processlen = head - from;
                if(len-(head-from)>=21)
                {
                    int rel = checkframe(pack,len-(head-from),head);
                    if(rel>=21)
                    {//full frame
                        String sJson = parseFrame(pack,head,rel);
                        sList.add(sJson);
                        t = head+rel;
                        processlen =  head+rel;
                    }else if(rel<0){
                        t += 1;
                    }else{
                        return 0;
                    }
                }else{
                    return processlen;
                }
            }
        }while(t<pack.length&&t-from<len);
        return processlen;
    }


    private static String parseFrame(byte[] frame,int from,int len){
        int t = from;
        int d;
        //magic
        int magic = 0;
        d = frame[t++];
        d &= 0xff;
        magic = d;

        d = frame[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        d = frame[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        d = frame[t++];
        d &= 0xff;
        magic <<= 8;
        magic |= d;

        if(magic!=0x5ccece5c)
            return null;//error head

        int type = (frame[t]>>4)&0x0f;
        int enycrt = frame[t]&0x0f;
        t++;

        //version
        int version = frame[t++];
        version &= 0xff;
        d = frame[t++];
        d &= 0xff;
        version <<= 8;
        version |= d;

        //session id
        int session = frame[t++];
        session &= 0xff;
        d = frame[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        d = frame[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        d = frame[t++];
        d &= 0xff;
        session <<= 8;
        session |= d;

        int checkmode =frame[t++];
        checkmode &= 0xff;

        int checkcode =frame[t++];
        checkcode &= 0xff;
        d = frame[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;
        d = frame[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;
        d = frame[t++];
        d &= 0xff;
        checkcode <<= 8;
        checkcode |= d;

        int devicetype = frame[t++];
        devicetype &= 0xff;
        d = frame[t++];
        d &= 0xff;
        devicetype <<= 8;
        devicetype |= d;

        int idlen = frame[t++];
        idlen &= 0xff;

        if(idlen+t-from+2>len)
            return null;
        if(idlen>0){
            byte []ids =new byte[idlen];
            System.arraycopy(frame,t,ids,0,idlen);
            try {
                String strid = new String(ids, "UTF-8");

            }catch (Exception e){

            }
        }
        t += idlen;

        int datalen = frame[t++];
        datalen &= 0xff;
        d = frame[t++];
        d &= 0xff;
        datalen <<= 8;
        datalen |= d;

        byte []datas = new byte[datalen];
        System.arraycopy(frame,t,datas,0,datalen);
        try {
            if (type == 0x01) {
                String xml = new String(datas, "UTF-8");
                //context.addXml(xml);
            } else if (type == 0x02) {
                //String json = new String(datas, "UTF-8");
                String sJson= new String(datas, "UTF-8");
                return sJson;
               // context.addJson(json);
            }else if(type==0x00){
               // ParseDescriptor(context,datas);
            }
        }catch (Exception e){

        }

        return null;
    }

    private void ParseDescriptor( byte []buff){
        int msgid = buff[0];
        msgid &= 0xff;
        int subid = buff[1];
        subid &= 0xff;

    }

    private static byte[] PackFrame(int devicetype,String deviceid,int ftype,int enycrt ,int session,byte[] data){
        byte []devid;
        int idlen = 0 ;
        if(data==null||data.length<=0)
        {
            return null;
        }
        if(deviceid==null) {
            devid = new byte[0];
        }else{
            devid = deviceid.getBytes();
        }

        if(devid.length>127)
        {
            idlen = 127;
        }
        else
        {
            idlen = devid.length;
        }
        byte [] buf = new byte[21+idlen+data.length];
        int t = 0;
        buf[t++] = 0x5c;
        buf[t++] = (byte)0xce;
        buf[t++] = (byte)0xce;
        buf[t++] = 0x5c;
        buf[t] = (byte)(ftype&0x0f);
        buf[t] <<= 4;
        buf[t] |= (byte)(enycrt&0x0f);
        t++;
        buf[t++] = 0;
        buf[t++] = 0;

        buf[t++] = (byte)((session>>24)&0xff);
        buf[t++] = (byte)((session>>16)&0xff);
        buf[t++] = (byte)((session>>8)&0xff);
        buf[t++] = (byte)((session)&0xff);
        buf[t++] = 0;
        buf[t++] = 0;
        buf[t++] = 0;
        buf[t++] = 0;
        buf[t++] = 0;

        buf[t++] = (byte)((devicetype>>8)&0xff);
        buf[t++] = (byte)((devicetype)&0xff);

        buf[t] = (byte)idlen;
        if(buf[t]>0)
        System.arraycopy(devid,0,buf,t+1,buf[t]);
        t += buf[t]+1;
        buf[t++] = (byte)((data.length>>8)&0xff);
        buf[t++] = (byte)((data.length)&0xff);
        System.arraycopy(data,0,buf,t,data.length);
        return buf;
    }

    public static byte[] PackXMLFrame(String xml,int session,String deviceid,int devtype){
        if(xml==null||xml.length()<=0)
        {
            return null;
        }
       return PackFrame(devtype,deviceid,1,0,session,xml.getBytes());
    }

    public static byte[] PackJSONFrame(String json,int session,String deviceid,int devtype){
        if(json==null||json.length()<=0)
        {
            return null;
        }
        return PackFrame(devtype,deviceid,2,0,session,json.getBytes());
    }
}
