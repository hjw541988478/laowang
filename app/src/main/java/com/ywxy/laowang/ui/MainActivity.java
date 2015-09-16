package com.ywxy.laowang.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobads.AdView;
import com.baidu.mobads.IconsAd;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;
import com.umeng.update.UmengUpdateAgent;
import com.ywxy.laowang.R;
import com.ywxy.laowang.common.base.BaseActivity;
import com.ywxy.laowang.common.bean.LaowangItemList;
import com.ywxy.laowang.net.RequestManager;

import java.lang.reflect.Field;


public class MainActivity extends BaseActivity {

    private static final int REQUEST_DETAIL_CHECK = 0x100;
    Toolbar mToolbar;
    SwipeRefreshLayout mSwipeRefresh;
    RecyclerView mRefreshList;
    RelativeLayout mBannerContainer;
    ImageView mBannerClose;
    TextView mTextTip;
    LaowangGridAdapter adapter;
    Handler mHandler;
    private boolean isBannerAdClicked = false;
    private AdView mCurAdView;
    private IconsAd iconsAd;
    private float mStartY = 0, mLastY = 0, mLastDeltaY;
    private boolean isToolbarShow = true;
    private int deltaY = 0;
    private int mLastVisibleItemPosition = 0;
    private int page = 1;
    private boolean isRefresh = false;
    private RequestManager.OnLaowangItemsRequestListener listener = null;

    private void onBannerClose() {
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
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.id_swipe_refresh);
        mRefreshList = (RecyclerView) findViewById(R.id.id_refresh_list);
        mBannerContainer = (RelativeLayout) findViewById(R.id.id_banner_container);
        mBannerClose = (ImageView) findViewById(R.id.id_banner_close);
        mTextTip = (TextView) findViewById(R.id.id_laowang_tip);
        mBannerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBannerClose();
            }
        });
        if (mToolbar != null)
            setSupportActionBar(mToolbar);


        mHandler = new Handler(Looper.getMainLooper());
        mRefreshList.setItemAnimator(new DefaultItemAnimator());
        mRefreshList.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new LaowangGridAdapter(this);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        adapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.view_load_more, null),
                LayoutInflater.from(this).inflate(R.layout.view_end_footer, null));
        adapter.setIsNeedLoadMore(true);
        adapter.setOnItemClickListener(new LaowangGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                LaowangItemList mLaowangList = adapter.getData();
                Intent intent = new Intent(MainActivity.this, LaowangDetailActivity.class);
                intent.putExtra(LaowangDetailActivity.KEY_LAOWANG_ITEMS, mLaowangList);
                intent.putExtra(LaowangDetailActivity.KEY_CUR_POS, pos);
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
        ViewTreeObserver vto = mSwipeRefresh.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                Float mDistanceToTriggerSync = 800 * metrics.density;

                try {
                    Field field = SwipeRefreshLayout.class.getDeclaredField("mDistanceToTriggerSync");
                    field.setAccessible(true);
                    field.setFloat(mSwipeRefresh, mDistanceToTriggerSync);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ViewTreeObserver obs = mSwipeRefresh.getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);
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
                mLastVisibleItemPosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

//                int firstVisibleItem = ((LinearLayoutManager) mRefreshList.getLayoutManager()).findFirstVisibleItemPosition();
//                if (firstVisibleItem == 0) {
//                    if (!isToolbarShow) {
//                        showToolbar();
//                    } else {
//                        if (deltaY > 25 && isToolbarShow) {
//                            hideToolbar();
//
//                        }
//                        if (deltaY <= -25 && !isToolbarShow) {
//                            showToolbar();
//                        }
//                    }
//
//                    if ((isToolbarShow && dy > 0) || (!isToolbarShow && dy < 0)) {
//                        deltaY += dy;
//                    }
//                }
            }
        };
        mRefreshList.addOnScrollListener(mOnScrollListener);

        initAdViews();
    }

    private void showToolbar() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(mToolbar, "translationY", -mToolbar.getHeight(), 0f);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isToolbarShow = true;
                deltaY = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(500).start();
    }

    private void hideToolbar() {
        ObjectAnimator hideAnim = ObjectAnimator.ofFloat(mToolbar, "translationY", 0f, mToolbar.getHeight());
        hideAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isToolbarShow = false;
                deltaY = 0;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnim.setDuration(500).start();
    }

    private void initAdViews() {
        mCurAdView = new AdView(this);
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBannerContainer.addView(mCurAdView, rllp);
    }

    private void showTipAnimation() {

    }

    private void initData() {
        listener = new RequestManager.OnLaowangItemsRequestListener() {
            @Override
            public void onSuccess(final LaowangItemList list) {

                if (isRefresh) {
                    adapter.setData(list);
                    isRefresh = false;
                    adapter.setIsNeedLoadMore(true);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!mTextTip.isShown())
                                mTextTip.setVisibility(View.VISIBLE);
                            mTextTip.setText(String.format(getResources().getString(R.string.str_info_latest_refresh_laowang), list.mLaowangList.size()));
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
                        adapter.appendData(list);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (!mTextTip.isShown())
                                    mTextTip.setVisibility(View.VISIBLE);
                                mTextTip.setText(String.format(mTextTip.getText().toString(), list.mLaowangList.size()));
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
            public void onError(final String error) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!mTextTip.isShown())
                            mTextTip.setVisibility(View.VISIBLE);
                        ObjectAnimator anim1 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.0f, 0.8f).setDuration(1500);
                        mTextTip.setText(error);
                        ObjectAnimator anim2 = ObjectAnimator.ofFloat(mTextTip, "alpha", 0.8f, 0.0f).setDuration(1500);
                        AnimatorSet set = new AnimatorSet();
                        set.play(anim2).after(anim1);
                        set.start();
                    }
                }, 50);
                if (mSwipeRefresh.isRefreshing())
                    mSwipeRefresh.setRefreshing(false);
            }
        };
        isRefresh = true;
        loadListData();
    }

    private void loadListData() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestManager.getInstance(MainActivity.this).loadLaowangItems(MainActivity.this, page++, listener);
            }
        }, 200);
    }

}
