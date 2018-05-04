package com.example.android.movies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class TrailerActivity extends AppCompatActivity {

    private final String YOUTUBEKEY = ("?api_key=" + getString(R.string.you_tube_key));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer);
    }
}
