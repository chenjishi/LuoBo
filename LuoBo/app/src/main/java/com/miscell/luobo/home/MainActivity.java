package com.miscell.luobo.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.miscell.luobo.BaseActivity;
import com.miscell.luobo.Config;
import com.miscell.luobo.R;
import com.miscell.luobo.utils.*;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class MainActivity extends BaseActivity implements Listener<FeedDoc>, ErrorListener,
        OnPageEndListener, SwipeRefreshLayout.OnRefreshListener, DrawerLayout.DrawerListener {
    private static final int REQUEST_PERMISSION = 233;

    private FeedListAdapter mListAdapter;

    private int mPage = 1;

    private OnListScrollListener mScrollListener;

    private SwipeRefreshLayout mRefreshLayout;

    private LinearLayout mLeftView;

    private DrawerLayout mDrawerLayout;

    private long mDownloadId;
    private boolean mDownloadReceiverRegistered = false;

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != mDownloadId) return;

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);

            if (!cursor.moveToFirst()) return;

            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex)) {
                return;
            }

            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String apkUriString = cursor.getString(uriIndex);

            installApk(apkUriString);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        int color = getResources().getColor(R.color.colorPrimary);
        findViewById(R.id.title_bar).setBackgroundColor(color);
        setStatusViewColor(color);

        mLeftView = (LinearLayout) findViewById(R.id.left_view);
        mLeftView.setBackgroundResource(R.drawable.home_up_bkg);
        mLeftView.setPadding(0, 0, 0, 0);
        TextView titleText = (TextView) findViewById(R.id.tv_title);
        titleText.setTextColor(Color.WHITE);
        titleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.f);
        titleText.setTypeface(null);
        ((ImageView) findViewById(R.id.ic_arrow)).setImageResource(
                R.drawable.ic_navigation_drawer);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(this);

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

        showLoading();
        request();
        checkUpdate();

        GlideBuilder glideBuilder = new GlideBuilder(this);
        String dirPath = FileUtils.getImageCacheDir(this);
        Log.i("test", "#dir path " + dirPath);
        glideBuilder.setDiskCache(new ExternalCacheDiskCacheFactory(this,
                dirPath, 100 * 1024 * 1024));
    }

    private void request() {
        mScrollListener.setIsLoading(true);

        String url = String.format(Constants.FEED_URL, mPage);
        NetworkRequest.getInstance().getFeedList(url, FeedDoc.class,
                this, this);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        int indent = (int) (slideOffset * dp2px(8));
        mLeftView.setPadding(-indent, 0, dp2px(8), 0);
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onBackClicked(View v) {
        if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
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
        hideLoading();
        onRequestEnd();
        if (null == response) return;

        List<Feed> list = response.list;
        if (null == list || list.size() == 0) return;

        if (mPage == 1) mListAdapter.clear();
        mListAdapter.addData(list);
    }

    @Override
    public void onErrorResponse() {
        setError();
        onRequestEnd();
    }

    private void onRequestEnd() {
        if (mPage > 1) mListAdapter.hideLoading();
        mScrollListener.setIsLoading(false);
        mRefreshLayout.setRefreshing(false);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        boolean flag = true;
        for (String s : permissions) {
            if (checkSelfPermission(s) != PackageManager.PERMISSION_GRANTED) {
                flag = false;
                break;
            }
        }

        if (!flag) requestPermissions(permissions, REQUEST_PERMISSION);
    }

    private void checkUpdate() {
        if (!Utils.isNetworkConnected(this)) return;

        long lastCheckTime = Config.getLastCheckTime(this);
        long currentTime = System.currentTimeMillis();
        if (lastCheckTime == -1 || currentTime >= lastCheckTime) {
            getUpdateInfo();
            Config.saveLastCheckTime(this, currentTime);
        }
    }

    private void getUpdateInfo() {
        String url = "http://misscell.oss-cn-beijing.aliyuncs.com/app/bohaishibei.txt";
        Request.Builder request = new Request.Builder().url(url);
        request.cacheControl(CacheControl.FORCE_NETWORK);
        OkHttpClient httpClient = new OkHttpClient();
        httpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                if (TextUtils.isEmpty(json)) return;

                try {
                    JSONObject jObj = new JSONObject(json);
                    JSONObject dataObj = jObj.getJSONObject("data");
                    final String apkUrl = dataObj.optString("url", "");
                    int versionCode = dataObj.optInt("versionCode", -1);
                    if (versionCode > Utils.getVersionCode(MainActivity.this)) {
                        findViewById(android.R.id.content).post(new Runnable() {
                            @Override
                            public void run() {
                                downloadApk(apkUrl);
                            }
                        });
                    }

                } catch (JSONException e) {
                }
            }
        });
    }

    private void downloadApk(final String url) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.new_version_tip))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startDownload(url);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void startDownload(String url) {
        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mDownloadCompleteReceiver, intentFilter);
        mDownloadReceiverRegistered = true;

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.updating_app))
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "bohaishibei.apk");
        mDownloadId = downloadManager.enqueue(request);
    }

    private void installApk(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        if (mDownloadReceiverRegistered) {
            unregisterReceiver(mDownloadCompleteReceiver);
        }
        if (null != mDrawerLayout) {
            mDrawerLayout.removeDrawerListener(this);
        }
        super.onDestroy();
    }
}
