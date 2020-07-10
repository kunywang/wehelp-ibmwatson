package com.jy.mfe.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jy.mfe.bean.QuestInfo;

/**
 * It is the fragment of task list.
 *
 * @author kuny
 */
public class QuestListFragment extends BaseListFragment<QuestInfo> {

    private QuestInfo selQuestInfo;

    public static QuestListFragment newInstance() {
        final QuestListFragment f = new QuestListFragment();
        f.setArguments(new Bundle());
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        //EventBusUtil.register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
       // EventBusUtil.unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        listView.setAdapter(new QuestListAdapter(getContext(), hashCode()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    protected long getLastDataId() {
        return 0;
    }

    @Override
    public boolean sendRequest(long sinceId) {
        return false;
    }



    public void UpdateData() {
        if (listView != null) {
            listView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
}
