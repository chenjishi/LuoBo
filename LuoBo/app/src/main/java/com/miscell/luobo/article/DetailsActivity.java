package com.miscell.luobo.article;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.R;
import com.miscell.luobo.home.Feed;
import com.miscell.luobo.utils.ErrorListener;
import com.miscell.luobo.utils.Listener;
import com.miscell.luobo.utils.NetworkRequest;
import com.miscell.luobo.utils.Utils;

/**
 * Created by jishichen on 2017/5/2.
 */
public class DetailsActivity extends BaseActivity implements Listener<Article>, ErrorListener, JSCallback {

    private WebView mWebView;

    private Feed mFeed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle args = getIntent().getExtras();
        mFeed = args.getParcelable("feed");
        String url = mFeed.url;

        Log.i("test", "#url " + url);

        mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptBridge(this), "U148");

        NetworkRequest.getInstance().getArticle(url, Article.class, this, this);
    }

    @Override
    public void onResponse(Article response) {
        if (null == response) return;

        renderPage(response);
    }

    @Override
    public void onErrorResponse() {

    }

    @Override
    public void onImageClicked(String url) {

    }

    @Override
    public void onThemeChange() {

    }

    private void renderPage(Article article) {
        String template = Utils.readFromAssets(this, "usite.html");
        template = template.replace("{TITLE}", mFeed.title);
        template = template.replace("{U_AUTHOR}", mFeed.time);
        template = template.replace("{U_COMMENT}", mFeed.views);
        template = template.replace("{CONTENT}", article.content);

        mWebView.loadDataWithBaseURL(null, template, "text/html", "UTF-8", null);
    }
}
