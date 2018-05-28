package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import static com.example.android.movies.data.MovieContract.IS_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_PLOT;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POPULARITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.example.android.movies.data.MovieContract.MovieEntry.FAVORITE_PROJECTION;
import static com.example.android.movies.data.MovieContract.ORDER_BY_POPULAR;

public class MainActivity extends AppCompatActivity {

    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private ArrayList<MovieData> movieData = new ArrayList<>();
    private final String BASE_API_URL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TOP_RATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String API_KEY = ("?api_key=" + Keys.MOVIE_KEY);
    private static String url = "about:blank";
    private ProgressBar loadingIndicator;
    private String pUrl = (BASE_API_URL + POPULAR + API_KEY);
    private String ORDER_BY = ORDER_BY_POPULAR;

    private static final String TAG = MainActivity.class.getSimpleName();

    private GridLayoutManager gridLayoutManager;
    private RecyclerViewAdapter adapter;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private RecyclerView recyclerView;
    private String tUrl = (BASE_API_URL + TOP_RATED + API_KEY);

    private static final String KEY_POPULARITY = "popular";
    private static final String KEY_RATING = "top_rated";
    private static final String KEY_FAVORITE = "favorite";
    private static final String PREF_KEY = "sort";
    private TextView mEmptyStateTextView;

    public static final String KEY_SAVE_STATE = "save_state";

    private Parcelable parcelable;
    private final LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    Log.e(TAG, "onCreateLoader: fav");
                    loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);
                    return new CursorLoader(
                            MainActivity.this,
                            CONTENT_URI,
                            FAVORITE_PROJECTION,
                            IS_FAVORITED,
                            null,
                            ORDER_BY
                    );
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                    loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);
                    if (data.getCount() > 0) {
                        if (data.moveToFirst()) {
                            movieData = new ArrayList<>();
                            do {

                                String title = data.getString(data.getColumnIndex(COLUMN_TITLE));
                                String releaseDate = data.getString(data.getColumnIndex(COLUMN_RELEASE_DATE));
                                String posterPath = data.getString(data.getColumnIndex(COLUMN_POSTER_PATH));
                                String voteAverage = data.getString(data.getColumnIndex(COLUMN_VOTE_AVERAGE));
                                String popularity = data.getString(data.getColumnIndex(COLUMN_POPULARITY));
                                String plot = data.getString(data.getColumnIndex(COLUMN_PLOT));
                                String id = data.getString(data.getColumnIndex(COLUMN_ID));

                                movieData.add(new MovieData(title, releaseDate, posterPath, voteAverage, popularity, plot, id));
                            } while (data.moveToNext());
                        }
                    }
                    if (movieData.isEmpty()) {
                        Log.i(TAG, "favorite loader : no favorites");
                        // Set empty state text to display "There are no favorites"
                        mEmptyStateTextView.setText(R.string.no_favorites);
                        mEmptyStateTextView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        adapter.addAll(movieData);
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader loader) {
                    // Clear the adapter of previous movie data
                    adapter.clear();
                }

            };
    private final LoaderManager.LoaderCallbacks<ArrayList<MovieData>> movieLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<ArrayList<MovieData>>() {

                @Override
                public MovieLoader onCreateLoader(int i, Bundle bundle) {

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);

                    if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_POPULARITY)) {
                        url = pUrl;
                    } else if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_RATING)) {
                        url = tUrl;
                    }

                    Log.e(TAG, "onCreateLoader: " + url);

                    return new MovieLoader(MainActivity.this, url);
                }

                @Override
                public void onLoadFinished(@NonNull android.support.v4.content.Loader<ArrayList<MovieData>> loader, ArrayList<MovieData> data) {
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
                    adapter.clear();

                    // If there is a valid list of {@link Movies}, then add them to the adapter's
                    // data set. This will trigger the GridView to update.
                    if (data != null && !data.isEmpty()) {
                        movieData = data;
                        Log.e(TAG, "onLoadFinished: " + data.get(0).getTitle());
                        mEmptyStateTextView.setVisibility(View.GONE);
                        adapter.addAll(data);
                        recyclerView.setAdapter(adapter);
                        if (parcelable != null)
                            recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<ArrayList<MovieData>> loader) {
                    // Loader reset, so we can clear out our existing data.
                    adapter.clear();
                }

            };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState: ");
        parcelable = recyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(KEY_SAVE_STATE, parcelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        recyclerView = findViewById(R.id.recycler_view);
        gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new RecyclerViewAdapter(this, new ArrayList<MovieData>());

        mEmptyStateTextView = findViewById(R.id.empty_view);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        editor.apply();

        RecyclerViewAdapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (int) v.getTag();
                launchDetailActivity(pos);
            }
        });

        if (savedInstanceState == null) {
            Log.e(TAG, "onCreate: savedInstanceState is null");

            if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_POPULARITY)) {
                url = pUrl;
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                Log.e(TAG, "onCreate: popular");
            } else if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_RATING)) {
                url = tUrl;
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                Log.e(TAG, "onCreate: rating");
            } else if (sharedPreferences.getString(PREF_KEY, KEY_FAVORITE).equals(KEY_FAVORITE)) {
                getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
                Log.e(TAG, "onCreate: fav");
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.e(TAG, "onRestoreInstanceState: ");
        if (savedInstanceState != null) {

            if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_POPULARITY)) {
                url = pUrl;
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                Log.e(TAG, "onCreate: popular");
            } else if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_RATING)) {
                url = tUrl;
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                Log.e(TAG, "onCreate: rating");
            } else if (sharedPreferences.getString(PREF_KEY, KEY_FAVORITE).equals(KEY_FAVORITE)) {
                getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
                Log.e(TAG, "onCreate: fav");
            }

            parcelable = savedInstanceState.getParcelable(KEY_SAVE_STATE);
            recyclerView.getLayoutManager().onRestoreInstanceState(parcelable);
        }
    }

    protected void launchDetailActivity(int position) {
        if (!movieData.isEmpty()) {
            MovieData detailMovie = movieData.get(position);
            String mId = detailMovie.getId();
            //Create intent
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("title", detailMovie.getTitle());
            intent.putExtra("releaseDate", detailMovie.getReleaseDate());
            intent.putExtra("posterPath", detailMovie.getPosterPath());
            intent.putExtra("voteAverage", detailMovie.getVoteAverage());
            intent.putExtra("popularity", detailMovie.getPopularity());
            intent.putExtra("plot", detailMovie.getPlot());
            intent.putExtra("id", mId);
            //Start details activity
            startActivity(intent);

        }

    }

    private boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
