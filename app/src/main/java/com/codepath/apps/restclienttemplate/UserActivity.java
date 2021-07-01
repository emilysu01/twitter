package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class UserActivity extends AppCompatActivity {

    // Constants
    public static final String TAG = "UserActivity";

    // UI components
    TextView tvName;
    TextView tvScreenName;
    TextView tvBio;
    ImageView ivProfileImage;
    TextView tvLocation;
    TextView tvFollowing;
    TextView tvFollowers;
    RecyclerView rvTweets;

    TwitterClient client;

    List<Tweet> tweets = new ArrayList<Tweet>();
    TweetsAdapter adapter;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Set up API client object
        client = TwitterApp.getRestClient(this);

        // Unwrap user from previous activity intent
        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

        // Retrieve UI components
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBio = findViewById(R.id.tvBio);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvLocation = findViewById(R.id.tvLocation);
        tvFollowing = findViewById(R.id.tvFollow);
        tvFollowers = findViewById(R.id.tvFollowers);
        rvTweets = findViewById(R.id.rvTweets);

        // Set UI components
        setUIComponents();

        // Display user's Tweets
        populateUserTweets();
    }

    private void populateUserTweets() {
        // Retrieve user's Tweets
        client.getUserTweets(user.getScreenName(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add to list of Tweets to display
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    // Notify adapter
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "onSuccess to retrieve current user's Tweets");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve current user's Tweets: " + response, throwable);
                Log.e(TAG, Integer.toString(statusCode));
                Log.e(TAG, headers.toString());
            }
        });
    }

    private void setUIComponents() {
        // Set TextView component text
        tvName.setText(user.getName());
        tvScreenName.setText(user.getScreenName());
        tvBio.setText(user.getBio());
        tvLocation.setText(user.getLocation());
        tvFollowing.setText(user.getFollowing() + " Following");
        tvFollowers.setText(user.getFollowers() + " Followers");

        // Generate profile image
        Glide.with(getApplicationContext())
                .load(user.getProfileImageUrl())
                .circleCrop()
                .into(ivProfileImage);

        // Set onClickListener for tvFollowing and tvFollowers
        tvFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FollowingActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });
        tvFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FollowersActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });

        // Set up RecyclerView
        setUpRvTweets();
    }

    private void setUpRvTweets() {
        // Initialize adapter
        adapter = new TweetsAdapter(this, tweets);

        // Configure RecyclerView (layout, adapter, and dividers)
        LinearLayoutManager layoutManager = new LinearLayoutManager((this));
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                layoutManager.getOrientation());
        rvTweets.addItemDecoration(dividerItemDecoration);
    }
}