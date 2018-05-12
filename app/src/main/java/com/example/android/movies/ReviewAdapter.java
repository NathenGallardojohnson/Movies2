package com.example.android.movies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

class ReviewAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.reviews = reviews;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Object getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ViewHolder holder;
        if (view == null) {
            v = inflater.inflate(R.layout.review_item, parent, false);
            holder = new ViewHolder();
            holder.authorView = v.findViewById(R.id.author_text_view);
            holder.contentView = v.findViewById(R.id.content_text_view);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        Review r = reviews.get(position);
        holder.authorView.setText(r.getAuthor());
        holder.contentView.setText(r.getContent());
        return v;
    }

    public void setReviews() {
        reviews.addAll(reviews);
        notifyDataSetChanged();
    }

    public void clear() {
        notifyDataSetInvalidated();
    }

    class ViewHolder {
        TextView authorView;
        TextView contentView;
    }
}
