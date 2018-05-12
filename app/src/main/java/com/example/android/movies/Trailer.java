package com.example.android.movies;

class Trailer {

    private final String trailer_name;
    private final String trailer_key;

    private final String trailer_url;

    Trailer(String trailer_name, String trailer_key, String trailer_url) {
        this.trailer_name = trailer_name;
        this.trailer_key = trailer_key;
        this.trailer_url = trailer_url;
    }

    public String getTrailer_key() {
        return trailer_key;
    }

    public String getTrailer_name() {
        return trailer_name;
    }

    public String getTrailer_url() {
        return trailer_url;
    }
}
