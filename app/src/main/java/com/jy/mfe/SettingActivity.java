package com.jy.mfe;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jy.mfe.cache.AppCache;
import com.jy.mfe.talk.POCENV;
import com.jy.mfe.talk.TalkChannelAdaptor;
import com.jy.mfe.talk.TalkChannelUserAdaptor;
import com.weivoice.srv.entity.Channel;
import com.weivoice.srv.entity.Contact;

import java.util.ArrayList;
import java.util.List;

import static com.weivoice.srv.Global.POC;

public class SettingActivity extends AppCompatActivity {

    ArrayList<Channel> m_channels = null;

    ArrayList<Contact> m_roomUsers = null;
    Context context;
    ListView channellistv;
    ListView userlistv;
    TalkChannelAdaptor listChannelAdapter = null;
    TalkChannelUserAdaptor listUserAdapter = null;

    final private String password = "172431";
    protected Context mContext;
    TextView txDeviceID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);
        mContext = this;
        txDeviceID = findViewById(R.id.ls_fcs_deviceid);
        txDeviceID.setText(AppCache.getIns().terminalResult.getDeviceid() );

        Button txSysSeting = findViewById(R.id.ls_fcs_btn_system_setup);
        txSysSeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                try{
                    intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings"));

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        m_channels = Channel.getChannels();
        Channel ch = POCENV.ins().getSavedChannel();
        if(ch != null){
            buildChannelUserList(ch);
            TextView txtV = findViewById(R.id._talk_channel_name);
            txtV.setText(ch.getName());
            POCENV.ins().setCid(ch.getRid());
        }

        channellistv =  findViewById(R.id.talk_channel_list_view);
        listChannelAdapter = new TalkChannelAdaptor(mContext, m_channels);
        channellistv.setAdapter(listChannelAdapter);
        channellistv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                try{
                    Channel ch = m_channels.get(arg2);
                    POC.join(ch.getRid());
                    POCENV.ins().setCid(ch.getRid());
                    buildChannelUserList(ch);
                    TextView txtV = findViewById(R.id._talk_channel_name);
                    txtV.setText(ch.getName());
                    listChannelAdapter.notifyDataSetChanged();
                    listUserAdapter.notifyDataSetChanged();
                }catch(Exception e){
                }
            }
        });

        userlistv =  findViewById(R.id.talk_user_list_view);
        listUserAdapter = new TalkChannelUserAdaptor(mContext, m_roomUsers);
        userlistv.setAdapter(listUserAdapter);
        userlistv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                try{

                }catch(Exception e){
                }
            }
        });
        
        TextView txtAPKVersion = (TextView)findViewById(R.id.ls_fcs_softversion_text);
        String sVersion = "1.1";
        try {
            sVersion =getString(R.string.title_softver) + getVersionName(mContext);
        }
        catch (Exception e) {
        }
        txtAPKVersion.setText(sVersion);

        TextView txtHWVersion = (TextView)findViewById(R.id.ls_fcs_hardversion_text);
        String sHWInfo = "0.1";
        try {
            sHWInfo = getString(R.string.title_hardversion) + Build.MANUFACTURER + "-" + Build.MODEL + "-" + Build.SERIAL;
        }
        catch (Exception e) {
        }
        txtHWVersion.setText(sHWInfo);


        Button btnFactorySetting=(Button)findViewById(R.id.ls_fcs_btn_factorySetting);
        btnFactorySetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent =new Intent(mContext, FactorySetActivity.class);
                startActivity(intent);

            }
        });

        Button btnClose=(Button)findViewById(R.id.ls_fcs_but_back);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
    }

    public static String getVersionName(Context context) throws Exception {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        String version = packInfo.versionName;
        return version;
    }

    public void Keyboard(){
        InputMethodManager imm = (InputMethodManager)this.getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void buildChannelUserList(Channel ch){
        if(m_roomUsers == null){
            m_roomUsers = new ArrayList<>();
        }
        m_roomUsers.clear();
        List<String> m_roomUID = ch.getMems();
        if (m_roomUID == null){
            return;
        }
        for (String uid:m_roomUID) {
            Contact member = Contact.find(uid);
            if(member != null){
                m_roomUsers.add(member);
            }
        }
    }

}
