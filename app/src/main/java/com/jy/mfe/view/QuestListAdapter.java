package com.jy.mfe.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.jy.mfe.R;
import com.jy.mfe.bean.QuestInfo;
import com.jy.mfe.cache.QuestWarehouse;

/**
 * @author kunpn
 */
public class QuestListAdapter extends RecyclerView.Adapter<VHQuest> {

    private Context context;
    private int groupId;

    public QuestListAdapter(Context context, int groupId) {
        super();

        this.context = context;
        this.groupId = groupId;
    }

    @Override
    public VHQuest onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VHQuest(LayoutInflater.from(context).inflate(R.layout.vh_quest_info, parent, false));
    }

    @Override
    public void onBindViewHolder(VHQuest holder, int position) {
        final QuestInfo qst = QuestWarehouse.load(context).get(position);

        holder.update(position, qst, this);
    }

    @Override
    public int getItemCount() {
        return QuestWarehouse.load(context).size();
    }


    public int getGroupId() {
        return groupId;
    }

}

