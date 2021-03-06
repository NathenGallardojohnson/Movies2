package com.example.android.movies;

import android.content.res.Resources;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    private Utils() {
    }


    static ArrayList<MovieData> getData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and return the list of {@link MovieData}
        return extractFeatureFromJson(jsonResponse);
    }

    static List<Review> getReviews(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and return the list of {@link Data}
        return extractFeatureFromReviewJson(jsonResponse);
    }

    static List<Trailer> getVideos(String requestUrl) {

        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and return the list of {@link Data}
        return extractFeatureFromVideoJson(jsonResponse);
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }


    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the movie JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link MovieData} objects that has been built up from
     * parsing the given JSON response.
     */
    private static ArrayList<MovieData> extractFeatureFromJson(String movieJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding news to
        ArrayList<MovieData> movieData = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(movieJSON);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of items (or movies).
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");
            // For each movie in the results, create an {@link MovieData} object
            for (int i = 0; i < resultsArray.length(); i++) {

                // Get a single article at position i within the list of movies
                JSONObject currentMovie = resultsArray.getJSONObject(i);

                // Extract the value for the key called "title"
                String title = currentMovie.getString("title");

                // Extract the value for the key called "release_date"
                String releaseDate = currentMovie.getString("release_date");

                // Extract the value for the key called "poster_path"
                String posterPath = currentMovie.getString("poster_path");

                // Extract the value for the key called "vote_average"
                String voteAverage = currentMovie.getString("vote_average");

                // Extract the value for the key called "vote_average"
                String popularity = currentMovie.getString("popularity");

                // Extract the value for the key called "overview"
                String plot = currentMovie.getString("overview");

                // Extract the movie ID for the key called "id"
                String id = currentMovie.getString("id");

                // Remove backslashes from the poster url
                posterPath = posterPath.replaceAll("\\\\", "");

                // Create a new {@link MovieData} object with the title, release date, poster url, vote average, plot synopsis, and movie ID
                // from the JSON response.
                MovieData mMovie = new MovieData(title, releaseDate, posterPath, voteAverage,
                        popularity, plot, id);

                // Add the new {@link MovieData} to the list of movies.
                movieData.add(mMovie);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("Utils", "Problem parsing the movie JSON results", e);
        }

        // Return the list of movies
        return movieData;
    }

    /**
     * Return a list of {@link Review} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Review> extractFeatureFromReviewJson(String reviewJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(reviewJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding reviews to
        List<Review> reviewData = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(reviewJSON);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of items (or movies).
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");
            // For each movie in the results, create a {@link Review} object
            for (int i = 0; i < resultsArray.length(); i++) {

                // Get a single article at position i within the list of movies
                JSONObject currentReview = resultsArray.getJSONObject(i);

                // Extract the value for the key called "author"
                String author = currentReview.getString("author");

                // Extract the value for the key called "content"
                String content = currentReview.getString("content");

                // Extract the value for the key called "url"
                //String url = currentReview.getString("url");

                // Create a new {@link Data} object with the author and content
                // from the JSON response.
                Review mReview = new Review(author, content);

                // Add the new {@link MovieData} to the list of reviews.
                reviewData.add(mReview);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("Utils", "Problem parsing the review JSON results", e);
        }
        // Return the list of reviews
        return reviewData;
    }

    /**
     * Return a list of {@link Trailer} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Trailer> extractFeatureFromVideoJson(String videoJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(videoJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding videos to
        List<Trailer> videoData = new ArrayList<>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject baseJsonResponse = new JSONObject(videoJSON);

            // Extract the JSONArray associated with the key called "results",
            // which represents a list of items (or videos).
            JSONArray resultsArray = baseJsonResponse.getJSONArray("results");

            // For each video in the results, create an {@link MovieData} object
            for (int i = 0; i < resultsArray.length(); i++) {
                    // Get a single article at position i within the list of videos
                    JSONObject currentVideo = resultsArray.getJSONObject(i);

                    // Extract the value for the key called "key"
                    String key = currentVideo.getString("key");

                //Get the URL to pull the thumbnail from
                String url = getTrailerThumbnailUrl(key);

                    // Extract the value for the key called "name"
                    String name = currentVideo.getString("name");

                // Create a new {@link Data} object with the name and key
                // from the JSON response.
                Trailer mVideo = new Trailer(name, key, url);

                    // Add the new {@link MovieData} to the list of movies.
                    videoData.add(mVideo);
                }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("Utils", "Problem parsing the video JSON results", e);
        }

        // Return the list of videos
        return videoData;
    }

    static String getPosterUrl(String posterPath) {
        String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
        String SIZE = "w185";

        return (BASE_IMAGE_URL + SIZE + posterPath);
    }

    private static String getTrailerThumbnailUrl(String videoId) {
        String BASE_THUMBNAIL_URL = "https://img.youtube.com/vi/";
        String SIZE = "/0.jpg";

        return (BASE_THUMBNAIL_URL + videoId + SIZE);
    }

    private static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static Pair setThumbSize() {
        int height = getScreenHeight();
        int width = getScreenWidth();

        if (height > width) {
            width = (int) (.7 * (double) width);
            height = width / 16 * 9;
        } else {
            width = (int) (.3 * (double) width);
            height = width / 16 * 9;
        }
        //noinspection unchecked
        return new Pair(height, width);
    }

}
