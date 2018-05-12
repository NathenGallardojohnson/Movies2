package com.example.android.movies;

class Review {

    private final String Author;
    private final String Content;

    Review(String author, String content) {
        this.Author = author;
        this.Content = content;
    }

    public String getAuthor() {
        return Author;
    }

    public String getContent() {
        return Content;
    }

}
