package com.jy.mfe.talk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jy.mfe.R;
import com.jy.mfe.util.VCUtil;
import com.weivoice.srv.entity.Channel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by kunpn on 2017/9/11.
 */

public class TalkChannelAdaptor extends BaseAdapter {

    private LayoutInflater mInflater;
    protected Context mContext;
    ArrayList<Channel> lst = new ArrayList<Channel>();

    public TalkChannelAdaptor(Context context, List<Channel> lst_data) {
        if(lst_data!=null){
            for(int i=0;i<lst_data.size();i++){
                Channel s = lst_data.get(i);
                if(s!=null){
                    lst.add(s);
                }
            }
        }
        //lst.addAll(lst_data);
        this.mInflater = LayoutInflater.from(context);
        mContext = context;

    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return 0;
        if(lst==null) {
            return 0;
        }
        return lst.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        //return null;
        if(lst==null) {
            return null;
        }
        return lst.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if(lst==null) {
            return null;
        }


        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.talkchannel_list_item_layout, null);
        }

        Channel assignment = lst.get(position);

        ImageView chl_state = convertView.findViewById(R.id._channel_state);
        if(POCENV.ins().isSavedChannel(assignment.getRid())) {
            VCUtil.setImage(chl_state, R.drawable.ic_talkchl_sel);
        }else {
            VCUtil.setImage(chl_state, R.drawable.ic_talkchl_unsel);
        }

        TextView txtV = (TextView) convertView.findViewById(R.id._channel_name);
        if(assignment.getName()!=null) {
            txtV.setText(assignment.getName());
        }
        else {
            txtV.setText("");
        }

        txtV = (TextView) convertView.findViewById(R.id._channel_info);
        txtV.setText(assignment.getAttendSummary());


        return convertView;
    }
}
