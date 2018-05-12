package com.example.android.movies;

import android.content.Context;

import java.util.List;

class MovieLoader extends android.support.v4.content.AsyncTaskLoader<List<MovieData>> {

    private final String mUrl;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<MovieData> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform network request, parse the response, and extract a list of movies
        return Utils.getData(mUrl);
    }
}
