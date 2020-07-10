package com.jy.mfe.view;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.jy.mfe.R;
import com.jy.mfe.util.VCUtil;

import java.lang.ref.WeakReference;

public class VHBase<T> extends RecyclerView.ViewHolder {

    protected View divider;


    protected Context context;
    protected int groupId;
    protected WeakReference<T> cell;


    public VHBase(View itemView) {
        super(itemView);
        context = itemView.getContext();

        divider = itemView.findViewById(R.id.divider);

        itemView.setLongClickable(true);
    }

    public void update(int position, int groupId, T info, RecyclerView.Adapter adapter) {
        cell = new WeakReference<T>(info);
        this.groupId = groupId;


        VCUtil.showView(divider, position < adapter.getItemCount() - 1);
    }
}
