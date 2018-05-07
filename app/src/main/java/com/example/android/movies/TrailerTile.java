package com.example.android.movies;

import com.google.android.youtube.player.YouTubeThumbnailView;

class TrailerTile {

    private YouTubeThumbnailView thumbnail;
    private String trailer_title;
    private String elementIdx;
    private String trailer_key;

    public TrailerTile(YouTubeThumbnailView thumbnail, String trailer_title, String elementIdx, String trailer_key) {
        this.thumbnail = thumbnail;
        this.trailer_title = trailer_title;
        this.elementIdx = elementIdx;
        this.trailer_key = trailer_key;
    }

    public YouTubeThumbnailView getThumbnail() {
        return thumbnail;
    }

    public String getTrailer_title() {
        return trailer_title;
    }

    public String getElementIdx() {
        return elementIdx;
    }

    public void setElementIdx(String elementIdx) {
        this.elementIdx = elementIdx;
    }


    public String getTrailer_key() {
        return trailer_key;
    }

    public void setTrailer_key(String trailer_key) {
        this.trailer_key = trailer_key;
    }


}
