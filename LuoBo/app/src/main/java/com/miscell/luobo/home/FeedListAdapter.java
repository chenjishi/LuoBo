package com.miscell.luobo.home;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.miscell.luobo.R;
import com.miscell.luobo.article.DetailsActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jishichen on 2017/5/2.
 */
public class FeedListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;

    private final List<Feed> feedList = new ArrayList<>();

    private Context context;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null == v.getTag()) return;

            Feed feed = (Feed) v.getTag();

            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("feed", feed);
            context.startActivity(intent);
        }
    };

    public FeedListAdapter(Context context) {
        this.context = context;
    }

    public void addData(List<Feed> list) {
        feedList.addAll(list);
        notifyDataSetChanged();
    }

    public void clear() {
        feedList.clear();
        notifyDataSetChanged();
    }

    public void showLoading() {
        feedList.add(null);
        notifyItemInserted(getItemCount() - 1);
    }

    public void hideLoading() {
        if (feedList.size() <= 0) return;

        feedList.remove(getItemCount() - 1);
        notifyItemRemoved(getItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        return null == feedList.get(position) ? TYPE_FOOTER : TYPE_NORMAL;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        boolean isFoot = viewType == TYPE_FOOTER;
        View v = LayoutInflater.from(context).inflate(
                isFoot ? R.layout.item_foot : R.layout.item_feed, parent, false);
        return isFoot ? new FootViewHolder(v) : new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (null == feedList.get(position)) return;

        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Feed feed = feedList.get(position);

        String url = feed.thumb;
        if (!TextUtils.isEmpty(url)) {
            Glide.with(context).load(url).into(viewHolder.imageView);
        }

        viewHolder.textView.setText(feed.title);
        if (!TextUtils.isEmpty(feed.category)) {
            viewHolder.cateView.setText(feed.category);
            viewHolder.cateView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.cateView.setText(feed.category);
            viewHolder.cateView.setVisibility(View.GONE);
        }
        viewHolder.reviewsLabel.setText(feed.views);
        int count = feed.imageCount;
        if (count > 0) {
            viewHolder.imageLabel.setText(String.valueOf(feed.imageCount));
            viewHolder.imageLabel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.imageLabel.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(onClickListener);
        viewHolder.itemView.setTag(feed);
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }
}
