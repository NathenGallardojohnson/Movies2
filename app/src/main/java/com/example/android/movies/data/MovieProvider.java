package com.example.android.movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.Objects;

import static com.example.android.movies.data.MovieContract.FAVORITED;
import static com.example.android.movies.data.MovieContract.IS_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_FAVORITED;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_PLOT;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POPULARITY;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_POSTER_PATH;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.example.android.movies.data.MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_FAVORITES_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_MOVIES_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.CONTENT_MOVIE_TYPE;
import static com.example.android.movies.data.MovieContract.MovieEntry.DETAIL_PROJECTION;
import static com.example.android.movies.data.MovieContract.MovieEntry.FAVORITE_PROJECTION;
import static com.example.android.movies.data.MovieContract.MovieEntry.IS_FAVORITE_PROJECTION;
import static com.example.android.movies.data.MovieContract.MovieEntry.TABLE_NAME;
import static com.example.android.movies.data.MovieContract.NOT_FAVORITED;
import static com.example.android.movies.data.MovieContract.ORDER_BY_POPULAR;
import static com.example.android.movies.data.MovieContract.PATH_FAVORITES;
import static com.example.android.movies.data.MovieContract.PATH_MOVIE;
import static com.example.android.movies.data.MovieContract.PATH_MOVIES;


public class MovieProvider extends ContentProvider {

    private static final String LOG_TAG = MovieProvider.class.getSimpleName();

    public static final int MOVIES = 1;

    public static final int MOVIE = 2;

    public static final int FAVORITES = 3;

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
        movieDbHelper.getReadableDatabase();

        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = "_ID ASC";
                cursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case MOVIE:
                selection = COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        DETAIL_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            case FAVORITES:
                if (TextUtils.isEmpty(sortOrder)) sortOrder = ORDER_BY_POPULAR;
                cursor = movieDbHelper.getReadableDatabase().query(
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

        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);

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
            throw new IllegalArgumentException("Movie item requires an id");
        }
        if (COLUMN_FAVORITED == null) {
            throw new IllegalArgumentException("Movie item requires a favorites designation");
        }
        if (COLUMN_TITLE == null) {
            throw new IllegalArgumentException("Movie item requires a title");
        }
        if (COLUMN_RELEASE_DATE == null) {
            throw new IllegalArgumentException("Movie item requires a release date");
        }
        if (COLUMN_POSTER_PATH == null) {
            throw new IllegalArgumentException("Movie item requires a poster path");
        }
        if (COLUMN_VOTE_AVERAGE == null) {
            throw new IllegalArgumentException("Movie item requires a vote average");
        }
        if (COLUMN_POPULARITY == null) {
            throw new IllegalArgumentException("Movie item requires a popularity number");
        }
        if (COLUMN_PLOT == null) {
            throw new IllegalArgumentException("Movie item requires a plot");
        }

        long id = movieDbHelper.getWritableDatabase().insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        final int match = sUriMatcher.match(uri);

        switch (match) {

            case MOVIE:
                selection = COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = movieDbHelper.getReadableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case MOVIES:
                numRowsDeleted = movieDbHelper.getReadableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        null,
                        null);
                break;

            case FAVORITES:
                if (selectionArgs == FAVORITED || selectionArgs == NOT_FAVORITED) {
                    selection = COLUMN_FAVORITED + "=?";
                    numRowsDeleted = movieDbHelper.getReadableDatabase().delete(
                            MovieContract.MovieEntry.TABLE_NAME,
                            selection,
                            selectionArgs);
                    break;
                } else {
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
                }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numRowsDeleted != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)) {

            case MOVIE:
                selection = COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return toggleFavorited(uri, null, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int toggleFavorited(@NonNull Uri uri, @Nullable ContentValues values,
                               @Nullable String selection, @Nullable String[] selectionArgs){
        Cursor cursor;
        ContentValues fValues = new ContentValues();
        int rowsUpdated = 0;
        cursor = movieDbHelper.getWritableDatabase().query
                (
                        TABLE_NAME,
                        IS_FAVORITE_PROJECTION,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null
                );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                boolean isFav = (FAVORITED[0].equals(cursor.getString(cursor.getColumnIndex(COLUMN_FAVORITED))));

                if (isFav) {
                    fValues.put(COLUMN_FAVORITED, "false");
                } else {
                    fValues.put(COLUMN_FAVORITED, "true");
                }

                rowsUpdated = movieDbHelper.getWritableDatabase().update(
                        TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

                if (rowsUpdated != 0) {
                    Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

                }
            }
        }
        cursor.close();
        return rowsUpdated;
    }
}
