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

    User user;
    TwitterClient client;
    RecyclerView rvUsers;
    List<User> users;
    UsersAdapter adapter;

    public static final String TAG = "FollowersActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        client = TwitterApp.getRestClient(this);

        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

        // Find the RecyclerView
        rvUsers = findViewById(R.id.rvUsers);
        // Initialize the list of Tweets and adapter
        users = new ArrayList<>();
        adapter = new UsersAdapter(this, users);
        // Configure the RecyclerView (layout and adapter)
        LinearLayoutManager layoutManager = new LinearLayoutManager((this));
        rvUsers.setLayoutManager(layoutManager);
        rvUsers.setAdapter(adapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvUsers.getContext(),
                layoutManager.getOrientation());
        rvUsers.addItemDecoration(dividerItemDecoration);

        populateUsers();

    }

    private void populateUsers() {
        client.getUserFollowers(user.getId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess " + json.toString());
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray allFollowing = jsonObject.getJSONArray("users");
                    Log.i(TAG, "IMPORTANT " + allFollowing.toString());
                    users.addAll(User.fromJsonArray(allFollowing));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure " + response, throwable);
            }
        });
    }
}