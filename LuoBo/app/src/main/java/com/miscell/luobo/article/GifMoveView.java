package com.miscell.luobo.article;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jishichen on 2017/5/5.
 */
public class GifMoveView extends View {
    private static final String TAG = "GifMovieView";

    private static final int GIF_DURATION = 2000;
    private static final int MSG_LOADED = 233;

    private Movie movie;

    private long startTime;

    private int duration;

    private float scaleX = 1.f, scaleY = 1.f;

    private boolean paused;

    private final LoadHandler handler = new LoadHandler(this);

    public GifMoveView(Context context) {
        this(context, null);
    }

    public GifMoveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifMoveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setImageUrl(final String url) {
        new Thread() {
            @Override
            public void run() {
                request(url);
                handler.sendEmptyMessage(MSG_LOADED);
            }
        }.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        paused = true;
        if (null != handler) {
            handler.removeMessages(MSG_LOADED);
        }
    }

    private void request(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setUseCaches(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            movie = Movie.decodeStream(is);
            if (null != movie) {
                duration = movie.duration();
                if (0 == duration) {
                    duration = GIF_DURATION;
                }
            }
            is.close();
        } catch (MalformedURLException e) {
            Log.i(TAG, "url not valid");
        } catch (IOException e) {
            Log.i(TAG, "connect fail");
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width, height;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        if (MeasureSpec.EXACTLY == widthMode) {
            width = widthSize;
        } else {
            width = getPaddingLeft() + getPaddingRight() + 1;
            if (MeasureSpec.AT_MOST == widthMode) {
                width = Math.min(width, widthSize);
            }
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.EXACTLY == heightMode) {
            height = heightSize;
        } else {
            height = getPaddingTop() + getPaddingBottom() + 1;
            if (MeasureSpec.AT_MOST == heightMode) {
                height = Math.min(height, heightSize);
            }
        }

        if (null != movie) {
            int movieWidth = movie.width();
            int movieHeight = movie.height();

            if (0 != movieWidth && 0 != movieHeight) {
                height = (int) (movieHeight * width * 1.f / movieWidth);

                scaleX = width * 1.f / movieWidth;
                scaleY = height * 1.f / movieHeight;
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();

        if (w == 0 || h == 0) return;
        if (null == movie) return;

        if (!paused) {
            long now = android.os.SystemClock.uptimeMillis();
            if (startTime == 0) startTime = now;

            int currentTime = (int) ((now - startTime) % duration);
            movie.setTime(currentTime);

            canvas.scale(scaleX, scaleY);
            movie.draw(canvas, 0, 0);
            postInvalidateOnAnimation();
        }
    }

    private static class LoadHandler extends Handler {

        private WeakReference<GifMoveView> mView;

        public LoadHandler(GifMoveView view) {
            mView = new WeakReference<GifMoveView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what != MSG_LOADED) return;

            GifMoveView gifView = mView.get();
            if (null == gifView) return;

            gifView.requestLayout();
            gifView.postInvalidateOnAnimation();
        }
    }
}
