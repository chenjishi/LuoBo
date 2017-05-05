package com.miscell.luobo.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.article.GifMoveView;
import com.miscell.luobo.utils.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jishichen on 2017/5/5.
 */
public class GifGridActivity extends BaseActivity {
    private final static int MSG_LOAD = 233;

    private final List<Gif> mGifList = new ArrayList<>();

    private GifGridAdapter mGridAdapter;

    private DBHelper mDatabase;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (MSG_LOAD != msg.what) return;

            mGridAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        mRootView.setBackgroundColor(getResources().getColor(R.color.gray_bg));
        mDatabase = DBHelper.getInstance(this);

        mGridAdapter = new GifGridAdapter();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favorite_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mGridAdapter);

        loadData();
    }

    private void loadData() {
        new Thread(){
            @Override
            public void run() {
                List<Gif> list = mDatabase.loadGifList();
                if (null != list && list.size() > 0) {
                    mGifList.clear();
                    mGifList.addAll(list);
                    for (Gif gif : list) {
                        Log.i("test", "#gif " + gif.url);
                    }
                }
                mHandler.sendEmptyMessage(MSG_LOAD);
            }
        }.start();
    }

    private class GifGridAdapter extends RecyclerView.Adapter<GifViewHolder> {

        @Override
        public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(GifGridActivity.this).inflate(R.layout.item_gif,
                    parent, false);
            return new GifViewHolder(v);
        }

        @Override
        public void onBindViewHolder(GifViewHolder holder, int position) {
            Gif gif = mGifList.get(position);

            holder.gifView.setImageUrl(gif.url);
        }

        @Override
        public int getItemCount() {
            return mGifList.size();
        }
    }

    private static class GifViewHolder extends RecyclerView.ViewHolder {

        public GifMoveView gifView;

        public GifViewHolder(View itemView) {
            super(itemView);
            gifView = (GifMoveView) itemView.findViewById(R.id.gif_view);
        }
    }
}
