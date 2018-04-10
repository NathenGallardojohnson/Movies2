package com.example.android.movies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by Hardkornate on 3/22/18.
 */

class ReviewLoader extends android.support.v4.content.AsyncTaskLoader<List<Data>> {

    private String mUrl = "about:blank";

    public ReviewLoader(Context context, String url) {
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

        // Perform network request, parse the response, and extract a list of reviews
        return Utils.getReviews(mUrl);
    }
}
