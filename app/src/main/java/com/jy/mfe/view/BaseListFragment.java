package com.jy.mfe.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jy.mfe.R;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

public abstract class BaseListFragment<T> extends Fragment {

    protected static final long SINCE_ID_REFRESH = -1;


    protected SuperRecyclerView listView = null;
    protected View root = null;
    protected long sinceId = SINCE_ID_REFRESH;
    protected LinearLayoutManager layoutManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            final View view = inflater.inflate(getLayoutResourceId(), container, false);
            initView(view);
            root = view;
        }
        return root;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void initView(View view) {
        listView = view.findViewById(R.id.list);

        layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);
        listView.setRefreshingColorResources(android.R.color.holo_orange_light, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_red_light);
        listView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        listView.setOnMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
                sinceId = getLastDataId();
                requestData();
            }
        });
    }

    protected void prepareToSendRequest(long sinceId) {

    }

    protected void requestData() {
        final long sinceId = this.sinceId;
        prepareToSendRequest(sinceId);
        if (!sendRequest(sinceId)) {
            listView.setRefreshing(false);
            listView.hideMoreProgress();
        }
    }

    public void refresh() {
        if (listView == null)
        {
            return;
        }
        sinceId = SINCE_ID_REFRESH;
        requestData();
    }

    protected int getLayoutResourceId() {
        return R.layout.fragment_list_base;
    }


    protected abstract long getLastDataId();

    public abstract boolean sendRequest(long sinceId);

}
