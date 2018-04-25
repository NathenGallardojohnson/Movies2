package com.example.android.movies;

import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.movies.data.MovieContract.FAVORITED;
import static com.example.android.movies.data.MovieContract.IS_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_PLOT;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POPULARITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_REVIEW_QUANTITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TRAILER_QUANTITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.example.android.movies.data.MovieContract.MovieEntry.FAVORITE_PROJECTION;
import static com.example.android.movies.data.MovieContract.ORDER_BY_POPULAR;
import static com.example.android.movies.data.MovieContract.ORDER_BY_VOTE;

public class MainActivity extends AppCompatActivity /*implements SharedPreferences.OnSharedPreferenceChangeListener*/ {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private static final String MOVIE_KEY = "movies";
    private static final String FAVORITE_KEY = "favorite";
    private static final String ORDER_BY_KEY = "orderby";
    private static final String URL_KEY = "url";
    private List<MovieData> movieData = new ArrayList<>();
    private final String BASEAPIURL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    private final String TOPRATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String APIKEY = "?api_key=bf4f905b88823288bf4ac9bca4225847";
    private GridViewAdapter gridAdapter;
    private View loadingIndicator;
    private String url = (BASEAPIURL + POPULAR + APIKEY);
    private String ORDERBY = null;
    private boolean SHOWFAVORITES = false;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        GridView gridView = findViewById(R.id.gridView);
        mEmptyStateTextView = findViewById(R.id.empty_view);

