package com.miscell.luobo.home;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.Constants;
import com.miscell.luobo.utils.ErrorListener;
import com.miscell.luobo.utils.Listener;
import com.miscell.luobo.utils.NetworkRequest;

import java.util.List;

public class MainActivity extends BaseActivity implements Listener<FeedDoc>, ErrorListener,
OnPageEndListener, SwipeRefreshLayout.OnRefreshListener {

    private FeedListAdapter mListAdapter;

    private int mPage = 1;

    private OnListScrollListener mScrollListener;

    private SwipeRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mListAdapter = new FeedListAdapter(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mScrollListener = new OnListScrollListener(layoutManager, this);

        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFE6E6E6);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mListAdapter);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int count = parent.getChildCount();
                for (int i = 0; i < count; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + 1;

                    c.drawRect(0, top, parent.getWidth(), bottom, paint);
                }
            }
        });
        recyclerView.addOnScrollListener(mScrollListener);

        request();
    }

    private void request() {
        mScrollListener.setIsLoading(true);

        String url = String.format(Constants.FEED_URL, mPage);
        NetworkRequest.getInstance().getFeedList(url, FeedDoc.class,
                this, this);
    }

    @Override
    public void onRefresh() {
        mRefreshLayout.setRefreshing(true);
        mPage = 1;
        request();
    }

    @Override
    public void onPageEnd() {
        mListAdapter.showLoading();
        mPage += 1;
        request();
    }

    @Override
    public void onResponse(FeedDoc response) {
        onRequestEnd();
        if (null == response) return;

        List<Feed> list = response.list;
        if (null == list || list.size() == 0) return;

        if (mPage == 1) mListAdapter.clear();
        mListAdapter.addData(list);
    }

    @Override
    public void onErrorResponse() {
        onRequestEnd();
    }

    private void onRequestEnd() {
        if (mPage > 1) mListAdapter.hideLoading();
        mScrollListener.setIsLoading(false);
        mRefreshLayout.setRefreshing(false);
    }
}
