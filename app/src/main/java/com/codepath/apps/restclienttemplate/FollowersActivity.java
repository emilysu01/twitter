package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class FollowersActivity extends AppCompatActivity {

    // Constants
    public static final String TAG = "FollowersActivity";

    TwitterClient client;

    List<User> users = new ArrayList<>();
    UsersAdapter adapter;

    User user;

    // UI components
    RecyclerView rvUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        // Set up API client object
        client = TwitterApp.getRestClient(this);

        // Unwrap user from previous activity intent
        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

        // Retrieve UI components
        rvUsers = findViewById(R.id.rvTweets);

        // Initialize adapter
        adapter = new UsersAdapter(this, users);

        // Configure RecyclerView (layout, adapter, and dividers)
        LinearLayoutManager layoutManager = new LinearLayoutManager((this));
        rvUsers.setLayoutManager(layoutManager);
        rvUsers.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvUsers.getContext(),
                layoutManager.getOrientation());
        rvUsers.addItemDecoration(dividerItemDecoration);

        // Displays users
        populateUsers();
    }

    private void populateUsers() {
        // Retrieve current user's followers
        client.getUserFollowers(user.getId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    // Unpack JSON object (get JSON array corresponding to key users)
                    JSONArray allFollowing = jsonObject.getJSONArray("users");
                    // Add to list of users to display
                    users.addAll(User.fromJsonArray(allFollowing));
                    // Notify adapter
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "onSuccess to retrieve current user's followers");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: ", e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retieve current user's followers: " + response, throwable);
            }
        });
    }
}