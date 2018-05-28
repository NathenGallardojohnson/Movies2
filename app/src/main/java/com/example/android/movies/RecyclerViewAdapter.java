package com.example.android.movies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private static ArrayList<MovieData> movieList;
    private Context context;
    static private RecyclerView.OnClickListener mClickListener;

    RecyclerViewAdapter(Context context, ArrayList<MovieData> movieList) {
        RecyclerViewAdapter.movieList = movieList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);

        return new RecyclerViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, final int position) {
        MovieData movieData = movieList.get(position);
        holder.imageTitle.setText(movieData.getTitle());
        holder.itemView.setTag(position);
        String url = Utils.getPosterUrl(movieData.getPosterPath());
        Picasso.with(context).load(url).placeholder(R.drawable.user_placeholder)
                .error(R.drawable.user_placeholder_error).into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick(view);
            }
        });

    }


    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }

    public void clear(){
        final int size = movieList.size();
        movieList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void addAll(ArrayList<MovieData> data){
        movieList.clear();
        movieList.addAll(data);
        notifyDataSetChanged();
    }

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        TextView imageTitle;
        ImageView imageView;

        RecyclerViewHolder(View itemView) {
            super(itemView);
            imageTitle = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.image);
        }

    }
}

