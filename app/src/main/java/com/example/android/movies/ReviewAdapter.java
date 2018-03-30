package com.example.android.movies;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Hardkornate on 3/22/18.
 */

public class ReviewAdapter extends ArrayAdapter<Data> {
    private final Context mContext;
    private final int layoutResourceId;
    private final List<Data> data;

    public ReviewAdapter(Context context, List<Data> data) {
        super(context, R.layout.review_item, data);
        this.layoutResourceId = R.layout.review_item;
        this.mContext = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.author = row.findViewById(R.id.author_text_view);
            holder.content = row.findViewById(R.id.content_text_view);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Data reviewData = data.get(position);
        holder.author.setText(reviewData.getName());
        holder.content.setText(reviewData.getContent());
        return row;
    }

    static class ViewHolder {
        TextView author;
        TextView content;
    }
}
