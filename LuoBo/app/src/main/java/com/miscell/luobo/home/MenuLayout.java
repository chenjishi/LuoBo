package com.miscell.luobo.home;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.Utils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by jishichen on 2017/5/5.
 */
public class MenuLayout extends FrameLayout implements View.OnClickListener {
    private static final int TAG_FAVORITE = 233;
    private static final int TAG_GIF = 234;
    private static final int TAG_ABOUT = 235;

    public MenuLayout(Context context) {
        this(context, null);
    }

    public MenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context ctx) { setClickable(true);
        LayoutParams lp = new LayoutParams(MATCH_PARENT, MATCH_PARENT);
        ImageView backgroundImage = new ImageView(ctx);
        backgroundImage.setImageResource(R.drawable.road);
        backgroundImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(backgroundImage, lp);

        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(container, lp);

        String[] titles = getResources().getStringArray(R.array.menu_items);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        for (int i = 0; i < titles.length; i++) {
            LinearLayout view = getItemView(ctx, titles[i]);
            view.setTag(TAG_FAVORITE + i);
            container.addView(view, lp1);
        }
    }

    private LinearLayout getItemView(Context ctx, String text) {
        LinearLayout layout = new LinearLayout(ctx);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp2px(8), dp2px(14), dp2px(8), 0);
        layout.setBackgroundResource(R.drawable.menu_list_highlight);
        layout.setOnClickListener(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        TextView textView = Utils.generateTextView(ctx, text, 0xFFEEEEEE,
                16.f);
        layout.addView(textView, lp);

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(MATCH_PARENT, 1);
        lp1.topMargin = dp2px(14);
        View divider = new View(ctx);
        divider.setBackgroundColor(0x66FFFFFF);
        layout.addView(divider, lp1);

        return layout;
    }

    private int dp2px(int d) {
        return (int) (d * getResources().getDisplayMetrics().density + .5f);
    }

    @Override
    public void onClick(View v) {
        if (null == v.getTag()) return;

        Intent intent = new Intent();
        int tag = (Integer) v.getTag();
        switch (tag) {
            case TAG_FAVORITE:
                intent.setClass(getContext(), FavoriteActivity.class);
                break;
            case TAG_GIF:
                intent.setClass(getContext(), GifGridActivity.class);
                break;
            case TAG_ABOUT:
                break;
        }
        getContext().startActivity(intent);
    }
}
