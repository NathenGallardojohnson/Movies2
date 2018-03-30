package com.example.android.movies;

import java.net.URL;

/**
 * Created by Hardkornate on 3/22/18.
 */

public class Data {
    private String name;
    private String content = " ";
    private URL url;
    private String type;

    public Data(String name, URL url) {
        this.name = name;
        this.url = url;
        type = "videoType";
    }

    public Data(String name, String content) {
        this.name = name;
        this.content = content;
        type = "reviewType";
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    //public void setName() { this.name = name; }

    public String getType() {
        return type;
    }

    //public void setType() { this.type = type; }

    public URL getURL() {
        return url;
    }

    //public void setUrl() { this.url = url; }

    public boolean isEmpty() {
        if (getType() == "videoType" || getType() == "reviewType") {
            return false;
        }
        return true;
    }
}
