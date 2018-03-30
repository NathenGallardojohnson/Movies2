package com.example.android.movies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
    private static final int REVIEW_LOADER_ID = 2;
    private static final int VIDEOS_LOADER_ID = 3;
    private final String BASEAPIURL = "http://api.themoviedb.org/3/movie/";
    private final String VIDEOS = "/videos";
    private final String REVIEWS = "/reviews";
    //API KEY REMOVED - get one at  https://www.themoviedb.org/account/signup
    private final String APIKEY = " ";
    VideoAdapter videoAdapter;
    ReviewAdapter reviewAdapter;
    ListView listView;
    private List<Data> reviewData = new ArrayList<>();
    private List<Data> videoData = new ArrayList<>();
    private TextView mEmptyStateTextView;
    private ImageView loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);

        String title = getIntent().getStringExtra("title");
        String releaseDate = getIntent().getStringExtra("releaseDate");
        String posterPath = getIntent().getStringExtra("posterPath");
        String voteAverage = getIntent().getStringExtra("voteAverage");
        String plot = getIntent().getStringExtra("plot");
        String id = getIntent().getStringExtra("id");
        String reviewUrl = (BASEAPIURL + id + REVIEWS + APIKEY);
        String videoUrl = (BASEAPIURL + id + VIDEOS + APIKEY);
        String posterUrl = Utils.getPosterUrl(posterPath);
        videoAdapter = new VideoAdapter(this, null);
        reviewAdapter = new ReviewAdapter(this, null);
        listView = findViewById(R.id.list_view);
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(title);
        TextView releaseTextView = findViewById(R.id.release_date);
        releaseTextView.setText(releaseDate);
        TextView voteAverageTextView = findViewById(R.id.vote);
        voteAverageTextView.setText(voteAverage);
        TextView plotTextView = findViewById(R.id.plot);
        plotTextView.setText(plot);
        ImageView imageView = findViewById(R.id.image);
        Picasso.with(this).load(posterUrl).into(imageView);
        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle reviewBundle = new Bundle();
            reviewBundle.putString("QUERY", reviewUrl);
            Bundle videoBundle = new Bundle();
            videoBundle.putString("QUERY", videoUrl);
            getLoaderManager().initLoader(REVIEW_LOADER_ID, reviewBundle, this);
            getLoaderManager().initLoader(VIDEOS_LOADER_ID, videoBundle, this);
            getLoaderManager().restartLoader(REVIEW_LOADER_ID, reviewBundle, this);
            getLoaderManager().restartLoader(VIDEOS_LOADER_ID, videoBundle, this);
        } else {

            // Clear the adapter of previous movie data
            videoAdapter.clear();
            reviewAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

    }

    private boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        String url = bundle.getString("QUERY");


        //loadingIndicator.setVisibility(View.VISIBLE);

        if (id == REVIEW_LOADER_ID) {
            return new ReviewLoader(this, url);
        } else if (id == VIDEOS_LOADER_ID) {

            return new VideoLoader(this, url);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        // Hide loading indicator because the data has been loaded
        //loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "There are no reviews"
        //mEmptyStateReviewTextView.setText(R.string.no_reviews);
        //mEmptyStateReviewTextView.setVisibility(View.VISIBLE);

        // Set empty state text to display "There are no videos"
        //mEmptyStateVideoTextView.setText(R.string.no_videos);
        //mEmptyStateVideoTextView.setVisibility(View.VISIBLE);

        if (!isOnline()) {
            //mEmptyStateVideoTextView.setText(R.string.no_internet_connection);
            //mEmptyStateReviewTextView.setText(R.string.no_internet_connection);
        }
        int id = loader.getId();// find which loader you called
        if (data != null) {
            if (id == REVIEW_LOADER_ID) {
                reviewData = (List<Data>) data;
                // Clear the adapter of previous review data
                try {
                    reviewAdapter.clear();
                }
                catch (java.lang.NullPointerException exception){
                    // Catch NullPointerExceptions.
                    log(exception);
                }
                // If there is a valid list of {@link Reviews}, then add them to the adapter's
                // data set. This will trigger the ListView to update.
                if (reviewData != null && !reviewData.isEmpty()) {
                    //mEmptyStateReviewTextView.setVisibility(View.GONE);
                    try {
                        reviewAdapter.addAll(reviewData);
                        reviewAdapter.notifyDataSetChanged();
                        listView.setAdapter(reviewAdapter);
                    }
                    catch (java.lang.NullPointerException exception){
                        // Catch NullPointerExceptions.
                        log(exception);
                    }

                }
            } else if (id == VIDEOS_LOADER_ID) {
                videoData = (List<Data>) data;
                // Clear the adapter of previous video data
                try {
                    videoAdapter.clear();
                }
            catch (java.lang.NullPointerException exception){
                // Catch NullPointerExceptions.
                log(exception);
            }

                // If there is a valid list of {@link Videos}, then add them to the adapter's
                // data set. This will trigger the ListView to update.
                if (videoData != null && !videoData.isEmpty()) {
                    // mEmptyStateReviewTextView.setVisibility(View.GONE);
                    try {
                        videoAdapter.addAll(videoData);
                        videoAdapter.notifyDataSetChanged();
                        listView.setAdapter(videoAdapter);
                    }
                catch (java.lang.NullPointerException exception){
                        // Catch NullPointerExceptions.
                        log(exception);
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        int id = loader.getId();
        if (id == REVIEW_LOADER_ID) {
            try {
                reviewAdapter.clear();
            } catch (java.lang.NullPointerException exception) {
                // Catch NullPointerExceptions.
                log(exception);
            }
        } else if (id == VIDEOS_LOADER_ID) {
            try {
                videoAdapter.clear();
            } catch (java.lang.NullPointerException exception) {
                // Catch NullPointerExceptions.
                log(exception);
            }
        }
    }

    public static void log(Object value)
    {
        System.out.println(value);
    }

}

