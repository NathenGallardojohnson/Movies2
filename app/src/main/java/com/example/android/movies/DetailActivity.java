package com.example.android.movies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import static com.example.android.movies.Keys.MOVIE_KEY;
import static com.example.android.movies.data.MovieContract.BASE_CONTENT_URI;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_PLOT;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POPULARITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.movies.data.MovieContract.PATH_MOVIES;
import static com.example.android.movies.data.MovieContract.PATH_MOVIE_DELETE;

public class DetailActivity extends AppCompatActivity {

    static String id;

    private final String BASE_API_URL = "http://api.themoviedb.org/3/movie/";
    private final String VIDEOS = "/videos";
    private final String REVIEWS = "/reviews";
    static String plot;
    private TextView mEmptyStateTextView;
    private ProgressBar loadingIndicator;
    static boolean isFavorited;
    static String popularity;
    static String title;
    static String releaseDate;
    static String posterPath;
    static String voteAverage;
    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    //API KEY REMOVED - get one at  https://www.themoviedb.org/account/signup
    private final String API_KEY = ("?api_key=" + MOVIE_KEY);

    ToggleButton favoritesButton;
    private ContentResolver contentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);

        final Bundle args = getIntent().getExtras();

        title = args.getString("title");
        releaseDate = args.getString("releaseDate");
        posterPath = args.getString("posterPath");
        voteAverage = args.getString("voteAverage");
        popularity = args.getString("popularity");
        plot = args.getString("plot");
        id = args.getString("id");
        isFavorited = args.getBoolean("isFavorited");

        final String reviewUrl = (BASE_API_URL + id + REVIEWS + API_KEY);
        final String trailerUrl = (BASE_API_URL + id + VIDEOS + API_KEY);

        setViews();

        contentResolver = DetailActivity.this.getContentResolver();

        favoritesButton = findViewById(R.id.favorite_button);
        favoritesButton.setChecked(isFavorited);
        if (isFavorited) {
            favoritesButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
        } else {
            favoritesButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
        }

        favoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFavorited) {
                    String selection = (PATH_MOVIE_DELETE + id);
                    Uri uri = Uri.withAppendedPath(BASE_CONTENT_URI, selection);
                    if (contentResolver.delete(uri, null, null) != 0) {
                        isFavorited = false;
                        favoritesButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_off));
                    }
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(COLUMN_ID, id);
                    contentValues.put(COLUMN_FAVORITED, "true");
                    contentValues.put(COLUMN_TITLE, title);
                    contentValues.put(COLUMN_RELEASE_DATE, releaseDate);
                    contentValues.put(COLUMN_POSTER_PATH, posterPath);
                    contentValues.put(COLUMN_VOTE_AVERAGE, voteAverage);
                    contentValues.put(COLUMN_POPULARITY, popularity);
                    contentValues.put(COLUMN_PLOT, plot);

                    Uri uri = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIES);
                    Uri newFavorite = contentResolver.insert(uri, contentValues);
                    if (newFavorite != null) Log.e(LOG_TAG, newFavorite.toString());

                    isFavorited = true;
                    favoritesButton.setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), android.R.drawable.btn_star_big_on));
                }
            }
        });


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

        Button trailerButton = findViewById(R.id.trailer_button);
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
        });
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setViews() {
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(title);

        TextView releaseTextView = findViewById(R.id.release_date);
        releaseTextView.setText(releaseDate);

        TextView voteAverageTextView = findViewById(R.id.vote);
        voteAverageTextView.setText(voteAverage);

        TextView plotTextView = findViewById(R.id.plot);
        plotTextView.setText(plot);

        ImageView imageView = findViewById(R.id.image);
        String posterUrl = Utils.getPosterUrl(posterPath);
        Picasso.with(this).load(posterUrl).into(imageView);
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        title = savedInstanceState.getString("title");
        releaseDate = savedInstanceState.getString("releaseDate");
        posterPath = savedInstanceState.getString("posterPath");
        voteAverage = savedInstanceState.getString("voteAverage");
        popularity = savedInstanceState.getString("popularity");
        plot = savedInstanceState.getString("plot");
        id = savedInstanceState.getString("id");
        isFavorited = savedInstanceState.getBoolean("isFavorited");

        setViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", title);
        outState.putString("releaseDate", releaseDate);
        outState.putString("posterPath", posterPath);
        outState.putString("voteAverage", voteAverage);
        outState.putString("popularity", popularity);
        outState.putString("plot", plot);
        outState.putString("id", id);
        outState.putBoolean("isFavorited", isFavorited);
    }

}

