package com.example.android.movies.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.movies.data.MovieContract.MovieEntry;

class MovieDbHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 3;

    MovieDbHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_TABLE =

                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieEntry.COLUMN_FAVORITED + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_ID + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_TITLE + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_RELEASE_DATE + " REAL NOT NULL, " +
                        MovieEntry.COLUMN_POSTER_PATH + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_VOTE_AVERAGE + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_POPULARITY + " STRING NOT NULL, " +
                        MovieEntry.COLUMN_PLOT + " STRING NOT NULL, " +
                        " UNIQUE (" + MovieEntry.COLUMN_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);

        onCreate(db);
    }
}
