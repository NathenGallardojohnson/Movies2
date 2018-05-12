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

import static com.example.android.movies.data.MovieContract.FAVORITED;
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

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_LOADER_ID = 2;
    private static final String MOVIE_KEY = "movies";
    private static final String FAVORITE_KEY = "favorite";
    private static final String ORDER_BY_KEY = "orderby";
    private static final String URL_KEY = "url";
    private List<MovieData> movieData = new ArrayList<>();
    private final String BASE_API_URL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    private final String TOP_RATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String API_KEY = ("?api_key=" + Keys.MOVIE_KEY);
    private GridViewAdapter gridAdapter;
    private ProgressBar loadingIndicator;
    private String url = (BASE_API_URL + POPULAR + API_KEY);
    private String ORDER_BY = null;
    private boolean SHOW_FAVORITES = false;
    private TextView mEmptyStateTextView;
    private List<MovieData> favoritesData = new ArrayList<>();

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

        getFavorites();

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
                intent.putExtra("popularity", movieData.getPopularity());
                intent.putExtra("plot", movieData.getPlot());
                intent.putExtra("id", movieData.getId());
                intent.putExtra("isFavorited", checkIfFavorited(movieData.getId()));

                //Start details activity
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPrefs();
        getFavorites();
        if (SHOW_FAVORITES) {
            showFavorites();
        } else {
            sortBy();
        }
    }

    @Override
    protected void onPause() {
        setPrefs();
        super.onPause();
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

    private LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
                    return new CursorLoader(
                            MainActivity.this,
                            CONTENT_URI,
                            FAVORITE_PROJECTION,
                            IS_FAVORITED,
                            FAVORITED,
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
                    data.close();
                }

                @Override
                public void onLoaderReset(@NonNull Loader loader) {
                    // Clear the adapter of previous movie data
                    gridAdapter.clear();
                }

            };

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

        MenuItem sortItem;
        MenuItem favBox = findViewById(R.id.favorites_only);

        if (ORDER_BY.equals(ORDER_BY_POPULAR)) sortItem = findViewById(R.id.sort_by_votes);
        else sortItem = findViewById(R.id.sort_by_rating);

        sortItem.setChecked(true);
        favBox.setChecked(SHOW_FAVORITES);
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
                if (item.isChecked()) {
                    item.setChecked(false);
                    url = (BASE_API_URL + POPULAR + API_KEY);
                    ORDER_BY = ORDER_BY_POPULAR;
                    sortBy();
                } else {
                    item.setChecked(true);
                    url = (BASE_API_URL + TOP_RATED + API_KEY);
                    ORDER_BY = ORDER_BY_VOTE;
                    sortBy();
                }
                setPrefs();
                return true;
            case R.id.sort_by_votes:
                if (item.isChecked()) {
                    item.setChecked(false);
                    url = (BASE_API_URL + TOP_RATED + API_KEY);
                    ORDER_BY = ORDER_BY_VOTE;
                    sortBy();
                } else {
                    item.setChecked(true);
                    url = (BASE_API_URL + POPULAR + API_KEY);
                    ORDER_BY = ORDER_BY_POPULAR;
                    sortBy();
                }
                setPrefs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean showFavorites() {
        // Clear the adapter of previous movie data
        gridAdapter.clear();

        // If there is a valid list of {@link Movies}, then add them to the adapter's
        // data set. This will trigger the GridView to update.
        if (favoritesData.isEmpty()) {
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_favorites);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            return false;
        } else {
            mEmptyStateTextView.setVisibility(View.GONE);
            gridAdapter.addAll(favoritesData);
            gridAdapter.notifyDataSetChanged();
            return true;
        }
    }

    private void getFavorites() {
        getPrefs();

        getSupportLoaderManager().initLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
        getSupportLoaderManager().restartLoader(FAVORITE_LOADER_ID, null, favoriteLoaderCallbacks);
    }

    private boolean sortBy() {
        getPrefs();
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

    private void getPrefs() {
        Context context = MainActivity.this;
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (sharedPref != null) {
            SHOW_FAVORITES = sharedPref.getBoolean(FAVORITE_KEY, SHOW_FAVORITES);
            ORDER_BY = sharedPref.getString(ORDER_BY_KEY, ORDER_BY_POPULAR);
        }
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_KEY, (ArrayList<? extends Parcelable>) movieData);
        outState.putString(URL_KEY, url);
        setPrefs();
        super.onSaveInstanceState(outState);
    }
}