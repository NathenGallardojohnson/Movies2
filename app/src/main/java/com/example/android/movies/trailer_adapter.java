package com.example.android.movies;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.youtube.player.YouTubeThumbnailLoader;


public class trailer_adapter extends BaseAdapter implements YouTubeThumbnailLoader {
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void setOnThumbnailLoadedListener(OnThumbnailLoadedListener onThumbnailLoadedListener) {

    }

    @Override
    public void setVideo(String s) {

    }

    @Override
    public void setPlaylist(String s) {

    }

    @Override
    public void setPlaylist(String s, int i) {

    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

    @Override
    public void first() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void release() {

    }
}
