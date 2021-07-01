package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class User {

    public String id;
    public String name;
    public String screenName;
    public String profileImageUrl;
    public String bio;
    public int following;
    public int followers;
    public String location;

    // Empty constructor needed by the Parceler library
    public User() {

    }
    public static User fromJson(JSONObject jsonObject) throws JSONException {
        // Construct new User object
        User user = new User();

        // Fill in attributes of new User object
        user.id = jsonObject.getString("id_str");
        user.name = jsonObject.getString("name");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url_https");
        user.bio = jsonObject.getString("description");
        user.followers = jsonObject.getInt("followers_count");
        user.following = jsonObject.getInt("friends_count");
        user.location = jsonObject.getString("location");

        return user;
    }

    // Convert a JSON array into a list of User objects
    public static List<User> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i += 1) {
            users.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return users;
    }

    // Getter methods
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBio() {
        return bio;
    }

    public int getFollowing() {
        return following;
    }

    public int getFollowers() {
        return followers;
    }

    public String getLocation() {
        return location;
    }

}
