package com.example.android.movies;

class MovieData {
    private String mTitle = " ";
    private String mReleaseDate = " ";
    private String mPosterPath = "about:blank";
    private String mVoteAverage = " ";
    private String mPlot = " ";
    private String mId = " ";

    public MovieData(String title, String releaseDate, String posterPath, String voteAverage, String plot, String id) {
        mTitle = title;
        mReleaseDate = releaseDate;
        mPosterPath = posterPath;
        mVoteAverage = voteAverage;
        mPlot = plot;
        mId = id;
    }

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

    public String getPlot() {
        return mPlot;
    }

    public String getId() {
        return mId;
    }

}