        gridAdapter = new GridViewAdapter(this, movieData);
        gridView.setAdapter(gridAdapter);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        if (isOnline()) {
            sortBy();
        } else {
            showFavorites();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                MovieData movieData = (MovieData) parent.getItemAtPosition(position);
                //Create intent
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("title", movieData.getTitle());
                intent.putExtra("releaseDate", movieData.getReleaseDate());
                intent.putExtra("posterPath", movieData.getPosterPath());
                intent.putExtra("voteAverage", movieData.getVoteAverage());
                intent.putExtra("plot", movieData.getPlot());
                intent.putExtra("id", movieData.getId());
                intent.putExtra("reviewQuantity", movieData.getReviewQuantity());
                intent.putExtra("trailerQuantity", movieData.getTrailerQuantity());

                //Start details activity
                startActivity(intent);
            }
        });


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

    private LoaderManager.LoaderCallbacks<List<MovieData>> movieLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<MovieData>>() {

                @Override
                public MovieLoader onCreateLoader(int i, Bundle bundle) {

                    String url = bundle.getString("QUERY");

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);

                    return new MovieLoader(MainActivity.this, url);
                }

                @Override
                public void onLoadFinished(android.support.v4.content.Loader<List<MovieData>> loader, List<MovieData> data) {
                    // Hide loading indicator because the data has been loaded
                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);

                    // Set empty state text to display "There are no movies"
                    mEmptyStateTextView.setText(R.string.no_movies);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);

                    if (!isOnline()) {
                        mEmptyStateTextView.setText(R.string.no_internet_connection);
                    }

                    // Clear the adapter of previous movie data
                    gridAdapter.clear();

                    // If there is a valid list of {@link Movies}, then add them to the adapter's
                    // data set. This will trigger the GridView to update.
                    if (data != null && !data.isEmpty()) {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        gridAdapter.addAll(data);
                        gridAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onLoaderReset(android.support.v4.content.Loader<List<MovieData>> loader) {
                    // Loader reset, so we can clear out our existing data.
                    gridAdapter.clear();
                }

            };

    private LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    ORDERBY = bundle.getString(ORDER_BY_KEY);
                    mEmptyStateTextView.setVisibility(View.GONE);
                    return new android.support.v4.content.CursorLoader(
                            MainActivity.this,
                            CONTENT_URI,
                            FAVORITE_PROJECTION,
                            IS_FAVORITED,
                            FAVORITED,
                            ORDERBY
                    );
                }

                @Override
                public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
                    movieData = new ArrayList<>();

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);


                    mEmptyStateTextView.setVisibility(View.VISIBLE);

                    if (data == null) {
                        mEmptyStateTextView.setText(R.string.no_favorites);
                    } else {
                        loadingIndicator.setVisibility(View.GONE);
                        data.moveToFirst();
                        while (data.moveToNext()) {
                            String title = data.getString(data.getColumnIndex(COLUMN_TITLE));
                            String releaseDate = data.getString(data.getColumnIndex(COLUMN_RELEASE_DATE));
                            String posterPath = data.getString(data.getColumnIndex(COLUMN_POSTER_PATH));
                            String voteAverage = data.getString(data.getColumnIndex(COLUMN_VOTE_AVERAGE));
                            String popularity = data.getString(data.getColumnIndex(COLUMN_POPULARITY));
                            String plot = data.getString(data.getColumnIndex(COLUMN_PLOT));
                            String id = data.getString(data.getColumnIndex(COLUMN_ID));
                            int reviewQuantity = data.getInt(data.getColumnIndex(COLUMN_REVIEW_QUANTITY));
                            int trailerQuantity = data.getInt(data.getColumnIndex(COLUMN_TRAILER_QUANTITY));

                            movieData.add(new MovieData(title, releaseDate, posterPath, voteAverage, popularity, plot, id,
                                    reviewQuantity, trailerQuantity));
                        }
                        // Clear the adapter of previous movie data
                        gridAdapter.clear();

                        // If there is a valid list of {@link Movies}, then add them to the adapter's
                        // data set. This will trigger the GridView to update.
                        if (movieData != null && !movieData.isEmpty()) {
                            mEmptyStateTextView.setVisibility(View.GONE);
                            gridAdapter.addAll(movieData);
                            gridAdapter.notifyDataSetChanged();
                        }

                    }

                }

                @Override
                public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
                    // Clear the adapter of previous movie data
                    gridAdapter.clear();
                }

            };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.favorites_only:
                if (item.isChecked()) {
                    sortBy();
                    item.setChecked(false);
                    SHOWFAVORITES = false;
                    return true;
                } else {
                    showFavorites();
                    item.setChecked(true);
                    SHOWFAVORITES = true;
                    return true;
                }
            case R.id.sort_by_rating:
                if (item.isChecked()) {
                    item.setChecked(false);
                    url = (BASEAPIURL + POPULAR + APIKEY);
                    ORDERBY = ORDER_BY_POPULAR;
                } else {
                    item.setChecked(true);
                    url = (BASEAPIURL + TOPRATED + APIKEY);
                    ORDERBY = ORDER_BY_VOTE;
                }
                return true;
            case R.id.sort_by_votes:
                if (item.isChecked()) {
                    item.setChecked(false);
                    url = (BASEAPIURL + TOPRATED + APIKEY);
                    ORDERBY = ORDER_BY_VOTE;
                } else {
                    item.setChecked(true);
                    url = (BASEAPIURL + POPULAR + APIKEY);
                    ORDERBY = ORDER_BY_POPULAR;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean showFavorites() {
        if (isOnline()) {
            Bundle bundle = new Bundle();
            bundle.putString(ORDER_BY_KEY, ORDERBY);
            getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, bundle, favoriteLoaderCallbacks);
            getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, bundle, favoriteLoaderCallbacks);
            return true;
        } else {
            // Clear the adapter of previous movie data
            gridAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
            return false;
        }
    }

    private boolean sortBy() {
        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle bundle = new Bundle();
            bundle.putString("QUERY", url);
            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, bundle, movieLoaderCallbacks);
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, bundle, movieLoaderCallbacks);
            return true;
        } else {
            // Clear the adapter of previous movie data
            gridAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
            return false;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, (ArrayList<? extends Parcelable>) movieData);
        outState.putBoolean(FAVORITE_KEY, SHOWFAVORITES);
        outState.putString(ORDER_BY_KEY, ORDERBY);
        outState.putString(URL_KEY, url);
        super.onSaveInstanceState(outState);
    }
}