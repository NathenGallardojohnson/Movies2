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


public class VideoAdapter extends ArrayAdapter<Data> {
    private final Context context;
    private final int layoutResourceId;
    private final List<Data> data;

    public VideoAdapter(Context context, List<Data> data) {
        super(context, R.layout.video_item, data);
        this.layoutResourceId = R.layout.video_item;
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
            holder.key = row.findViewById(R.id.key_text_view);
            holder.name = row.findViewById(R.id.name_text_view);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Data videoData = data.get(position);
        holder.key.setText((videoData.getURL()).toString());
        holder.name.setText(videoData.getName());
        return row;
    }

    static class ViewHolder {
        //TODO key textview is placeholder for testing. Change layout later to just use the URL as the link when clicked
        TextView key;
        TextView name;
    }
}