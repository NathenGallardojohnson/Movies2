package com.example.android.movies;

import java.net.URL;

/**
 * Created by Hardkornate on 3/22/18.
 */

public class Data {
    private String name;
    private String content = " ";
    private URL url;
    private int type;

    public Data() {
        this.name = name;
        this.url = url;
        this.content = content;
        type = 0;
    }

    public Data(String name, URL url) {
        this.name = name;
        this.url = url;
        this.content = " ";
        this.type = 2;
    }

    public Data(String name, String content) {
        this.name = name;
        this.content = content;
        this.url = null;
        this.type = 1;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public void setName(String mName) {
        this.name = mName;
    }

    public void setContent(String mContent) {
        this.content = mContent;
    }

    public int getType() {
        return type;
    }

    public void setType(int mType) {
        this.type = mType;
    }

    public URL getURL() {
        return url;
    }

    public void setUrl(URL mUrl) {
        this.url = mUrl;
    }

    public boolean isEmpty() {
        if (getType() == 0) {
            return false;
        } else {
            return true;
        }
    }
}
