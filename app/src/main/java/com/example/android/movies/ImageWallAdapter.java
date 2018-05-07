package com.example.android.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class ImageWallAdapter extends ArrayAdapter<TrailerTile> {
    private final Context context;
    private final int layoutResourceId;
    private final List<TrailerTile> trailerTiles;

    public ImageWallAdapter(Context context, List<TrailerTile> trailerTiles) {
        super(context, R.layout.trailer_item, trailerTiles);
        layoutResourceId = R.layout.trailer_item;
        this.context = context;
        this.trailerTiles = trailerTiles;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    static class ViewHolder {
        TextView title;
        ImageView image;
    }

}
