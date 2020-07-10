package com.jy.mfe.talk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jy.mfe.R;
import com.weivoice.srv.entity.Contact;

import java.util.ArrayList;


/**
 * Created by kunpn on 2017/9/11.
 */

public class TalkChannelUserAdaptor extends BaseAdapter {

    private LayoutInflater mInflater;
    protected Context mContext;
    ArrayList<Contact> lst = new ArrayList<>();

    public TalkChannelUserAdaptor(Context context, ArrayList<Contact> lst_data) {

        lst = lst_data;
        //lst.addAll(lst_data);
        this.mInflater = LayoutInflater.from(context);
        mContext = context;

    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        //return 0;
        if(lst==null)
            return 0;
        return lst.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        //return null;
        if(lst==null)
            return null;
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
        if(lst==null){
            return null;
        }

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.talkchannel_user_item_layout, null);
        }

        Contact assignment = lst.get(position);


        TextView txtV = (TextView) convertView.findViewById(R.id._user_name);
        txtV.setText(assignment.getName());

        txtV = (TextView) convertView.findViewById(R.id._user_info);
        txtV.setText(String.valueOf(assignment.getOrg()));


        return convertView;
    }
}
