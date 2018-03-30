package com.example.android.movies;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

class GridViewAdapter extends ArrayAdapter<MovieData> {
    private final Context context;
    private final int layoutResourceId;
    private final List<MovieData> data;

    public GridViewAdapter(Context context, List<MovieData> data) {
        super(context, R.layout.grid_item, data);
        this.layoutResourceId = R.layout.grid_item;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = row.findViewById(R.id.text);
            holder.image = row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        MovieData movieData = data.get(position);
        holder.imageTitle.setText(movieData.getTitle());
        String url = Utils.getPosterUrl(movieData.getPosterPath());
        Picasso.with(context).load(url).into(holder.image);

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