//        getPrefs();
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//
        MenuItem favoritesItem = menu.findItem(R.id.favorites_only);
        MenuItem ratingItem = menu.findItem(R.id.sort_by_rating);
        MenuItem votesItem = menu.findItem(R.id.sort_by_votes);

        if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_POPULARITY)) {
            votesItem.setChecked(true);
        } else if (sharedPreferences.getString(PREF_KEY, KEY_POPULARITY).equals(KEY_RATING)) {
            ratingItem.setChecked(true);
        } else if (sharedPreferences.getString(PREF_KEY, KEY_FAVORITE).equals(KEY_FAVORITE)) {
            favoritesItem.setChecked(true);
        }

        Log.i(TAG, "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.favorites_only:
                Log.e(TAG, "onOptionsItemSelected: fav");
                editor.putString(PREF_KEY, KEY_FAVORITE);
                editor.apply();
                item.setChecked(true);
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, favoriteLoaderCallbacks);
                return true;

            case R.id.sort_by_rating:
                Log.e(TAG, "onOptionsItemSelected: rating");
                editor.putString(PREF_KEY, KEY_RATING);
                editor.apply();
                item.setChecked(true);
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                return true;

            case R.id.sort_by_votes:
                Log.e(TAG, "onOptionsItemSelected: popular");
                editor.putString(PREF_KEY, KEY_POPULARITY);
                editor.apply();
                item.setChecked(true);
                getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, movieLoaderCallbacks);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
}