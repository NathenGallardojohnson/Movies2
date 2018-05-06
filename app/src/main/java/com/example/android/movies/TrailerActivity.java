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
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class TrailerActivity extends AppCompatActivity implements
        YouTubePlayer.OnInitializedListener,
        YouTubeThumbnailView.OnInitializedListener, YouTubeThumbnailView.OnClickListener {

    private static final int TRAILER_LOADER_ID = 4;

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    // The player view cannot be smaller than 110 pixels high.
    private static final float PLAYER_VIEW_MINIMUM_HEIGHT_DP = 110;
    private static final int MAX_NUMBER_OF_ROWS_WANTED = 4;

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
    private View loadingIndicator;
    private Dialog errorDialog;
    private int videoCol;
    private int videoRow;
    private boolean nextThumbnailLoaded;
    private boolean activityResumed;
    private State state;
    private Trailer Current_Video;
    private String Trailer_Key;
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

                    // Set empty state text to display "There are no reviews"
                    mEmptyStateTextView.setText(R.string.no_trailers);
                    mEmptyStateTextView.setVisibility(View.VISIBLE);

                    //Check connectivity and display error message if needed
                    if (!isOnline()) {
                        mEmptyStateTextView.setText(R.string.no_internet_connection);
                    }

                    // If there is a valid list of {@link Review}, then add them to the adapter's
                    // data set. This will trigger the ListView to update.
                    if (data != null && !data.isEmpty()) {
                        mEmptyStateTextView.setVisibility(View.GONE);
                        VIDEO_LIST = data;
                    } else { //This is just dummy data in case I need to work on this offline
                        List<Trailer> list = new ArrayList<>();
                        list.add(new Trailer("YouTube Collection", "Y_UmWdcTrrc"));
                        list.add(new Trailer("GMail Tap", "1KhZKNZO8mQ"));
                        list.add(new Trailer("Chrome Multitask", "UiLSiqyDf4Y"));
                        list.add(new Trailer("Google Fiber", "re0VRK6ouwI"));
                        list.add(new Trailer("Autocompleter", "blB_X38YSxQ"));
                        list.add(new Trailer("GMail Motion", "Bu927_ul_X0"));
                        list.add(new Trailer("Translate for Animals", "3I24bSteJpw"));
                        VIDEO_LIST = Collections.unmodifiableList(list);
                    }

                }


                @Override
                public void onLoaderReset(@NonNull android.support.v4.content.Loader<List<Trailer>> loader) {
                    VIDEO_LIST = new ArrayList<>();
                }
            };

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
        state = State.UNINITIALIZED;

        ViewGroup viewFrame = new FrameLayout(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int maxAllowedNumberOfRows = (int) Math.floor(
                (displayMetrics.heightPixels / displayMetrics.density) / PLAYER_VIEW_MINIMUM_HEIGHT_DP);
        int numberOfRows = Math.min(maxAllowedNumberOfRows, MAX_NUMBER_OF_ROWS_WANTED);
        int interImagePaddingPx = (int) displayMetrics.density * INTER_IMAGE_PADDING_DP;
        int imageHeight = (displayMetrics.heightPixels / numberOfRows) - interImagePaddingPx;
        int imageWidth = (int) (imageHeight * THUMBNAIL_ASPECT_RATIO);

        imageWallView = new ImageWallView(this, imageWidth, imageHeight, interImagePaddingPx);
        viewFrame.addView(imageWallView, MATCH_PARENT, MATCH_PARENT);

        thumbnailView = new YouTubeThumbnailView(this);
        thumbnailView.initialize(Keys.YOU_TUBE_KEY, this);

        playerView = new FrameLayout(this);
        playerView.setId(R.id.player_view);
        playerView.setVisibility(View.INVISIBLE);
        viewFrame.addView(playerView, imageWidth, imageHeight);

        playerFragment = YouTubePlayerFragment.newInstance();
        playerFragment.initialize(Keys.YOU_TUBE_KEY, this);
        getFragmentManager().beginTransaction().add(R.id.player_view, playerFragment).commit();

        setContentView(viewFrame);
    }

    @Override
    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
        this.thumbnailLoader = youTubeThumbnailLoader;
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

    private void loadThumbs() {
        if (activityResumed && player != null && thumbnailLoader != null
                && state.equals(State.UNINITIALIZED)) {
            int i = 0;
            state = State.LOADING_THUMBNAILS;
            while (i < VIDEO_LIST.size()) {
                Current_Video = VIDEO_LIST.get(i);
                String Current_Key = Current_Video.getTrailer_key();
                thumbnailLoader.setVideo(Current_Key);
                i++;
            }
        }
    }

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
                loadThumbs();
            } else if (state.equals(State.LOADING_THUMBNAILS)) {
                loadThumbs();
            } else {
                if (state.equals(State.VIDEO_PLAYING)) {
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

    private void loadNextThumbnail() {
        nextThumbnailLoaded = false;
        if (thumbnailLoader.hasNext()) {
            thumbnailLoader.next();
        } else {
            thumbnailLoader.first();
        }
    }

    @Override
    public void onClick(View v) {
        Trailer_Key = (String) v.getTag();
        state = State.VIDEO_LOADING;
        player.cueVideo(Trailer_Key);
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

    private enum State {
        UNINITIALIZED,
        LOADING_THUMBNAILS,
        VIDEO_LOADING,
        VIDEO_CUED,
        VIDEO_PLAYING,
        VIDEO_ENDED
    }

    private final class ThumbnailListener implements
            YouTubeThumbnailLoader.OnThumbnailLoadedListener {
        @Override
        public void onThumbnailLoaded(YouTubeThumbnailView thumbnail, String videoId) {
            nextThumbnailLoaded = true;
            thumbnail.setTag(videoId);

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

    private final class VideoListener implements YouTubePlayer.PlayerStateChangeListener {

        @Override
        public void onLoaded(String videoId) {
            state = State.VIDEO_CUED;
            playerView.setVisibility(View.VISIBLE);
            state = State.VIDEO_PLAYING;
            player.play();
        }

        @Override
        public void onVideoEnded() {
            state = State.VIDEO_ENDED;
            imageWallView.showImage(videoCol, videoRow);
            playerView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            if (errorReason == YouTubePlayer.ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
                // player has encountered an unrecoverable error
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




