package com.example.android.movies;

public class Review {

    private String Author;
    private String Content;

    Review() {

    }

    Review(String author, String content) {
        this.Author = author;
        this.Content = content;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

}
