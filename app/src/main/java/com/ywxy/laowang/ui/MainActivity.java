package com.ywxy.laowang.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobads.AdView;
import com.baidu.mobads.IconsAd;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;
import com.ywxy.laowang.R;
import com.ywxy.laowang.common.util.Logger;
import com.ywxy.laowang.common.base.BaseActivity;
import com.ywxy.laowang.common.bean.LaowangItem;
import com.ywxy.laowang.common.bean.LaowangItemList;
import com.ywxy.laowang.net.RequestManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity {

    private static final int REQUEST_DETAIL_CHECK = 0x100;
    private boolean isBannerAdClicked = false;
    @Bind(R.id.id_toolbar)
    Toolbar mToolbar;

    @Bind(R.id.id_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    @Bind(R.id.id_refresh_list)
    RecyclerView mRefreshList;


    @Bind(R.id.id_banner_container)
    RelativeLayout mBannerContainer;

    @Bind(R.id.id_banner_close)
    ImageView mBannerClose;

    @Bind(R.id.id_laowang_tip)
    TextView mTextTip;

    @OnClick(R.id.id_banner_close)
    void onBannerClose() {
        if (mBannerClose.isShown()) {
            if (!isBannerAdClicked) {
                isBannerAdClicked = true;
            } else {
                iconsAd = new IconsAd(this);
                iconsAd.loadAd(this);
                mBannerClose.setVisibility(View.GONE);
                mBannerContainer.setVisibility(View.GONE);
            }
        }
    }

    private AdView mCurAdView;
    private IconsAd iconsAd;

    LaowangListAdapter adapter;

    Handler mHandler;


    @Override
    protected void onDestroy() {
        if (mCurAdView != null)
            mCurAdView.destroy();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        PushAgent.getInstance(this).onAppStart();

        initViews();
        initData();

        UmengUpdateAgent.update(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_DETAIL_CHECK) {
                int curPos = data.getIntExtra(LaowangDetailActivity.KEY_CUR_POS, 0);
                Logger.d("onActivityResult:curPos:" + curPos);
                mRefreshList.getLayoutManager().scrollToPosition(curPos);
            }
        }

    }

    @Override
    protected void onResume() {
        MobclickAgent.onPageStart("ListScreen");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd("ListScreen");
        super.onPause();
    }

    private void initViews() {
        if (mToolbar != null)
            setSupportActionBar(mToolbar);

        mHandler = new Handler(Looper.getMainLooper());
        mRefreshList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LaowangListAdapter(this);
        adapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.view_load_more, null),
                LayoutInflater.from(this).inflate(R.layout.view_end_footer, null));
        adapter.setIsNeedLoadMore(true);
        adapter.setOnItemClickListener(new LaowangListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                LaowangItemList mLaowangList = adapter.getData();
                Intent intent = new Intent(MainActivity.this, LaowangDetailActivity.class);
                intent.putExtra(LaowangDetailActivity.KEY_LAOWANG_ITEMS, mLaowangList);
                intent.putExtra(LaowangDetailActivity.KEY_CUR_POS, pos);
                Logger.d("onItemClick:pos:" + pos);
                startActivityForResult(intent, REQUEST_DETAIL_CHECK);
            }
        });
        mRefreshList.setAdapter(adapter);
        mSwipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                isRefresh = true;
                loadListData();
            }
        });

        RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItemPosition + 1 == adapter.getItemCount()) {
                    loadListData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();
            }
        };
        mRefreshList.addOnScrollListener(mOnScrollListener);

        initAdViews();
    }

    private void initAdViews() {
        mCurAdView = new AdView(this);
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBannerContainer.addView(mCurAdView, rllp);
    }

    private int mLastVisibleItemPosition = 0;
    private int page = 1;
    private boolean isRefresh = false;

    private RequestManager.OnLaowangItemsRequestListener listener = null;

    private void initData() {
        listener = new RequestManager.OnLaowangItemsRequestListener() {
            @Override
            public void onSuccess(LaowangItemList list) {

                final LaowangItemList newlyList = new LaowangItemList();
                for (LaowangItem item : list.mLaowangList) {
                    String url = item.item_url;
                    if (!TextUtils.isEmpty(url)) {
                        newlyList.mLaowangList.add(item);
                    }
                }
                if (isRefresh) {
                    adapter.setData(newlyList);
                    isRefresh = false;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mTextTip.isShown())
                                mTextTip.setVisibility(View.VISIBLE);
                            mTextTip.setText(String.format(getResources().getString(R.string.str_info_latest_refresh_laowang), newlyList.mLaowangList.size()));
                            ObjectAnimator anim1 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.0f, 0.8f).setDuration(1500);
                            ObjectAnimator anim2 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.8f, 0.0f).setDuration(1500);
                            AnimatorSet set = new AnimatorSet();
                            set.play(anim2).after(anim1);
                            set.start();
                        }
                    }, 50);
                } else {
                    if (list.mLaowangList.size() == 0) {
                        adapter.setIsNeedLoadMore(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.appendData(newlyList);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!mTextTip.isShown())
                                    mTextTip.setVisibility(View.VISIBLE);
                                mTextTip.setText(String.format(mTextTip.getText().toString(), newlyList.mLaowangList.size()));
                                ObjectAnimator anim1 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.0f, 0.8f).setDuration(1500);
                                ObjectAnimator anim2 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.8f, 0.0f).setDuration(1500);
                                AnimatorSet set = new AnimatorSet();
                                set.play(anim2).after(anim1);
                                set.start();
                            }
                        }, 50);
                    }
                }
                if (mSwipeRefresh.isRefreshing())
                    mSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        };
        isRefresh = true;
        loadListData();
    }

    private void loadListData() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestManager.getInstance(getApplicationContext()).loadLaowangItems(page++, listener);
            }
        }, 200);
    }

}
