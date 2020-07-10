package com.jy.mfe.view;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.jy.mfe.R;
import com.jy.mfe.bean.NotifyInfo;
import com.jy.mfe.cache.NotifyWarehouse;

public class NotifyListAdapter extends RecyclerView.Adapter<VHNotify> {

    private Context context;
    private int groupId;

    public NotifyListAdapter(Context context, int groupId) {
        super();

        this.context = context;
        this.groupId = groupId;
    }

    @Override
    public VHNotify onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VHNotify(LayoutInflater.from(context).inflate(R.layout.vh_notify_info, parent, false));
    }

    @Override
    public void onBindViewHolder(VHNotify holder, int position) {
        final NotifyInfo notify = NotifyWarehouse.loadReaded(context).get(position);

        holder.update(position, notify, this);
    }

    @Override
    public int getItemCount() {
        return NotifyWarehouse.loadReaded(context).size();
    }


    public int getGroupId() {

        return groupId;
    }

}

