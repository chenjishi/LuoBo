package com.miscell.luobo.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.DBHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jishichen on 2017/5/5.
 */
public class FavoriteActivity extends BaseActivity {
    private final static int MSG_LOAD = 233;

    private final List<Feed> mFeedList = new ArrayList<>();

    private final LoadHandler mHandler = new LoadHandler(this);

    private DBHelper mDatabase;

    private FeedListAdapter mListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        mDatabase = DBHelper.getInstance(this);

        mListAdapter = new FeedListAdapter(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorite_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mListAdapter);

        loadData();
    }

    private void loadData() {
        new Thread(){
            @Override
            public void run() {
                List<Feed> list = mDatabase.loadFavorites();
                if (null != list && list.size() > 0) {
                    mFeedList.clear();
                    mFeedList.addAll(list);
                }
                mHandler.sendEmptyMessage(MSG_LOAD);
            }
        }.start();
    }

    public void updateView() {
        mListAdapter.addData(mFeedList);
    }

    private static class LoadHandler extends Handler {

        private WeakReference<FavoriteActivity> mActivity;

        public LoadHandler(FavoriteActivity activity) {
            mActivity = new WeakReference<FavoriteActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_LOAD) return;

            FavoriteActivity activity = mActivity.get();
            if (null == activity) return;

            activity.updateView();
        }
    }
}
