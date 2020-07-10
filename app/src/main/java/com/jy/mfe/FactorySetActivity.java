package com.jy.mfe;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jy.mfe.cache.AppCache;

/**
 * @author kunpn
 */
public class FactorySetActivity extends AppCompatActivity {
    protected Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factory_set);
        mContext = this;
        Button btn_cancel = findViewById(R.id.image_retreat);
        btn_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        TextView tx_cdid = (TextView) findViewById(R.id.fs_cur_cdid_label);
        tx_cdid.setText(AppCache.getIns().terminalResult.getDeviceid());

        EditText ed_cid = (EditText) findViewById(R.id.fs_cdid_edit);
        ed_cid.setText( AppCache.getIns().terminalResult.getDeviceid());

        Button btn_modify_cdid = (Button)findViewById(R.id.fs_cdid_modify);
        btn_modify_cdid.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {

                EditText tv = (EditText)findViewById(R.id.fs_cdid_edit);
                String result = tv.getText().toString();

                AppCache.getIns().setDeviceId(mContext,result );

                TextView tx_cdid = (TextView) findViewById(R.id.fs_cur_cdid_label);
                tx_cdid.setText( AppCache.getIns().getDeviceId(mContext));
            }
        });
    }
}
