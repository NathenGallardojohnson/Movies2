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
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static java.lang.Math.sqrt;

public class TrailerActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener,
        YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailView.OnClickListener {

    private static final int TRAILER_LOADER_ID = 4;

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // The player view cannot be smaller than 110 pixels high.
    private static final float PLAYER_VIEW_MINIMUM_HEIGHT_DP = 110;
    private static int MAX_NUMBER_OF_ROWS_WANTED = 4;

    private static final int INTER_IMAGE_PADDING_DP = 5;

    // YouTube thumbnails have a 16 / 9 aspect ratio
    private static final double THUMBNAIL_ASPECT_RATIO = 16 / 9d;
    private static List<Trailer> VIDEO_LIST = new ArrayList<>();
    private ImageWallView imageWallView;
    private YouTubeThumbnailView thumbnailView;
    private YouTubeThumbnailLoader thumbnailLoader;
    private YouTubePlayerFragment playerFragment;
    private View playerView;
    private YouTubePlayer player;
    private TextView mEmptyStateTextView;
    private ProgressBar loadingIndicator;
    private Dialog errorDialog;
    private boolean activityResumed;
    private State state;
    private LoaderManager.LoaderCallbacks<List<Trailer>> trailerLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<Trailer>>() {
                @NonNull
                @Override
                public VideoLoader onCreateLoader(int id, @Nullable Bundle args) {
                    String url = args.getString("QUERY");

                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.VISIBLE);
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

                    // If there is a valid list of {@link Trailer}, then send them to the image wall
                    if (data != null && !data.isEmpty()) {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        if ((data.size() % 2) == 1) {
                            MAX_NUMBER_OF_ROWS_WANTED = (int) (1 + sqrt((double) data.size()));
                        } else {
                            MAX_NUMBER_OF_ROWS_WANTED = (int) sqrt((double) data.size());
                        }
                        //Clear the list so it doesn't accumulate erroneous items
                        VIDEO_LIST = new ArrayList<>();
                        //Set list to current movies trailers
                        VIDEO_LIST = data;
                        buildTheWall();
                    }
                }

                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<Trailer>> loader) {
                    //Clear the VIDEO_LIST for the next use
                    VIDEO_LIST = new ArrayList<>();
                }
            };

    //Custom callbacks for the VideoLoader - returns the list of Trailer objects and if successful,
    // triggers the buildTheWall method that sets up the image wall and populates it

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trailer);
        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);
        Intent intent = getIntent();
        String trailerUrl = intent.getStringExtra("QUERY");
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", trailerUrl);
        if (isOnline()) {
            getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, bundle, trailerLoaderCallbacks);
        } else {
            // Hide loading indicator because the data cannot be loaded
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

    }

    //Recursive method for loading all thumbnails in the VIDEO_LIST - the matching
    //listener takes these as they come and loads them into the individual image wall view

    private void loadThumbs() {
        if (activityResumed && player != null && thumbnailLoader != null
                && state.equals(State.UNINITIALIZED)) {
            int i = 0;
            state = State.LOADING_THUMBNAILS;
            while (i < VIDEO_LIST.size()) {
                thumbnailLoader.setVideo(VIDEO_LIST.get(i).getTrailer_key());
                i++;
            }
        }
    }

    //Overridden YouTubeThumbnailView methods

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView thumbnailView, YouTubeThumbnailLoader thumbnailLoader) {
        this.thumbnailLoader = thumbnailLoader;
        thumbnailLoader.setOnThumbnailLoadedListener(new ThumbnailListener());
        loadThumbs();
    }

    @Override
    public void onInitializationFailure(
            YouTubeThumbnailView thumbnailView, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            if (errorDialog == null || !errorDialog.isShowing()) {
                errorDialog = errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST);
                errorDialog.show();
            }
        } else {
            String errorMessage =
                    String.format(getString(R.string.error_thumbnail_view), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    //Overridden YouTubePlayer methods

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasResumed) {
        TrailerActivity.this.player = player;
        player.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        player.setPlayerStateChangeListener(new VideoListener());
        loadThumbs();
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

    private void buildTheWall() {
        state = State.UNINITIALIZED;

        ViewGroup viewFrame = findViewById(R.id.viewFrame);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int maxAllowedNumberOfRows = (int) Math.floor(
                (displayMetrics.heightPixels / displayMetrics.density) / PLAYER_VIEW_MINIMUM_HEIGHT_DP);
        int numberOfRows = Math.min(maxAllowedNumberOfRows, MAX_NUMBER_OF_ROWS_WANTED);
        int interImagePaddingPx = (int) displayMetrics.density * INTER_IMAGE_PADDING_DP;
        int imageHeight = (displayMetrics.heightPixels / numberOfRows) - interImagePaddingPx;
        int imageWidth = (int) (imageHeight * THUMBNAIL_ASPECT_RATIO);

        imageWallView = new ImageWallView(this, imageWidth, imageHeight, interImagePaddingPx);
        imageWallView.setVisibility(View.VISIBLE);
        viewFrame.addView(imageWallView, MATCH_PARENT, MATCH_PARENT);

        thumbnailView = new YouTubeThumbnailView(this);
        thumbnailView.initialize(Keys.YOU_TUBE_KEY, this);

        playerView = new FrameLayout(this);
        playerView.setId(R.id.player_view);
        playerView.setVisibility(View.GONE);
        viewFrame.addView(playerView, MATCH_PARENT, MATCH_PARENT);

        playerFragment = YouTubePlayerFragment.newInstance();
        playerFragment.initialize(Keys.YOU_TUBE_KEY, this);
        getFragmentManager().beginTransaction().add(R.id.player_view, playerFragment).commit();

        // Hide loading indicator and empty view as they are no longer needed
        loadingIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setVisibility(View.GONE);

        //Set the image wall and player as the main view
        setContentView(viewFrame);
    }

    //OnClick method for the trailer thumbnails that launches the player
    @Override
    public void onClick(View v) {
        state = State.VIDEO_LOADING;
        player.cueVideo((String) v.getTag());
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
            thumbnailView.initialize(Keys.YOU_TUBE_KEY, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;
        if (thumbnailLoader != null && player != null) {
            if (state.equals(State.UNINITIALIZED)) {
                imageWallView.setVisibility(View.INVISIBLE);
                loadThumbs();
            } else if (state.equals(State.LOADING_THUMBNAILS)) {
                imageWallView.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);
                loadThumbs();
            } else {
                if (state.equals(State.VIDEO_PLAYING)) {
                    imageWallView.setVisibility(View.GONE);
                    playerView.setVisibility(View.VISIBLE);
                    player.play();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        activityResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (thumbnailLoader != null) {
            thumbnailLoader.release();
        }
        getSupportLoaderManager().destroyLoader(TRAILER_LOADER_ID);
        super.onDestroy();
    }

    //Utility for network testing
    protected boolean isOnline() {
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

    //Thumbnail listener adds each thumbnail to the image wall as they are loaded
    private final class ThumbnailListener implements
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {
        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {
            Pair<Integer, Integer> target = imageWallView.getNextLoadTarget();
            int elementIdx = imageWallView.getElementIdx(target.first, target.second);
            thumbnail.setTag(videoId);
            imageWallView.unInitializedImages.remove(new Integer(elementIdx));
            imageWallView.images[elementIdx] = thumbnail;
            imageWallView.images[elementIdx].setVisibility(View.VISIBLE);

            if (activityResumed) {
                if (state.equals(State.LOADING_THUMBNAILS)) {
                    loadThumbs();
                } else if (state.equals(State.VIDEO_PLAYING)) {
                    state = State.VIDEO_LOADING;
                    player.cueVideo(videoId);
                }
            }
        }

        @Override
        public void onThumbnailError(YouTubeThumbnailView thumbnail,
                                     YouTubeThumbnailLoader.ErrorReason reason) {
            loadThumbs();
        }
    }

    //VideoListener manages view visibility based on player state and trigger video playback upon completion of cuing
    private final class VideoListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoaded(String videoId) {
            thumbnailView.setVisibility(View.GONE);
            playerView.setVisibility(View.VISIBLE);
            state = State.VIDEO_PLAYING;
            player.play();
        }

        @Override
        public void onVideoEnded() {
            state = State.VIDEO_ENDED;
            imageWallView.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.GONE);
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // player has encountered an unrecoverable error
                mEmptyStateTextView = findViewById(R.id.empty_view);
                mEmptyStateTextView.setText(errorReason.toString());
                mEmptyStateTextView.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);
                imageWallView.setVisibility(View.GONE);
                state = State.UNINITIALIZED;
                thumbnailLoader.release();
                thumbnailLoader = null;
                player = null;
            } else {
                state = State.VIDEO_ENDED;
            }
        }

        // ignored callbacks

        @Override
        public void onVideoStarted() {
        }

        @Override
        public void onAdStarted() {
        }

        @Override
        public void onLoading() {
        }
    }
}



