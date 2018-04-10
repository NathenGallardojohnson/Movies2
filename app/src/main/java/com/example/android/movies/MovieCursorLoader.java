package com.example.android.movies;

import android.content.Context;
import android.net.Uri;

public class MovieCursorLoader extends android.support.v4.content.CursorLoader {
    public MovieCursorLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }
}
