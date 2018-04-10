package com.example.android.movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import static com.example.android.movies.data.MovieContract.IS_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_PLOT;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_REVIEW_QUANTITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TRAILER_QUANTITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_FAVORITES_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_MOVIES_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_MOVIE_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.DETAIL_PROJECTION;
import static com.example.android.movies.data.MovieContract.MovieEntry.FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.FAVORITE_PROJECTION;
import static com.example.android.movies.data.MovieContract.MovieEntry.TABLE_NAME;
import static com.example.android.movies.data.MovieContract.MovieEntry._ID;
import static com.example.android.movies.data.MovieContract.PATH_FAVORITES;
import static com.example.android.movies.data.MovieContract.PATH_MOVIE;
import static com.example.android.movies.data.MovieContract.PATH_MOVIES;


public class MovieProvider extends ContentProvider {

    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    private static final int MOVIES = 1;

    private static final int MOVIE = 2;

    private static final int FAVORITES = 3;

    private MovieDbHelper movieDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MovieContract.AUTHORITY, PATH_MOVIES, MOVIES);
        sUriMatcher.addURI(MovieContract.AUTHORITY, PATH_MOVIE, MOVIE);
        sUriMatcher.addURI(MovieContract.AUTHORITY, PATH_FAVORITES, FAVORITES);
    }

    public MovieProvider() {
    }

    @Override
    public boolean onCreate() {

        movieDbHelper = new MovieDbHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = movieDbHelper.getReadableDatabase();

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                cursor = database.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            case FAVORITES:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                cursor = database.query(
                        TABLE_NAME,
                        FAVORITE_PROJECTION,
                        IS_FAVORITED,
                        FAVORITED,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unknown Uri" + uri);

        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return CONTENT_MOVIES_TYPE;
            case MOVIE:
                return CONTENT_MOVIE_TYPE;
            case FAVORITES:
            return CONTENT_FAVORITES_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES:
                return insertMovie(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertMovie(Uri uri, ContentValues values) {
        if (COLUMN_ID == null) {
            throw new IllegalArgumentException("Inventory item requires an id");
        }
        if (COLUMN_FAVORITED == null) {
            throw new IllegalArgumentException("Inventory item requires a favorites designation");
        }
        if (COLUMN_TITLE == null) {
            throw new IllegalArgumentException("Inventory item requires a title");
        }
        if (COLUMN_RELEASE_DATE == null) {
            throw new IllegalArgumentException("Inventory item requires a release date");
        }
        if (COLUMN_POSTER_PATH == null) {
            throw new IllegalArgumentException("Inventory item requires a poster path");
        }
        if (COLUMN_VOTE_AVERAGE == null) {
            throw new IllegalArgumentException("Inventory item requires a vote average");
        }
        if (COLUMN_PLOT == null) {
            throw new IllegalArgumentException("Inventory item requires a plot");
        }
        if (COLUMN_REVIEW_QUANTITY == null) {
            throw new IllegalArgumentException("Inventory item requires a review quantity");
        }
        if (COLUMN_TRAILER_QUANTITY == null) {
            throw new IllegalArgumentException("Inventory item requires a trailer quantity");
        }

        SQLiteDatabase database = movieDbHelper.getWritableDatabase();

        long id = database.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {
            case FAVORITES:
                return toggleFavorited(uri, values,selection,selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int toggleFavorited(@NonNull Uri uri, @Nullable ContentValues values,
                               @Nullable String selection, @Nullable String[] selectionArgs){
        SQLiteDatabase database = movieDbHelper.getWritableDatabase();
        if(values.containsKey(COLUMN_FAVORITED)) {

                if (values.getAsBoolean(COLUMN_FAVORITED)) {
                    values.put(COLUMN_FAVORITED, "false");
                } else {
                    values.put(COLUMN_FAVORITED, "true");
                }
            }
            int rowsUpdated = database.update(TABLE_NAME, values, selection, selectionArgs);

            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            return rowsUpdated;

    }
}
