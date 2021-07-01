package com.codepath.apps.restclienttemplate.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String id;
    public User user;
    public String body;
    public String mediaUrl;
    public String createdAt;
    public boolean liked;
    public boolean retweeted;

    // Empty constructor needed by the Parceler library
    public Tweet() {

    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        // Construct new Tweet object
        Tweet tweet = new Tweet();

        // Fill in attributes of new Tweet object
        tweet.id = jsonObject.getString("id_str");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        if(jsonObject.has("full_text")) {
            tweet.body = jsonObject.getString("full_text");
        } else {
            tweet.body = jsonObject.getString("text");
        }
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities != null && entities.has("media")) {
            JSONArray media = entities.getJSONArray("media");
            tweet.mediaUrl = media.getJSONObject(0).getString("media_url_https");
            Log.i("Tweet", tweet.mediaUrl);
        } else {
            tweet.mediaUrl = null;
        }
        tweet.createdAt = jsonObject.getString("created_at");
        // Favorited is given as a string by the Twitter API
        if (jsonObject.getString("favorited").equals("true")) {
            tweet.liked = true;
        } else {
            tweet.liked = false;
        }
        tweet.retweeted = jsonObject.getBoolean("retweeted");

        return tweet;
    }

    // Convert a JSON array into a list of Tweet objects
    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i += 1) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked() {
        liked = !liked;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted() {
        retweeted = !retweeted;
    }

}
