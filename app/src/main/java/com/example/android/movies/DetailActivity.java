package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private final String BASEAPIURL = "http://api.themoviedb.org/3/movie/";
    private final String VIDEOS = "/videos";
    private final String REVIEWS = "/reviews";
    //API KEY REMOVED - get one at  https://www.themoviedb.org/account/signup
    private final String APIKEY = "?api_key=bf4f905b88823288bf4ac9bca4225847";
    ListView listView;
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
        final String reviewUrl = (BASEAPIURL + id + REVIEWS + APIKEY);
        final String trailerUrl = (BASEAPIURL + id + VIDEOS + APIKEY);
        String posterUrl = Utils.getPosterUrl(posterPath);
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

        Button reviewButton = findViewById(R.id.review_button);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    // Launch Review Activity with url for this movie
                    Intent reviewIntent = new Intent(DetailActivity.this, ReviewActivity.class);
                    reviewIntent.putExtra("QUERY", reviewUrl);
                    DetailActivity.this.startActivity(reviewIntent);

                } else {
                    loadingIndicator.setVisibility(View.GONE);
                    // Update empty state with no connection error message
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });

/*        Button trailerButton = findViewById(R.id.trailer_button);
        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()) {
                    // Launch Trailer Activity with url for this movie
                    Intent trailerIntent = new Intent(DetailActivity.this, TrailerActivity.class);
                    trailerIntent.putExtra("QUERY", trailerUrl);
                    DetailActivity.this.startActivity(trailerIntent);

                } else {
                    loadingIndicator.setVisibility(View.GONE);
                    // Update empty state with no connection error message
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });*/
/*        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle reviewBundle = new Bundle();
            reviewBundle.putString("QUERY", reviewUrl);
            Bundle videoBundle = new Bundle();
            videoBundle.putString("QUERY", videoUrl);
            getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, reviewBundle, this );
            getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, videoBundle, this);
            getSupportLoaderManager().restartLoader(REVIEW_LOADER_ID, reviewBundle, this);
            getSupportLoaderManager().restartLoader(VIDEOS_LOADER_ID, videoBundle, this);
        } else {

            // Clear the adapter of previous movie data
            mDataAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }*/


    }

    private boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void log(Object value)
    {
        System.out.println(value);
    }

}

