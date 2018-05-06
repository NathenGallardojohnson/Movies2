package com.example.android.movies;

public class Trailer {


    private String trailer_name;
    private String trailer_key;

    public Trailer(String trailer_name, String trailer_key) {
        this.trailer_name = trailer_name;
        this.trailer_key = trailer_key;
    }

    public String getTrailer_key() {
        return trailer_key;
    }

    public String getTrailer_name() {
        return trailer_name;
    }
}
