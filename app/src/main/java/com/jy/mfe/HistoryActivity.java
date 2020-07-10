package com.jy.mfe;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jy.mfe.util.VCUtil;
import com.jy.mfe.view.NotifyListFragment;
import com.jy.mfe.view.PagerSlidingTabStrip;
import com.jy.mfe.view.QuestListFragment;

/**
 * @author kunpn
 */
public class HistoryActivity extends AppCompatActivity {
    protected Context mContext;

    private ViewPager pager;
    private PagerSlidingTabStrip tabBar;
   // private AlartFragment alartFragment = AlartFragment.newInstance();
    private NotifyListFragment noitfyFragment = NotifyListFragment.newInstance();
    private QuestListFragment questFragment = QuestListFragment.newInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mContext = this;
        pager = findViewById(R.id.history_pager);
        tabBar = findViewById(R.id.history_tab_bar);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return questFragment;
                    case 1:
                        return noitfyFragment;
                    default:
                        return questFragment;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.page_task);
                    case 1:
                        return getString(R.string.page_notify);
                    default:
                        return getString(R.string.page_task);
                }
            }

        });

        final Resources res = getResources();
        tabBar.setViewPager(pager);
        tabBar.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
        tabBar.setTextSize((int) VCUtil.dp2px(mContext, 30));
        tabBar.setTextColorStateList(res.getColorStateList(R.color.tab_bar_color_list_main));
        tabBar.setTabScaleRate(0.81212f);
        tabBar.setFixedUnderLineWidth((int) VCUtil.dp2px(mContext, 15f));
        tabBar.setIndicatorHeight((int) VCUtil.dp2px(mContext, 1.5f));

        Button btnClose=(Button)findViewById(R.id.history_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
    }
}
