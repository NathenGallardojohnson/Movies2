package com.example.android.movies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<MovieData>> {

    private static final int MOVIE_LOADER_ID = 1;
    private final List<MovieData> movieData = new ArrayList<>();
    private final String BASEAPIURL = "http://api.themoviedb.org/3/movie";
    private final String POPULAR = "/popular";
    private final String TOPRATED = "/top_rated";
    //API KEY REMOVED - get one at https://www.themoviedb.org/account/signup
    private final String APIKEY = "?api_key=bf4f905b88823288bf4ac9bca4225847";
    private GridViewAdapter gridAdapter;
    private View loadingIndicator;
    private String url = (BASEAPIURL + POPULAR + APIKEY);
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch switchButton = findViewById(R.id.switchOne);


        GridView gridView = findViewById(R.id.gridView);
        mEmptyStateTextView = findViewById(R.id.empty_view);

        gridAdapter = new GridViewAdapter(this, movieData);
        gridView.setAdapter(gridAdapter);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);


        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle bundle = new Bundle();
            bundle.putString("QUERY", url);
            getLoaderManager().initLoader(MOVIE_LOADER_ID, bundle, this);
            getLoaderManager().restartLoader(MOVIE_LOADER_ID, bundle, this);
        } else {

            // Clear the adapter of previous movie data
            gridAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    Toast.makeText(getApplicationContext(), R.string.switch_popular, Toast.LENGTH_SHORT).show();
                    url = (BASEAPIURL + POPULAR + APIKEY);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.switch_rating, Toast.LENGTH_SHORT).show();
                    url = (BASEAPIURL + TOPRATED + APIKEY);
                }
                if (isOnline()) {
                    // Get a reference to the LoaderManager, in order to interact with loaders
                    Bundle bundle = new Bundle();
                    bundle.putString("QUERY", url);
                    getLoaderManager().restartLoader(MOVIE_LOADER_ID, bundle, MainActivity.this);
                } else {

                    // Clear the adapter of previous movie data
                    gridAdapter.clear();
                    loadingIndicator.setVisibility(View.GONE);
                    // Update empty state with no connection error message
                    mEmptyStateTextView.setText(R.string.no_internet_connection);
                }
            }
        });

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

                //Start details activity
                startActivity(intent);
            }
        });


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
    public Loader<List<MovieData>> onCreateLoader(int i, Bundle bundle) {

        String url = bundle.getString("QUERY");

        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);

        return new MovieLoader(this, url);
    }


    @Override
    public void onLoadFinished(Loader<List<MovieData>> loader, List<MovieData> data) {
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
    public void onLoaderReset(Loader<List<MovieData>> loader) {
        // Loader reset, so we can clear out our existing data.
        gridAdapter.clear();
    }
}