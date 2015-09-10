package com.ywxy.laowang.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mobads.InterstitialAd;
import com.bumptech.glide.Glide;
import com.umeng.analytics.MobclickAgent;
import com.ywxy.laowang.R;
import com.ywxy.laowang.common.base.BaseActivity;
import com.ywxy.laowang.common.bean.LaowangItem;
import com.ywxy.laowang.common.bean.LaowangItemList;
import com.ywxy.laowang.common.util.Logger;

import uk.co.senab.photoview.PhotoView;

public class LaowangDetailActivity extends BaseActivity {

    public static final String KEY_LAOWANG_ITEMS = "key_laowang";
    public static final String KEY_CUR_POS = "key_cur_pos";

    DetailViewPager mPager;

    TextView mText;
    InterstitialAd interAd;
    private LaowangItemList itemList = new LaowangItemList();
    private int curPos = 0;


    @Override
    protected void onResume() {
        MobclickAgent.onPageStart("DetailScreen");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd("DetailScreen");
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_CUR_POS, curPos);
        Logger.d("onBackPressed:pos:" + curPos);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            itemList.mLaowangList.clear();
            itemList.mLaowangList.addAll(
                    ((LaowangItemList) getIntent().getSerializableExtra(KEY_LAOWANG_ITEMS))
                            .mLaowangList);
            curPos = getIntent().getIntExtra(KEY_CUR_POS, 0);
        }
        setContentView(R.layout.activity_laowang_detail);
        mPager = (DetailViewPager) findViewById(R.id.id_laowang_pager);
        mText = (TextView) findViewById(R.id.id_detail_text);
        findViewById(R.id.id_detail_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        interAd = new InterstitialAd(LaowangDetailActivity.this);
        interAd.loadAd();
        mPager.setAdapter(new LaowangPagerAdapter());
        mText.setText(String.format("%s %s/%s", itemList.mLaowangList.get(curPos).item_text, curPos + 1, itemList.mLaowangList.size()));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position != itemList.mLaowangList.size()) {
                    mText.setText(String.format("%s %s/%s", itemList.mLaowangList.get(position).item_text, position + 1, itemList.mLaowangList.size()));
                    curPos = position;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setCurrentItem(curPos);
    }

    class LaowangPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return itemList.mLaowangList.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            if (position == curPos + 1 || position == curPos - 1) {
                if (interAd.isAdReady())
                    interAd.showAd(LaowangDetailActivity.this);
            }
            LaowangItem item = itemList.mLaowangList.get(position);
//            RequestManager.getInstance(LaowangDetailActivity.this).
//                    loadNetworkImage(photoView, item.item_url);
            Glide.with(LaowangDetailActivity.this).load(item.item_url)
                    .placeholder(R.drawable.ic_default_img)
                    .error(R.drawable.ic_default_img)
                    .into(photoView);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
