package com.miscell.luobo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.flurry.android.FlurryAgent;
import com.miscell.luobo.utils.LoadingView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by jishichen on 2017/5/2.
 */
public class BaseActivity extends AppCompatActivity {
    protected boolean mHideTitle;
    protected int mTitleResId = -1;

    protected FrameLayout mRootView;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRootView = (FrameLayout) findViewById(android.R.id.content);
        mRootView.setBackgroundColor(getResources().getColor(R.color.background));
        setStatusViewColor(0xFFE5E5E5);
    }

    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        if (!mHideTitle) {
            int resId = mTitleResId == -1 ? R.layout.title_base : mTitleResId;
            inflater.inflate(resId, mRootView);
        }

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT, Gravity.BOTTOM);
        lp.topMargin = mHideTitle ? 0 : dp2px(48);
        mRootView.addView(inflater.inflate(layoutResID, null), lp);
    }

    protected void setContentView(int layoutResID, int titleResId) {
        mTitleResId = titleResId;
        setContentView(layoutResID);
    }

    protected void setContentView(int layoutResID, boolean hideTitle) {
        mHideTitle = hideTitle;
        setContentView(layoutResID);
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView textView = (TextView) findViewById(R.id.tv_title);
        textView.setText(title);
        textView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    protected void onRightIconClicked() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    public void onBackClicked(View v) {
        finish();
    }

    protected void showLoading() {
        if (null != mLoadingView && null != mLoadingView.getParent()) {
            ((ViewGroup) mLoadingView.getParent()).removeView(mLoadingView);
        } else {
            mLoadingView = new LoadingView(this);
        }

        mLoadingView.showLoading();
        mRootView.addView(mLoadingView, new FrameLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT));
    }

    protected void hideLoading() {
        if (null != mLoadingView) {
            mRootView.removeView(mLoadingView);
        }
    }

    protected void setError() {
        setError(getString(R.string.network_disconnect));
    }

    protected void setError(String text) {
        if (null != mLoadingView) {
            mLoadingView.setError(text);
        }
    }

    protected void setStatusViewColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(color);
    }

    protected int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
