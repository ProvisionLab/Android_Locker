package com.dev.joks.lockscreen;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Evgeniy on 13-Feb-18.
 */

public class Time implements Parcelable {

    private int hours;
    private int minutes;
    private int seconds;

    public Time() {
    }

    public Time(int hours, int minutes, int seconds) {
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    protected Time(Parcel in) {
        hours = in.readInt();
        minutes = in.readInt();
        seconds = in.readInt();
    }

    public static final Creator<Time> CREATOR = new Creator<Time>() {
        @Override
        public Time createFromParcel(Parcel in) {
            return new Time(in);
        }

        @Override
        public Time[] newArray(int size) {
            return new Time[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hours);
        dest.writeInt(minutes);
        dest.writeInt(seconds);
    }
}
