package com.miscell.luobo.article;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import com.flurry.android.FlurryAgent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.comment.CommentActivity;
import com.miscell.luobo.home.Feed;
import com.miscell.luobo.utils.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.miscell.luobo.utils.Constants.EVENT_ARTICLE_SHARE;
import static com.tencent.open.SocialConstants.PARAM_TITLE;

/**
 * Created by jishichen on 2017/5/2.
 */
public class DetailsActivity extends BaseActivity implements Listener<Article>, ErrorListener,
JSCallback, View.OnClickListener {
    private static final int TAG_SHARE = 233;
    private static final int TAG_FAVORITE = 234;
    private static final int TAG_COMMENT = 235;

    private WebView mWebView;

    private Feed mFeed;

    private Article mArticle;

    private DBHelper mDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mDatabase = DBHelper.getInstance(this);

        Bundle args = getIntent().getExtras();
        mFeed = args.getParcelable("feed");
        String url = mFeed.url;
        String cate = mFeed.category;
        setTitle(!TextUtils.isEmpty(cate) ? cate : getString(R.string.app_name));

        findViewById(R.id.title_bar).setOnClickListener(this);
        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptBridge(this), "U148");
        generateButtons();

        showLoading();
        NetworkRequest.getInstance().getArticle(url, Article.class, this, this);
    }

    @Override
    public void onResponse(Article response) {
        hideLoading();
        if (null == response) return;

        mArticle = response;
        renderPage(response);
    }

    @Override
    public void onErrorResponse() {
        setError();
    }

    @Override
    public void onImageClicked(String url) {
        HashMap<String, String> infos = mArticle.infos;

        Intent intent = new Intent(this, ImageBrowseActivity.class);
        intent.putExtra("imgsrc", url);
        intent.putStringArrayListExtra("images", mArticle.imageList);

        if (null != infos && infos.size() > 0) {
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(infos);

            intent.putExtra("infos", json);
        }

        startActivity(intent);
    }

    @Override
    public void onVideoClicked(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onThemeChange() {

    }

    private void renderPage(Article article) {
        Document doc = Jsoup.parse(article.content);
        if (null == doc) return;

        Elements embed = doc.select("embed");
        handleVideos(embed);

        Elements iframe = doc.select("iframe");
        handleVideos(iframe);

        article.content = doc.html();

        String template = Utils.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);
        template = template.replace("{U_AUTHOR}", mFeed.time);
        template = template.replace("{U_COMMENT}", mFeed.views);
        template = template.replace("{CONTENT}", article.content);

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
    }

    private void handleVideos(Elements elements) {
        if (null == elements || elements.size() == 0) return;

        for (Element el : elements) {
            String videoUrl = "http://www.u148.net/";
            if (null != el.parent()) {
                Element prev = el.parent().previousElementSibling();
                if (null != prev) {
                    Elements links = prev.select("a");
                    if (null != links && links.size() > 0) {
                        videoUrl = links.get(0).attr("href");
                    }
                }
            }
            el.parent().html("<img src=\"file:///android_asset/video.png\" title=\"" + videoUrl + "\" />");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_bar) {
            mWebView.scrollTo(0, 0);
            return;
        }

        if (null == v.getTag()) return;

        int idx = (Integer) v.getTag();
        switch (idx) {
            case TAG_COMMENT:
                Intent intent = new Intent(this, CommentActivity.class);
                startActivity(intent);
                break;
            case TAG_FAVORITE:
                addToFavorite();
                break;
            case TAG_SHARE:
                share();
                break;
        }
    }

    private void addToFavorite() {
        if (mDatabase.isFavorite(mFeed.url)) {
            Utils.showToast(this, R.string.favorite_already);
            return;
        }

        mDatabase.insertFeed(mFeed);
        generateButtons();
    }

    private void share() {
        Map<String, String> params = new HashMap<>();
        params.put(PARAM_TITLE, mFeed.title);
        FlurryAgent.logEvent(EVENT_ARTICLE_SHARE, params);

        ShareDialog dialog = new ShareDialog(this);
        ArrayList<String> imageList = mArticle.imageList;
        if (null == imageList || imageList.size() == 0) {
            imageList.add(mFeed.thumb);
        }

        dialog.setShareFeed(mFeed);
        dialog.setImageList(imageList);
        dialog.show();
    }

    private void generateButtons() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.right_view);
        layout.removeAllViews();

        int[] icons = {R.drawable.ic_social_share, R.drawable.ic_favorite,
                R.drawable.ic_comment};
        if (mDatabase.isFavorite(mFeed.url)) {
            icons[1] = R.drawable.ic_favorite_full;
        }

        for (int i = 0; i < icons.length; i++) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(48), MATCH_PARENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = i * dp2px(48);
            ImageButton button = getImageButton(icons[i]);
            button.setTag(i + TAG_SHARE);
            button.setOnClickListener(this);
            layout.addView(button, lp);
        }

        if (null != mFeed && mFeed.imageCount > 0) {
            int num = mFeed.imageCount;
            if (num >= 100) num = 99;

            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dp2px(12),
                    dp2px(12));
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.rightMargin = dp2px(48) * 2 + dp2px(4);
            lp.topMargin = dp2px(8);
            CircleView numView = new CircleView(this);
            numView.setNumber(num);
            layout.addView(numView, lp);
        }
    }

    protected ImageButton getImageButton(int resId) {
        ImageButton button = new ImageButton(this);
        button.setBackgroundResource(R.drawable.highlight_bkg);
        button.setImageResource(resId);

        return button;
    }
}
