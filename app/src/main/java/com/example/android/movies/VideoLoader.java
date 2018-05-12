package com.example.android.movies;

import android.content.Context;

import java.util.List;

class VideoLoader extends android.support.v4.content.AsyncTaskLoader<List<Trailer>> {

    private final String mUrl;

    public VideoLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Trailer> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform network request, parse the response, and extract a list of videos
        return Utils.getVideos(mUrl);
    }
}
