package com.jy.mfe.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jy.mfe.bean.NotifyInfo;

/**
 * It is the fragment of task list.
 *
 * @author kuny
 */
public class NotifyListFragment extends BaseListFragment<NotifyInfo> {

    protected View root = null;
    private NotifyInfo selNotifyInfo;
    public static NotifyListFragment newInstance() {
        final NotifyListFragment f = new NotifyListFragment();
        f.setArguments(new Bundle());
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            View view = inflater.inflate(getLayoutResourceId(), container, false);
            initView(view);
            root = view;
        }

       // EventBusUtil.register(this);
        return root;
    }

    @Override
    public void onDestroyView() {
        //EventBusUtil.unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        listView.setAdapter(new NotifyListAdapter(getContext(), hashCode()));
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


    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

}
