package com.example.android.movies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MovieCursorAdapter extends CursorAdapter {

    private final Context mContext;

    MovieCursorAdapter(Context context){
    super(context, null, 0);
    mContext = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.grid_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    static class ThumbViewHolder {
        TextView imageTitle;
        ImageView image;
    }
    static class DetailViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}

