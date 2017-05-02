package com.miscell.luobo.home;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jishichen on 2017/4/26.
 */
public class Feed implements Parcelable {

    public String category;

    public String title;

    public int imageCount;

    public String desc;

    public String time;

    public String views;

    public String tag;

    public String thumb;

    public String url;

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };

    public Feed() {

    }

    public Feed(Parcel in) {
        category = in.readString();
        title = in.readString();
        imageCount = in.readInt();
        desc = in.readString();
        time = in.readString();
        views = in.readString();
        tag = in.readString();
        thumb = in.readString();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(title);
        dest.writeInt(imageCount);
        dest.writeString(desc);
        dest.writeString(time);
        dest.writeString(views);
        dest.writeString(tag);
        dest.writeString(thumb);
        dest.writeString(url);
    }
}
