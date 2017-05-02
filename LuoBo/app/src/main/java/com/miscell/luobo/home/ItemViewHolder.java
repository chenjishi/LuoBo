package com.miscell.luobo.home;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.miscell.luobo.R;

/**
 * Created by jishichen on 2017/5/2.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;

    public TextView  textView, cateView, reviewsLabel, imageLabel;

    public ItemViewHolder(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.image_view);
        textView = (TextView) itemView.findViewById(R.id.title_label);
        cateView = (TextView) itemView.findViewById(R.id.cate_label);
        reviewsLabel = (TextView) itemView.findViewById(R.id.views_label);
        imageLabel = (TextView) itemView.findViewById(R.id.image_count);
    }
}
