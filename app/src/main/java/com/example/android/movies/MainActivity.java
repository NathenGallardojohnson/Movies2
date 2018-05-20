package com.example.android.movies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private static final String ORDER_BY_KEY = "orderby";
    private ArrayList<MovieData> movieData = new ArrayList<>();
    private final String BASE_API_URL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TOP_RATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String API_KEY = ("?api_key=" + Keys.MOVIE_KEY);
    private ProgressBar loadingIndicator;
    private String url = (BASE_API_URL + POPULAR + API_KEY);
    private String ORDER_BY = ORDER_BY_POPULAR;
    private boolean SHOW_FAVORITES = false;
    private TextView mEmptyStateTextView;
    private ArrayList<MovieData> favoritesData = new ArrayList<>();
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private GridLayoutManager gridLayoutManager;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        getLayoutManager();
        recyclerView.setLayoutManager(gridLayoutManager);

        mEmptyStateTextView = findViewById(R.id.empty_view);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        getPrefs();

        if (savedInstanceState != null) {
            movieData = savedInstanceState.getParcelableArrayList(MOVIE_KEY);
            favoritesData = savedInstanceState.getParcelableArrayList(FAVORITE_KEY);
            if (SHOW_FAVORITES) {
                adapter = new RecyclerViewAdapter(this, favoritesData);
            } else {
                adapter = new RecyclerViewAdapter(this, movieData);
            }
        } else {
            if (SHOW_FAVORITES) {
                getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
                adapter = new RecyclerViewAdapter(this, favoritesData);
            } else {
                sortBy();
                adapter = new RecyclerViewAdapter(this, movieData);
            }
        }

        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = recyclerView.indexOfChild(v);
                launchDetailActivity(pos);
            }
        });

        recyclerView.setAdapter(adapter);

    }

    private void getLayoutManager(){
        if (MainActivity.this.getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_PORTRAIT ){
            gridLayoutManager = new GridLayoutManager(this, 3);
        } else { gridLayoutManager = new GridLayoutManager(this, 5);}
    }

    protected void launchDetailActivity(int position){
        MovieData detailMovie = movieData.get(position);
        //Create intent
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("title", detailMovie.getTitle());
        intent.putExtra("releaseDate", detailMovie.getReleaseDate());
        intent.putExtra("posterPath", detailMovie.getPosterPath());
        intent.putExtra("voteAverage", detailMovie.getVoteAverage());
        intent.putExtra("popularity", detailMovie.getPopularity());
        intent.putExtra("plot", detailMovie.getPlot());
        intent.putExtra("id", detailMovie.getId());
        intent.putExtra("isFavorited", checkIfFavorited(detailMovie.getId()));

        //Start details activity
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPrefs();
        getLayoutManager();
        if (SHOW_FAVORITES) {
            getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
            adapter = new RecyclerViewAdapter(this, favoritesData);
        } else {
            sortBy();
            adapter = new RecyclerViewAdapter(this, movieData);
        }
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        setPrefs();
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
                    if (favoritesData.isEmpty()) {
                        loadingIndicator.setVisibility(View.GONE);
                        // Update empty state with no connection error message
                        mEmptyStateTextView.setText(R.string.no_favorites);
                        mEmptyStateTextView.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        adapter.addAll(favoritesData);
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

                    String url = bundle.getString("QUERY");

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);

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
                        mEmptyStateTextView.setVisibility(View.GONE);
                        adapter.addAll(data);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<ArrayList<MovieData>> loader) {
                    // Loader reset, so we can clear out our existing data.
                    adapter.clear();
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
                    return true;
                }

            case R.id.sort_by_rating:
                ORDER_BY = ORDER_BY_VOTE;
                url = (BASE_API_URL + TOP_RATED + API_KEY);
                setPrefs();
                if (!item.isChecked()) item.setChecked(true);
                if (SHOW_FAVORITES) {
                    getFavorites();
                } else sortBy();
                return true;

            case R.id.sort_by_votes:
                ORDER_BY = ORDER_BY_POPULAR;
                url = (BASE_API_URL + POPULAR + API_KEY);
                setPrefs();
                if (!item.isChecked()) item.setChecked(true);
                if (SHOW_FAVORITES) {
                    getFavorites();
                } else sortBy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

/*    private void showFavorites() {
        // Clear the adapter of previous movie data
        adapter.clear();

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
            adapter.addAll(favoritesData);
            adapter.notifyDataSetChanged();
        }
    }*/

    private void getFavorites() {
        getPrefs();
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
            adapter.clear();
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
/*
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }*/

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        getPrefs();
        savedInstanceState.putParcelableArrayList(MOVIE_KEY, movieData);
        savedInstanceState.putParcelableArrayList(FAVORITE_KEY, favoritesData);
        setPrefs();
        super.onSaveInstanceState(savedInstanceState);

        Log.i(LOG_TAG, "onSaveInstanceState");
    }
}