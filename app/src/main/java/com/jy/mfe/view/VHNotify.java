package com.jy.mfe.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jy.mfe.R;
import com.jy.mfe.bean.NotifyInfo;
import com.jy.mfe.util.VCUtil;

public class VHNotify extends VHBase<NotifyInfo> {
    protected TextView tx_title;
    protected TextView tx_content;
    protected TextView tx_time;

    protected ImageView nft_Icon;
    public VHNotify(View itemView) {
        super(itemView);

        // tx_id = itemView.findViewById(R.id.no);
        tx_title = itemView.findViewById(R.id.notify_title);
        tx_content = itemView.findViewById(R.id.notify_content);
        tx_time = itemView.findViewById(R.id.notify_time);
        nft_Icon= itemView.findViewById(R.id.notify_status);
    }

    public void update(int position, NotifyInfo info, NotifyListAdapter adapter)
    {
        update(position, adapter.getGroupId(), info, adapter);

        tx_title.setText(info.title);
        tx_content.setText(info.content);
        tx_time.setText(info.time);
        //VCUtil.showViewLite(nft_Icon, true);
        VCUtil.setImage(nft_Icon, R.drawable.ic_readed_notify);

    }
}
