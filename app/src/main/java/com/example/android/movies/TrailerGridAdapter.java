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


class TrailerGridAdapter extends ArrayAdapter<Trailer> {
    private final Context context;
    private final int layoutResourceId;
    private final List<Trailer> trailers;

    TrailerGridAdapter(Context context, List<Trailer> trailers) {
        super(context, R.layout.trailer_item, trailers);
        this.layoutResourceId = R.layout.trailer_item;
        this.context = context;
        this.trailers = trailers;
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
            holder.imageTitle = row.findViewById(R.id.textView_title);
            holder.image = row.findViewById(R.id.trailer_thumbnail);
            holder.image.setScaleType(ImageView.ScaleType.MATRIX);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Trailer trailer = trailers.get(position);
        String name = trailer.getTrailer_name();
        holder.imageTitle.setText(name);
        String url = trailer.getTrailer_url();
        android.support.v4.util.Pair thumbSize = Utils.setThumbSize();
        int height = (int) thumbSize.first;
        int width = (int) thumbSize.second;
        Picasso.with(context).load(url).resize(width, height).placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder_error).into(holder.image);

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
