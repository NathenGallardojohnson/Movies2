package com.example.android.movies;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable{
    private String mTitle = " ";
    private String mReleaseDate = " ";
    private String mPosterPath;
    private String mVoteAverage = " ";
    private String mPopularity = " ";
    private String mPlot = " ";
    private String mId;

    public MovieData(String title, String releaseDate, String posterPath, String voteAverage,
                     String popularity, String plot, String id) {
        mTitle = title;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mVoteAverage = voteAverage;
        mPopularity = popularity;
        mPlot = plot;
        mId = id;
    }

    public MovieData(String posterPath, String id){
        mPosterPath = posterPath;
        mId = id;
    }

    public MovieData(Parcel parcel){
        mTitle = parcel.readString();
        mReleaseDate = parcel.readString();
        mPosterPath = parcel.readString();
        mVoteAverage = parcel.readString();
        mPopularity = parcel.readString();
        mPlot = parcel.readString();
        mId = parcel.readString();
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    public String getTitle() {
        return mTitle;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getPosterPath() {
        return mPosterPath;
    }

    public String getVoteAverage() {
        return mVoteAverage;
    }

    public String getPopularity(){
        return mPopularity;
    }

    public String getPlot() {
        return mPlot;
    }

    public String getId() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mReleaseDate);
        dest.writeString(mPosterPath);
        dest.writeString(mVoteAverage);
        dest.writeString(mPopularity);
        dest.writeString(mPlot);
        dest.writeString(mId);
    }
}
