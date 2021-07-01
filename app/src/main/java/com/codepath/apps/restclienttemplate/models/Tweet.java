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

    public String body;
    public String createdAt;
    public User user;
    public String mediaUrl;
    public String id;
    public boolean liked;
    public boolean retweeted;
    // public Entity entity;

    // Empty constructor needed by the Parceler library
    public Tweet() {

    }

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        JSONObject entities = jsonObject.getJSONObject("entities");
        tweet.id = jsonObject.getString("id_str");
        if (jsonObject.getString("favorited").equals("true")) {
            tweet.liked = true;
        } else {
            tweet.liked = false;
        }
        if (entities != null && entities.has("media")) {
            JSONArray media = entities.getJSONArray("media");
            tweet.mediaUrl = media.getJSONObject(0).getString("media_url_https");
            Log.i("Tweet", tweet.mediaUrl);
        } else {
            tweet.mediaUrl = null;
        }
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        // tweet.entity = Entity.fromJson(jsonObject.getJSONObject("entities"));
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i += 1) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getId() {
        return id;
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
