package com.example.android.movies.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MovieContract {

    private MovieContract(){}

    public static final String AUTHORITY = "com.example.android.movies.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY );

    public static final String PATH_MOVIES = "movie";

    public static final String PATH_MOVIE = "movie/#";

    public static final String PATH_FAVORITES = "movie/favorites";

    public static final class MovieEntry implements BaseColumns {

        public final static String _ID = BaseColumns._ID;

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_ID = "id";

        public static final String COLUMN_FAVORITED = "favorited";

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_RELEASE_DATE = "releaseDate";

        public static final String COLUMN_POSTER_PATH = "posterPath";

        public static final String COLUMN_VOTE_AVERAGE = "voteAverage";

        public static final String COLUMN_POPULARITY = "popularity";

        public static final String COLUMN_PLOT = "plot";

        public static final String COLUMN_REVIEW_QUANTITY = "reviewQuantity";

        public static final String COLUMN_TRAILER_QUANTITY = "trailerQuantity";

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_MOVIE);

        public static final String CONTENT_MOVIES_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIES;

        public static final String CONTENT_MOVIE_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_FAVORITES_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + PATH_FAVORITES;

        public static final String[] DETAIL_PROJECTION =
                {_ID, COLUMN_ID, COLUMN_FAVORITED,
                        COLUMN_TITLE,
                        COLUMN_RELEASE_DATE,
                        COLUMN_POSTER_PATH,
                        COLUMN_VOTE_AVERAGE,
                        COLUMN_PLOT,
                        COLUMN_REVIEW_QUANTITY,
                        COLUMN_TRAILER_QUANTITY};

        public static final String[] FAVORITE_PROJECTION =
                    {_ID, COLUMN_ID, COLUMN_FAVORITED,
                            COLUMN_TITLE,
                            COLUMN_RELEASE_DATE,
                            COLUMN_POSTER_PATH,
                            COLUMN_VOTE_AVERAGE,
                            COLUMN_POPULARITY,
                            COLUMN_PLOT};

        public static final String[] FAVORITED_PROJECTION =
                    {_ID, COLUMN_ID, COLUMN_FAVORITED};

        }

        public static final String IS_FAVORITED = "WHERE COLUMN_FAVORITED = ?";

        public static final String ORDER_BY_POPULAR = "ORDER BY COLUMN_POPULARITY DESC";

        public static final String ORDER_BY_VOTE = "ORDER BY COLUMN_VOTE_AVERAGE DESC";

        public static final String[] FAVORITED = {"true"};

        public static final String[] NOT_FAVORITED = {"false"};

}