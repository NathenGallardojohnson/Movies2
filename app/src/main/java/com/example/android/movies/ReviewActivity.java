package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class ReviewActivity extends AppCompatActivity {

    private static final int REVIEW_LOADER_ID = 3;
    ListView listView;
    private TextView mEmptyStateTextView;
    private View loadingIndicator;
    private ReviewAdapter reviewAdapter;
    private LoaderManager.LoaderCallbacks<List<Review>> reviewLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Review>>() {

                @NonNull
                @Override
                public ReviewLoader onCreateLoader(int id, @Nullable Bundle args) {
                    String url = args.getString("QUERY");
                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);
                    return new ReviewLoader(ReviewActivity.this, url);
                }

                @Override
                public void onLoadFinished(@NonNull android.support.v4.content.Loader<List<Review>> loader, List<Review> data) {
                    // Hide loading indicator because the data has been loaded
                    loadingIndicator.setVisibility(View.GONE);

                    // Set empty state text to display "There are no reviews"
                    mEmptyStateTextView.setText(R.string.no_reviews);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);

                    //Check connectivity and display error message if needed
                    if (!isOnline()) {
                        mEmptyStateTextView.setText(R.string.no_internet_connection);
                    }

                    // Clear the adapter of previous movie data
                    //reviewAdapter.clear();

                    // If there is a valid list of {@link Review}, then add them to the adapter's
                    // data set. This will trigger the ListView to update.
                    if (data != null && !data.isEmpty()) {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        reviewAdapter = new ReviewAdapter(getApplicationContext(), data);
                        reviewAdapter.setReviews();
                        listView.setAdapter(reviewAdapter);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<Review>> loader) {
                    // Clear the adapter of previous movie data
                    reviewAdapter.clear();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        listView = findViewById(R.id.review_list_view);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        Intent intent = getIntent();
        String review_url = intent.getStringExtra("QUERY");
        Bundle reviewBundle = new Bundle();
        reviewBundle.putString("QUERY", review_url);
        if (isOnline()) {
            getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, reviewBundle, reviewLoaderCallbacks);
        } else {
            // Hide loading indicator because the data cannot be loaded
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    protected boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }
}
