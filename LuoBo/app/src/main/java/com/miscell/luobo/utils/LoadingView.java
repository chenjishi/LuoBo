package com.miscell.luobo.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.miscell.luobo.R;

/**
 * Created by jishichen on 2017/5/8.
 */
public class LoadingView extends FrameLayout {

    private ProgressBar mProgress;

    private TextView mTextView;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.loading_layout, this, true);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mTextView = (TextView) findViewById(R.id.text_view);
    }

    public void showLoading() {
        mTextView.setVisibility(GONE);
        mProgress.setVisibility(VISIBLE);
    }

    public void setError(String text) {
        if (!Utils.isNetworkConnected(getContext())) {
            mTextView.setText(R.string.network_disconnect);
        } else {
            mTextView.setText(text);
        }

        mProgress.setVisibility(GONE);
        mTextView.setVisibility(VISIBLE);
    }
}
