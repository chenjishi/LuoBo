package com.miscell.luobo.article;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jishichen on 2017/4/26.
 */
public class ImageBrowseActivity extends BaseActivity implements ViewPager.OnPageChangeListener,
        Listener<byte[]>, ErrorListener {
    private static final String TAG_PAGE = "page_%d";
    private ViewPager mViewPager;
    private RelativeLayout mToolBar;

    private final List<String> mImageList = new ArrayList<>();

    private int mCurrentIndex;

    private HashMap<String, String> mInfos = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        setContentView(R.layout.activity_image_browse, true);

        Bundle bundle = getIntent().getExtras();
        if (null == bundle) return;

        String json = bundle.getString("infos");
        if (!TextUtils.isEmpty(json)) {
            Gson gson = new GsonBuilder().create();
            HashMap<String, String> map = gson.fromJson(json, HashMap.class);
            if (null != map && map.size() > 0) {
                mInfos.putAll(map);
            }
        }

        List<String> list = bundle.getStringArrayList("images");
        mImageList.clear();
        mImageList.addAll(list);
        String currentUrl = bundle.getString("imgsrc");

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(currentUrl)) {
                mCurrentIndex = i;
                break;
            }
        }

        mToolBar = (RelativeLayout) findViewById(R.id.tool_bar);
        mViewPager = (ViewPager) findViewById(R.id.pager_photo);
        mViewPager.setAdapter(new PhotoPagerAdapter());
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOffscreenPageLimit(5);
    }

    @Override
    protected void onDestroy() {
        if (null != mViewPager) mViewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    public void onCloseButtonClicked(View v) {
        finish();
    }

    public void onDownloadButtonClicked(View v) {
        String imageUrl = mImageList.get(mCurrentIndex);
        if (TextUtils.isEmpty(imageUrl)) return;

        if (isGif(imageUrl)) {
            String text = mInfos.get(imageUrl);
            DBHelper.getInstance(this).insertGif(imageUrl, text);
            Utils.showToast(this, R.string.gif_save_success);
        } else {
            NetworkRequest.getInstance().getBytes(imageUrl, this, this);
        }
    }

    public void onShareButtonClicked(View v) {
        ShareDialog dialog = new ShareDialog(this);
        String imageUrl = mImageList.get(mCurrentIndex);
        ArrayList<String> imageList = new ArrayList<>();
        imageList.add(imageUrl);
        dialog.setImageList(imageList);
        dialog.show();

        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.PARAM_URL, imageUrl);
        FlurryAgent.logEvent(Constants.EVENT_IMAGE_SHARE, params);
    }

    private void onImageTap() {
        final boolean visible = mToolBar.getVisibility() == View.VISIBLE;

        int h = mToolBar.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(mToolBar, View.TRANSLATION_Y,
                visible ? 0 : h, visible ? h : 0);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (!visible) mToolBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (visible) mToolBar.setVisibility(View.GONE);
            }
        });
        animator.start();

        titleAnimation(visible);
    }

    private void titleAnimation(final boolean visible) {
        View view = mViewPager.findViewWithTag(String.format(TAG_PAGE, mCurrentIndex));
        if (null == view) return;

        final View titleView = view.findViewById(R.id.title_label);
        int h = titleView.getHeight();
        ObjectAnimator animator = ObjectAnimator.ofFloat(titleView, View.TRANSLATION_Y,
                visible ? 0 : -h, visible ? -h : 0);
        animator.setDuration(250);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                if (!visible) titleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (visible) titleView.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentIndex = position;

        View view = mViewPager.findViewWithTag(String.format(TAG_PAGE, position + 1));
        if (null == view) return;


        View titleView = view.findViewById(R.id.title_label);
        titleView.setVisibility(mToolBar.getVisibility() == View.VISIBLE
                ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onResponse(byte[] response) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);

        String url = mImageList.get(mCurrentIndex);
        String suffix = ".jpg";
        int idx = url.lastIndexOf(".");
        if (-1 != idx) suffix = url.substring(idx);

        String name = System.currentTimeMillis() + suffix;
        ContentResolver cr = ImageBrowseActivity.this.getContentResolver();
        String picUrl = MediaStore.Images.Media.insertImage(cr, bitmap, name, "Image Saved From U148");

        if (!TextUtils.isEmpty(picUrl)) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            String imagePath = getFilePathByContentResolver(Uri.parse(picUrl));
            Uri uri = Uri.fromFile(new File(imagePath));
            intent.setData(uri);
            ImageBrowseActivity.this.sendBroadcast(intent);
        }

        showDownloadTips(getString(TextUtils.isEmpty(picUrl) ?
                R.string.image_save_fail : R.string.image_save_success));
    }

    @Override
    public void onErrorResponse() {
        showDownloadTips(getString(R.string.image_save_fail));
    }

    private void showDownloadTips(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(ImageBrowseActivity.this, tip);
            }
        });
    }

    private String getFilePathByContentResolver(Uri uri) {
        if (null == uri) return null;

        Cursor c = getContentResolver().query(uri, null, null, null, null);
        String filePath = null;
        if (null == c) {
            throw new IllegalArgumentException(
                    "Query on " + uri + " returns null result.");
        }
        try {
            if ((c.getCount() != 1) || !c.moveToFirst()) {
            } else {
                filePath = c.getString(c.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
        } finally {
            c.close();
        }
        return filePath;
    }

    private boolean isGif(String url) {
        return url.endsWith("gif") || url.endsWith("GIF") || url.endsWith("Gif");
    }

    private class PhotoPagerAdapter extends PagerAdapter {

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageTap();
            }
        };

        @Override
        public int getCount() {
            return mImageList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(ImageBrowseActivity.this)
                    .inflate(R.layout.photo_item, null);
            view.setTag(String.format(TAG_PAGE, position));

            TextView textView = (TextView) view.findViewById(R.id.loading_text);
            TouchImageView imageView = (TouchImageView) view.findViewById(R.id.img_photo);
            GifMoveView gifView = (GifMoveView) view.findViewById(R.id.gif_view);
            TextView titleView = (TextView) view.findViewById(R.id.title_label);

            String url = mImageList.get(position);

            if (!TextUtils.isEmpty(url)) {
                if (null != mInfos.get(url)) {
                    titleView.setText(mInfos.get(url));
                    titleView.setVisibility(View.VISIBLE);
                } else {
                    titleView.setVisibility(View.GONE);
                }

                if (isGif(url)) {
                    imageView.setVisibility(View.GONE);
                    gifView.setImageUrl(url);
                    gifView.setVisibility(View.VISIBLE);
                    gifView.setOnClickListener(mOnClickListener);
                } else {
                    gifView.setVisibility(View.GONE);
                    Glide.with(ImageBrowseActivity.this).load(url).into(imageView);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setOnClickListener(mOnClickListener);
                }
            } else {
                imageView.setVisibility(View.GONE);
                gifView.setVisibility(View.GONE);
                textView.setText(R.string.image_loading_fail);
                textView.setVisibility(View.VISIBLE);
                titleView.setVisibility(View.GONE);
            }
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
