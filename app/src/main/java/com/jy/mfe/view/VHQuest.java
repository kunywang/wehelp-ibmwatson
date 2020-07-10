package com.jy.mfe.view;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jy.mfe.R;
import com.jy.mfe.bean.QuestInfo;
import com.jy.mfe.util.VCUtil;

/**
 * @author kunpn
 */
public class VHQuest extends VHBase<QuestInfo>{

    protected TextView tx_title;
    protected TextView tx_content;
    protected TextView tx_time;
    protected TextView tx_type;
    protected TextView tx_addr;
    protected TextView tx_qid;
    protected ImageView nft_Icon;
    public VHQuest(View itemView) {
        super(itemView);

        tx_title = itemView.findViewById(R.id.quest_title);
        tx_content = itemView.findViewById(R.id.quest_content);
        tx_time = itemView.findViewById(R.id.quest_time);
        nft_Icon= itemView.findViewById(R.id.quest_status);
        tx_type = itemView.findViewById(R.id.quest_type);
        tx_addr= itemView.findViewById(R.id.quest_addr);
        tx_qid = itemView.findViewById(R.id.quest_id);

    }

    public void update(int position, QuestInfo info, QuestListAdapter adapter)
    {
        update(position, adapter.getGroupId(), info, adapter);

        tx_title.setText(info.sTitle);
        tx_content.setText(info.sContent);
        tx_time.setText(info.sTime);
        tx_type.setText(info.sType + ":");
        tx_addr.setText(info.sAddress);
        tx_qid.setText(info.sQuestID);

        VCUtil.showViewLite(nft_Icon, true);

        if(info.sType.equalsIgnoreCase(this.context.getString(R.string.quest_fire_alart)))
        {
            VCUtil.setImage(nft_Icon, R.drawable.ic_firealart_open);
        }
        else if(info.sType.equalsIgnoreCase(this.context.getString(R.string.quest_help)))
        {
            VCUtil.setImage(nft_Icon, R.drawable.ic_help_open);
        }
        else if(info.sType.equalsIgnoreCase(this.context.getString(R.string.quest_maintain)))
        {
            VCUtil.setImage(nft_Icon, R.drawable.ic_maintern_open);
        }
        else
        {
            VCUtil.setImage(nft_Icon, R.drawable.ic_help_open);
        }

    }
}

