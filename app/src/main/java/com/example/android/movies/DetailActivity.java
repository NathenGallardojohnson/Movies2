package com.example.android.movies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks {
    private static final int REVIEW_LOADER_ID = 2;
    private static final int VIDEOS_LOADER_ID = 3;
    private static final int VIDEO_TYPE = 1;
    private static final int REVIEW_TYPE = 2;
    private static final int LIST_ITEM_TYPE_COUNT = 2;
    private final String BASEAPIURL = "http://api.themoviedb.org/3/movie/";
    private final String VIDEOS = "/videos";
    private final String REVIEWS = "/reviews";
    //API KEY REMOVED - get one at  https://www.themoviedb.org/account/signup
    private final String APIKEY = "?api_key=bf4f905b88823288bf4ac9bca4225847";
    ListView listView;
    private List<Data> reviewData = new ArrayList<>();
    private List<Data> videoData = new ArrayList<>();
    private TextView mEmptyStateTextView;
    private ImageView loadingIndicator;
    private DataAdapter mDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDataAdapter = new DataAdapter();

        mEmptyStateTextView = findViewById(R.id.empty_view);
        loadingIndicator = findViewById(R.id.loading_indicator);

        String title = getIntent().getStringExtra("title");
        String releaseDate = getIntent().getStringExtra("releaseDate");
        String posterPath = getIntent().getStringExtra("posterPath");
        String voteAverage = getIntent().getStringExtra("voteAverage");
        String plot = getIntent().getStringExtra("plot");
        String id = getIntent().getStringExtra("id");
        String reviewUrl = (BASEAPIURL + id + REVIEWS + APIKEY);
        String videoUrl = (BASEAPIURL + id + VIDEOS + APIKEY);
        String posterUrl = Utils.getPosterUrl(posterPath);
      //  videoAdapter = new VideoAdapter(this, null);
      //  reviewAdapter = new ReviewAdapter(this, null);
        listView = findViewById(R.id.list_view);
        TextView titleTextView = findViewById(R.id.title);
        titleTextView.setText(title);
        TextView releaseTextView = findViewById(R.id.release_date);
        releaseTextView.setText(releaseDate);
        TextView voteAverageTextView = findViewById(R.id.vote);
        voteAverageTextView.setText(voteAverage);
        TextView plotTextView = findViewById(R.id.plot);
        plotTextView.setText(plot);
        ImageView imageView = findViewById(R.id.image);
        Picasso.with(this).load(posterUrl).into(imageView);
        if (isOnline()) {
            // Get a reference to the LoaderManager, in order to interact with loaders
            Bundle reviewBundle = new Bundle();
            reviewBundle.putString("QUERY", reviewUrl);
            Bundle videoBundle = new Bundle();
            videoBundle.putString("QUERY", videoUrl);
            getLoaderManager().initLoader(REVIEW_LOADER_ID, reviewBundle, this);
            getLoaderManager().initLoader(VIDEOS_LOADER_ID, videoBundle, this);
            getLoaderManager().restartLoader(REVIEW_LOADER_ID, reviewBundle, this);
            getLoaderManager().restartLoader(VIDEOS_LOADER_ID, videoBundle, this);
        } else {

            // Clear the adapter of previous movie data
            mDataAdapter.clear();
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }


    }

    private class DataAdapter extends BaseAdapter {

        private List<Data> mData = new ArrayList<>();
        private LayoutInflater mInflater;

        public DataAdapter() {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        protected void clear(){
            mData.clear();
        }

        public void addAllToAdapter(List<Data> data){
            for (int i = 0; i < data.size(); i++) {
                Data dataItem = getItem(i);
                String mName = dataItem.getName();
                String mContent = dataItem.getContent();
                int mType = dataItem.getType();
                URL mUrl = dataItem.getURL();
                dataItem.setName(mName);
                dataItem.setContent(mContent);
                dataItem.setUrl(mUrl);
                dataItem.setType(mType);
                mData.add(dataItem);
            }

        }

        public int getItemViewType(Data data) {
            return data.getType();
        }

        @Override
        public int getViewTypeCount() {
            return LIST_ITEM_TYPE_COUNT;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Data getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            Data data = getItem(position);
            int type = getItemViewType(data);
            if (convertView == null) {
                holder = new ViewHolder();
                switch(type) {
                    case REVIEW_TYPE:
                        convertView = mInflater.inflate(R.layout.review_item, parent, false);
                        holder.nameTextView = convertView.findViewById(R.id.author_text_view);
                        holder.contentTextView = convertView.findViewById(R.id.content_text_view);
                        break;
                    case VIDEO_TYPE:
                        convertView = mInflater.inflate(R.layout.video_item, parent, false);
                        holder.nameTextView = convertView.findViewById(R.id.name_text_view);
                        holder.contentTextView = convertView.findViewById(R.id.key_text_view);
                        break;
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.nameTextView.setText(data.getName());
            holder.contentTextView.setText(data.getContent());
            return convertView;
        }

    }

    public static class ViewHolder {
        public TextView nameTextView;
        public TextView contentTextView;
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
    public Loader onCreateLoader(int id, Bundle bundle) {
        String url = bundle.getString("QUERY");


        //loadingIndicator.setVisibility(View.VISIBLE);

        if (id == REVIEW_LOADER_ID) {
            return new ReviewLoader(this, url);
        } else if (id == VIDEOS_LOADER_ID) {

            return new VideoLoader(this, url);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        // Hide loading indicator because the data has been loaded
        //loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "There are no reviews"
        //mEmptyStateReviewTextView.setText(R.string.no_reviews);
        //mEmptyStateReviewTextView.setVisibility(View.VISIBLE);

        // Set empty state text to display "There are no videos"
        //mEmptyStateVideoTextView.setText(R.string.no_videos);
        //mEmptyStateVideoTextView.setVisibility(View.VISIBLE);

        if (!isOnline()) {
            //mEmptyStateVideoTextView.setText(R.string.no_internet_connection);
            //mEmptyStateReviewTextView.setText(R.string.no_internet_connection);
        }
        int id = loader.getId();// find which loader you called
        if (data != null ) {
            if (id == REVIEW_LOADER_ID) {
                reviewData = (List<Data>) data;
            } else if (id == VIDEOS_LOADER_ID) {
                videoData = (List<Data>) data;
            }
            if (videoData != null && !videoData.isEmpty() && reviewData != null && !reviewData.isEmpty()) {
                // mEmptyStateReviewTextView.setVisibility(View.GONE);
                try {
                    reviewData.addAll(videoData);
                    mDataAdapter.addAllToAdapter(reviewData);
                    mDataAdapter.notifyDataSetChanged();
                    listView.setAdapter(mDataAdapter);
                }
                catch (java.lang.NullPointerException exception){
                    // Catch NullPointerExceptions.
                    log(exception);
                }
            }

        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
            try {
                mDataAdapter.clear();
            } catch (java.lang.NullPointerException exception) {
                // Catch NullPointerExceptions.
                log(exception);
            }
    }

    public static void log(Object value)
    {
        System.out.println(value);
    }

}

