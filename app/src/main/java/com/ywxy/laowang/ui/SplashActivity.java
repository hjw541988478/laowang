package com.ywxy.laowang.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.umeng.analytics.MobclickAgent;
import com.ywxy.laowang.R;
import com.ywxy.laowang.common.base.BaseActivity;


public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,
                        MainActivity.class));
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        MobclickAgent.onPageStart("SplashScreen");
        super.onResume();
    }

    @Override
    protected void onPause() {
        MobclickAgent.onPageEnd("SplashScreen");
        super.onPause();
    }
}
