package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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
import static com.example.android.movies.data.MovieContract.ORDER_BY_VOTE;

public class MainActivity extends AppCompatActivity {

    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private static final String MOVIE_KEY = "movies";
    private static final String FAVORITE_KEY = "favorite";
    private static final String INDEX_KEY = "index";
    private static final String TOP_KEY = "top";
    private static final String ORDER_BY_KEY = "orderby";
    private static final String URL_KEY = "url";
    private List<MovieData> movieData = new ArrayList<>();
    private final String BASE_API_URL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TOP_RATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String API_KEY = ("?api_key=" + Keys.MOVIE_KEY);
    private GridViewAdapter gridAdapter;
    private ProgressBar loadingIndicator;
    private String url = (BASE_API_URL + POPULAR + API_KEY);
    private String ORDER_BY = ORDER_BY_POPULAR;
    private boolean SHOW_FAVORITES = false;
    private TextView mEmptyStateTextView;
    private List<MovieData> favoritesData = new ArrayList<>();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    static int index = 0;
    static int top = 0;
    private List<MovieData> initData = new ArrayList<>();
    private GridView gridView;
    private static final String LIST_STATE = "listState";
    private Parcelable mListState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        gridView = findViewById(R.id.gridView);
        mEmptyStateTextView = findViewById(R.id.empty_view);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            movieData = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
            favoritesData = savedInstanceState.getParcelableArrayList(FAVORITE_KEY);
            url = savedInstanceState.getString(URL_KEY);
            index = savedInstanceState.getInt(INDEX_KEY);
            getPrefs();
            if (SHOW_FAVORITES) {
                gridAdapter = new GridViewAdapter(this, initData);
                gridView.setAdapter(gridAdapter);
                if (favoritesData != null && !favoritesData.isEmpty()) {
                    gridView.setSelection(index);
                    gridAdapter.clear();
                    gridAdapter.addAll(favoritesData);
                    mEmptyStateTextView.setVisibility(View.GONE);
                    gridAdapter.notifyDataSetChanged();
                } else {
                    getFavorites();
                    showFavorites();
                }
            } else {
                gridAdapter = new GridViewAdapter(this, initData);
                gridView.setSelection(index);
                gridView.setAdapter(gridAdapter);
                // If there is a valid list of {@link Movies}, then add them to the adapter's
                // data set. This will trigger the GridView to update.
                if (movieData != null && !movieData.isEmpty()) {
                    mEmptyStateTextView.setVisibility(View.GONE);
                    gridAdapter.addAll(movieData);
                    gridAdapter.notifyDataSetChanged();
                } else {
                    sortBy();
                }
            }
        } else {
            gridAdapter = new GridViewAdapter(this, initData);
            gridView.setAdapter(gridAdapter);
            getPrefs();
            if (SHOW_FAVORITES) {
                getFavorites();
                showFavorites();
            } else sortBy();
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
                intent.putExtra("popularity", movieData.getPopularity());
                intent.putExtra("plot", movieData.getPlot());
                intent.putExtra("id", movieData.getId());
                intent.putExtra("isFavorited", checkIfFavorited(movieData.getId()));

                //Start details activity
                startActivity(intent);
            }
        });
        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPrefs();

        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPref != null) {
            SHOW_FAVORITES = sharedPref.getBoolean(FAVORITE_KEY, false);
            ORDER_BY = sharedPref.getString(ORDER_BY_KEY, ORDER_BY_POPULAR);
            Log.e(LOG_TAG, "onResume: " + ORDER_BY + " " + SHOW_FAVORITES);
            if (SHOW_FAVORITES) getFavorites();
        }
        if (mListState != null)
            gridView.onRestoreInstanceState(mListState);
        mListState = null;
        Log.i(LOG_TAG, "onResume");
        //gridView.setSelectionFromTop(index, top);

    }


    @Override
    protected void onPause() {
        setPrefs();
        //index = gridView.getFirstVisiblePosition();
        //View v = gridView.getChildAt(0);
        //top = (v == null) ? 0 : (v.getTop() - gridView.getPaddingTop());
        mListState = gridView.onSaveInstanceState();
        super.onPause();

        Log.i(LOG_TAG, "onPause");
    }

    private final LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {


                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    Log.e(LOG_TAG, "onCreateLoader: fav");
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

                    if (data.getCount() > 0) {
                        if (data.moveToFirst()) {
                            favoritesData = new ArrayList<>();
                            do {

                                String title = data.getString(data.getColumnIndex(COLUMN_TITLE));
                                String releaseDate = data.getString(data.getColumnIndex(COLUMN_RELEASE_DATE));
                                String posterPath = data.getString(data.getColumnIndex(COLUMN_POSTER_PATH));
                                String voteAverage = data.getString(data.getColumnIndex(COLUMN_VOTE_AVERAGE));
                                String popularity = data.getString(data.getColumnIndex(COLUMN_POPULARITY));
                                String plot = data.getString(data.getColumnIndex(COLUMN_PLOT));
                                String id = data.getString(data.getColumnIndex(COLUMN_ID));

                                favoritesData.add(new MovieData(title, releaseDate, posterPath, voteAverage, popularity, plot, id));
                            } while (data.moveToNext());
                        }
                    }
                    showFavorites();
                }

                @Override
                public void onLoaderReset(@NonNull Loader loader) {
                    // Clear the adapter of previous movie data
                    gridAdapter.clear();
                }

            };

    private final LoaderManager.LoaderCallbacks<List<MovieData>> movieLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<MovieData>>() {

                @Override
                public MovieLoader onCreateLoader(int i, Bundle bundle) {

                    String url = bundle.getString("QUERY");

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);

                    return new MovieLoader(MainActivity.this, url);
                }

                @Override
                public void onLoadFinished(@NonNull android.support.v4.content.Loader<List<MovieData>> loader, List<MovieData> data) {
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
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<MovieData>> loader) {
                    // Loader reset, so we can clear out our existing data.
                    gridAdapter.clear();
                }

            };

    private boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean checkIfFavorited(String id) {
        for (int i = 0; i < favoritesData.size(); i++) {
            MovieData mFavoriteData = favoritesData.get(i);
            if (mFavoriteData.getId().contains(id)) return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getPrefs();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem favoritesItem = menu.findItem(R.id.favorites_only);
        MenuItem ratingItem = menu.findItem(R.id.sort_by_rating);
        MenuItem votesItem = menu.findItem(R.id.sort_by_votes);

        if (SHOW_FAVORITES) favoritesItem.setChecked(true);
        else favoritesItem.setChecked(false);

        if (ORDER_BY.equals(ORDER_BY_POPULAR)) votesItem.setChecked(true);
        else ratingItem.setChecked(true);
        Log.i(LOG_TAG, "onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.favorites_only:
                if (item.isChecked()) {
                    item.setChecked(false);
                    SHOW_FAVORITES = false;
                    setPrefs();
                    sortBy();
                    return true;
                } else {
                    item.setChecked(true);
                    SHOW_FAVORITES = true;
                    setPrefs();
                    getFavorites();
                    showFavorites();
                    return true;
                }

            case R.id.sort_by_rating:
                ORDER_BY = ORDER_BY_VOTE;
                url = (BASE_API_URL + TOP_RATED + API_KEY);
                setPrefs();
                if (!item.isChecked()) item.setChecked(true);
                if (SHOW_FAVORITES) {
                    getFavorites();
                    showFavorites();
                } else sortBy();
                return true;

            case R.id.sort_by_votes:
                ORDER_BY = ORDER_BY_POPULAR;
                url = (BASE_API_URL + POPULAR + API_KEY);
                setPrefs();
                if (!item.isChecked()) item.setChecked(true);
                if (SHOW_FAVORITES) {
                    getFavorites();
                    showFavorites();
                } else sortBy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFavorites() {
        // Clear the adapter of previous movie data
        gridAdapter.clear();

        // If there is a valid list of {@link Movies}, then add them to the adapter's
        // data set. This will trigger the GridView to update.
        if (favoritesData.isEmpty()) {
            getFavorites();
            if (favoritesData.isEmpty()) {
                loadingIndicator.setVisibility(View.GONE);
                // Update empty state with no connection error message
                mEmptyStateTextView.setText(R.string.no_favorites);
                mEmptyStateTextView.setVisibility(View.VISIBLE);
            }
        } else {
            mEmptyStateTextView.setVisibility(View.GONE);
            gridAdapter.addAll(favoritesData);
            gridAdapter.notifyDataSetChanged();
        }
    }

    private void getFavorites() {
        getPrefs();

        //        getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
        getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
    }

    private void sortBy() {
        getPrefs();
        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle bundle = new Bundle();
            bundle.putString("QUERY", url);
            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, bundle, movieLoaderCallbacks);
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, bundle, movieLoaderCallbacks);
        } else {
            // Clear the adapter of previous movie data
            gridAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    private void getPrefs() {
        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPref != null) {
            SHOW_FAVORITES = sharedPref.getBoolean(FAVORITE_KEY, false);
            ORDER_BY = sharedPref.getString(ORDER_BY_KEY, ORDER_BY_POPULAR);
            if (ORDER_BY.equals(ORDER_BY_POPULAR)) {
                url = (BASE_API_URL + POPULAR + API_KEY);
            } else url = (BASE_API_URL + TOP_RATED + API_KEY);
        } else setPrefs();
    }

    private void setPrefs() {
        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FAVORITE_KEY, SHOW_FAVORITES);
        editor.putString(ORDER_BY_KEY, ORDER_BY);
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mListState = state.getParcelable(LIST_STATE);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        getPrefs();
        //index = gridView.getFirstVisiblePosition();
        //View v = gridView.getChildAt(0);
        //top = (v == null) ? 0 : (v.getTop() - gridView.getPaddingTop());
        savedInstanceState.putParcelableArrayList(MOVIE_KEY,
                (ArrayList<? extends Parcelable>) movieData);
        savedInstanceState.putParcelableArrayList(FAVORITE_KEY,
                (ArrayList<? extends Parcelable>) favoritesData);
        savedInstanceState.putString(URL_KEY, url);
        savedInstanceState.putInt(INDEX_KEY, index);
        savedInstanceState.putInt(TOP_KEY, top);
        setPrefs();
        mListState = gridView.onSaveInstanceState();
        savedInstanceState.putParcelable(LIST_STATE, mListState);
        super.onSaveInstanceState(savedInstanceState);

        Log.i(LOG_TAG, "onSaveInstanceState");
    }
}