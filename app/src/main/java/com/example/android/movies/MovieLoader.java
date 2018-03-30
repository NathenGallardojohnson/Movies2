package com.example.android.movies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

class MovieLoader extends AsyncTaskLoader<List<MovieData>> {

    private String mUrl = "about:blank";

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
