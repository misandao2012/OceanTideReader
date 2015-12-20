package jian.zhang.oceantidereader.domainobjects;

import android.os.Parcel;
import android.os.Parcelable;

public class Station implements Parcelable {
    private long mId;
    private String mStateName;
    private String mStationId;
    private String mName;
    private String mFavorite;

    public Station(){}

    public Station(Parcel in) {
        readFromParcel(in);
    }

    public String getStateName() {
        return mStateName;
    }

    public void setStateName(String stateName) {
        mStateName = stateName;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getStationId() {
        return mStationId;
    }

    public void setStationId(String stationId) {
        mStationId = stationId;
    }

    public String getFavorite() {
        return mFavorite;
    }

    public void setFavorite(String favorite) {
        this.mFavorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeString(mStateName);
        dest.writeString(mStationId);
        dest.writeString(mName);
        dest.writeString(mFavorite);
    }

    private void readFromParcel(Parcel in) {
        mId = in.readLong();
        mStateName = in.readString();
        mStationId = in.readString();
        mName = in.readString();
        mFavorite = in.readString();
    }

    public static final Parcelable.Creator<Station> CREATOR =
            new Parcelable.Creator<Station>() {
                @Override
                public Station createFromParcel(Parcel in) {
                    return new Station(in);
                }

                @Override
                public Station[] newArray(int size) {
                    return new Station[size];
                }
            };

}
