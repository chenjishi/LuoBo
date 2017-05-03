package com.miscell.luobo.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.miscell.luobo.article.Article;
import com.miscell.luobo.home.Feed;
import com.miscell.luobo.home.FeedDoc;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jishichen on 2017/4/26.
 */
public class NetworkRequest {
    private static final NetworkRequest INSTANCE = new NetworkRequest();

    private OkHttpClient mHttpClient;


    private Handler mHandler;

    private NetworkRequest() {
        mHttpClient = new OkHttpClient();
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static NetworkRequest getInstance() {
        return INSTANCE;
    }

    public void getBytes(String url,
                         final Listener<byte[]> listener,
                         final ErrorListener errorListener) {
        Request.Builder request = new Request.Builder()
                .url(url);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                listener.onResponse(response.body().bytes());
            }
        });
    }

    public <T> void getFeedList(String url,
                            final Class<T> clazz,
                            final Listener<T> listener,
                            final ErrorListener errorListener) {
        final Request.Builder request = new Request.Builder()
                .url(url);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                final List<Feed> feedList = new ArrayList<>();
                final Elements list = doc.select("article.excerpt");

                for (Element el : list) {
                    Feed feed = new Feed();

                    Elements header = el.select("header");
                    if (null != header && header.size() > 0) {
                        Element title = header.get(0);
                        feed.category = title.select(".cat").get(0).text();
                        feed.title = title.select("h2 a").text();
                        String count = title.select(".text-muted").text();
                        if (!TextUtils.isEmpty(count)) {
                            feed.imageCount = Integer.parseInt(count);
                        }
                    }

                    Elements time = el.select("p.time");
                    if (null != time && time.size() > 0) {
                        feed.time = time.get(0).text();
                    }

                    Elements image = el.select("p.focus a");
                    if (null != image && image.size() > 0) {
                        Element thumb = image.get(0);
                        feed.url = thumb.attr("href");
                        feed.thumb = thumb.select("img").get(0).attr("src");
                    }

                    Elements content = el.select("p.note");
                    if (null != content && content.size() > 0) {
                        feed.desc = content.get(0).text();
                    }

                    Elements views = el.select("p.views");
                    if (null != views && views.size() > 0) {
                        Element view = views.get(0);
                        feed.views = view.select(".post-views").text();
                    }

                    feedList.add(feed);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        FeedDoc doc = new FeedDoc();
                        doc.list = new ArrayList<>();
                        doc.list.addAll(feedList);

                        T t = clazz.cast(doc);
                        listener.onResponse(t);
                    }
                });
            }
        });
    }

    public <T> void getArticle(String url,
                               final Class<T> clazz,
                               final Listener<T> listener,
                               final ErrorListener errorListener) {
        final Request.Builder request = new Request.Builder()
                .url(url);
        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                Elements contents = doc.select("article.article-content");
                final Article article = new Article();
                if (null != contents && contents.size() > 0) {
                    article.content = contents.get(0).html();
                }

                Elements images = doc.select("article.article-content img");
                if (null != images && images.size() > 0) {
                    article.imageList = new ArrayList<>();
                    article.infos = new HashMap<>();

                    for (Element img : images) {
                        String url = img.attr("src");
                        if (null != img.parent()) {
                            Element title = img.parent().previousElementSibling();
                            if (null != title && title.tagName().equals("p")) {
                                String text = title.text();
                                if (!TextUtils.isEmpty(text)) {
                                    article.infos.put(url, text);
                                }
                            }
                        }
                        article.imageList.add(url);
                    }
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        T t = clazz.cast(article);
                        listener.onResponse(t);
                    }
                });
            }
        });
    }

    public void get(String url,
                    final Listener<String> listener,
                    final ErrorListener errorListener) {
        Request.Builder request = new Request.Builder()
                .url(url);

        mHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                onError(errorListener);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                onSuccess(listener, response.body().string());
            }
        });
    }

    private void onError(final ErrorListener listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onErrorResponse();
            }
        });
    }

    private void onSuccess(final Listener<String> listener, final String json) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(json);
            }
        });
    }
}
