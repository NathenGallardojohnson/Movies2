package com.example.android.movies;

import android.content.Context;

import java.util.List;

/**
 * Created by Hardkornate on 3/22/18.
 */

class VideoLoader extends android.support.v4.content.AsyncTaskLoader<List<Data>> {

    private String mUrl = "about:blank";

    public VideoLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Data> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform network request, parse the response, and extract a list of videos
        return Utils.getVideos(mUrl);
    }
}
