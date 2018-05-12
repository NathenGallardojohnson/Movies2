package com.example.android.movies;

import android.app.Dialog;
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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TrailerActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener {

    private static final int TRAILER_LOADER_ID = 4;

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static Trailer trailer;
    private static final Bundle bundle = new Bundle();
    private TrailerGridAdapter trailerGridAdapter;
    private GridView gridView;
    private YouTubePlayerFragment playerFragment;
    private final List<Trailer> trailers = new ArrayList<>();
    private View playerView;
    private YouTubePlayer player;
    private TextView mEmptyStateTextView;
    private ProgressBar loadingIndicator;
    private Dialog errorDialog;
    private State state;
    private final LoaderManager.LoaderCallbacks<List<Trailer>> trailerLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Trailer>>() {
                @NonNull
                @Override
                public VideoLoader onCreateLoader(int id, @Nullable Bundle args) {
                    String url = args != null ? args.getString("QUERY") : null;

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);
                    state = State.LOADING_THUMBNAILS;
                    return new VideoLoader(TrailerActivity.this, url);
                }

                @Override
                public void onLoadFinished(@NonNull android.support.v4.content.Loader<List<Trailer>> loader, List<Trailer> data) {
                    // Hide loading indicator because the data has been loaded
                    loadingIndicator.setVisibility(View.GONE);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);

                    // If the query comes back empty, set empty state text to display "There are no trailers"
                    if (data == null) {
                        mEmptyStateTextView.setText(R.string.no_trailers);
                    }

                    //Check connectivity and display error message if needed
                    if (!isOnline()) {
                        mEmptyStateTextView.setText(R.string.no_internet_connection);
                    }

                    // If there is a valid list of {@link Trailer}, then add them to the adapter's
                    // data set. This will trigger the GridView to update.
                    if (data != null && !data.isEmpty()) {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        trailerGridAdapter.addAll(data);
                        trailerGridAdapter.notifyDataSetChanged();
                        gridView.setVisibility(View.VISIBLE);
                        state = State.VIDEO_ENDED;
                    }
                }

                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<Trailer>> loader) {
                    // Loader reset, so we can clear out our existing data.
                    trailerGridAdapter.clear();
                }
            };

    //Custom callbacks for the VideoLoader - returns the list of Trailer objects and if successful,
    // triggers the buildTheWall method that sets up the image wall and populates it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);
        state = State.UNINITIALIZED;

        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        ViewGroup viewFrame = findViewById(R.id.viewFrame);
        playerView = new FrameLayout(this);
        playerView.setId(R.id.player_view);
        playerView.setVisibility(View.GONE);
        viewFrame.addView(playerView, MATCH_PARENT, MATCH_PARENT);

        playerFragment = YouTubePlayerFragment.newInstance();
        playerFragment.initialize(Keys.YOU_TUBE_KEY, this);
        getFragmentManager().beginTransaction().add(R.id.player_view, playerFragment).commit();

        Intent intent = getIntent();
        String trailerUrl = intent.getStringExtra("QUERY");
        bundle.putString("QUERY", trailerUrl);

        gridView = findViewById(R.id.trailerGridView);
        trailerGridAdapter = new TrailerGridAdapter(TrailerActivity.this, trailers);
        gridView.setAdapter(trailerGridAdapter);

        if (isOnline()) {
            getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, bundle, trailerLoaderCallbacks);
        } else {
            // Hide loading indicator because the data cannot be loaded
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                trailer = (Trailer) parent.getItemAtPosition(position);
                state = State.VIDEO_LOADING;
                player.cueVideo(trailer.getTrailer_key());
            }
        });
    }

    //Overridden YouTubePlayer methods

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasResumed) {
        TrailerActivity.this.player = player;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        player.setPlayerStateChangeListener(new VideoListener());
    }

    @Override
    public void onInitializationFailure(
            YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            if (errorDialog == null || !errorDialog.isShowing()) {
                errorDialog = errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST);
                errorDialog.show();
            }
        } else {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    //Activity lifecycle methods
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            if (errorDialog != null && errorDialog.isShowing()) {
                errorDialog.dismiss();
            }
            errorDialog = null;
            playerFragment.initialize(Keys.YOU_TUBE_KEY, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (state.equals(State.VIDEO_PLAYING)) {
            gridView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            player.play();
        }
        if (state.equals(State.VIDEO_LOADING)) {
            gridView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.VISIBLE);
            player.cueVideo(trailer.getTrailer_key());
        }
        if (state.equals(State.VIDEO_ENDED)) {
            gridView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.GONE);
        }
        if (state.equals(State.LOADING_THUMBNAILS)) {
            gridView.setVisibility(View.GONE);
            playerView.setVisibility(View.GONE);
            loadingIndicator.setVisibility(View.VISIBLE);
            if (isOnline()) {
                getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, bundle, trailerLoaderCallbacks);
            } else {
                // Hide loading indicator because the data cannot be loaded
                loadingIndicator.setVisibility(View.GONE);
                // Update empty state with no connection error message
                mEmptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }

    @Override
    protected void onDestroy() {
        getSupportLoaderManager().destroyLoader(TRAILER_LOADER_ID);
        super.onDestroy();
    }

    //Utility for network testing
    private boolean isOnline() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        // If there is a network connection, fetch data
        return (networkInfo != null && networkInfo.isConnected());
    }

    //All of the states for the thumbnail loader
    private enum State {
        UNINITIALIZED,
        LOADING_THUMBNAILS,
        VIDEO_LOADING,
        VIDEO_CUED,
        VIDEO_PLAYING,
        VIDEO_ENDED
    }

    //VideoListener manages view visibility based on player state and trigger video playback upon completion of cuing
    private final class VideoListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoaded(String videoId) {
            gridView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            state = State.VIDEO_CUED;
            player.play();
        }

        @Override
        public void onVideoEnded() {
            gridView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
            state = State.LOADING_THUMBNAILS;
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // player has encountered an unrecoverable error
                mEmptyStateTextView = findViewById(R.id.empty_view);
                mEmptyStateTextView.setText(errorReason.toString());
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);
                gridView.setVisibility(View.GONE);
                state = State.UNINITIALIZED;
                player = null;
            } else {
                state = State.VIDEO_ENDED;
            }
        }

        // ignored callbacks

        @Override
        public void onVideoStarted() {
            state = State.VIDEO_PLAYING;
        }

        @Override
        public void onAdStarted() {
        }

        @Override
        public void onLoading() {
            state = State.VIDEO_LOADING;
        }
    }
}



