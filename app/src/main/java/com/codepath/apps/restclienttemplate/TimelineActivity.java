package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    // Constants
    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    // UI components and helpers
    RecyclerView rvTweets;
    FloatingActionButton btnCompose;
    MenuItem miActionProgressItem;
    private SwipeRefreshLayout swipeContainer;

    TwitterClient client;

    List<Tweet> tweets = new ArrayList<>();
    TweetsAdapter adapter;

    User user;

    // Store the oldest Tweet that is currently in the RecyclerView (for infinite scrolling)
    public long max_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Set up API client object
        client = TwitterApp.getRestClient(this);

        // Retrieve UI components
        rvTweets = findViewById(R.id.rvTweets);
        btnCompose = findViewById(R.id.btnCompose);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // Initialize adapter
        adapter = new TweetsAdapter(this, tweets);

        // Set up RecyclerView and onClickListeners for compose button and swipeContainer
        setUpRecyclerView();
        setUpComposeButton();
        setUpSwipeContainer();

        // Display Tweets
        populateHomeTimeline();
    }

    public void setUpRecyclerView() {
        // Configure RecyclerView (layout, adapter, dividers, and scrolling)
        LinearLayoutManager layoutManager = new LinearLayoutManager((this));
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                layoutManager.getOrientation());
        rvTweets.addItemDecoration(dividerItemDecoration);
        // Triggered only when new data needs to be appended to the list
        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                loadNextDataFromApi();
            }
        };
        rvTweets.addOnScrollListener(scrollListener);
    }

    // Load data for infinite scrolling
    public void loadNextDataFromApi() {
        // Retrieve the Tweets before max_id (with id < max_id)
        client.getRefreshedTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add to list of Tweets to display
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    // Notify adapter
                    adapter.notifyDataSetChanged();
                    // Update max_id to be the oldest Tweet currently in the RecyclerView
                    max_id = tweets.get(tweets.size() - 1).getId();
                    Log.i(TAG, "onSuccess to retrieve Tweets for infinite scroll");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure to retrieve Tweets for infinite scroll" + response, throwable);
            }
        });

    }

    // Load Tweets for refresh (25 Tweets, this limit is set in API call)
    public void fetchTimelineAsync(int page) {
        // Retrieve Tweets in the user's home timeline
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // Clear out old items in the adapter
                adapter.clear();
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add to list of Tweets to display
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.addAll(tweets);
                    // Notify adapter
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "onSuccess to retrieve Tweets for refresh");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    e.printStackTrace();
                }

                // Call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve Tweets for refresh: " + response, throwable);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (adds items to the action bar if it's present)
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Must return true for the menu to be displayed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Log out
        if (item.getItemId() == R.id.miLogout) {
            client.clearAccessToken();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        // resultCode is defined by Android to make sure the child activity has finished successfully
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Unwrap Tweet from previous activity intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Add a Tweet to display
            tweets.add(0, tweet);
            // Notify adapter
            adapter.notifyItemInserted(0);
            // Scroll to the top of the feed
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        // Retrieve current user's timeline
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add to list of Tweets to display
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    max_id = tweets.get(tweets.size() - 1).getId();
                    // Notify adapter
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "onSuccess to retrieve current user's timeline");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve current user's timeline: " + response, throwable);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // Show progress item
    public void showProgressBar() {
        miActionProgressItem.setVisible(true);
    }

    // Hide progress item
    public void hideProgressBar() {
        miActionProgressItem.setVisible(false);
    }

    // Set up setOnClickListener for compose button
    public void setUpComposeButton(){
        btnCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                intent.putExtra("previousActivity", "TimelineActivity");
                intent.putExtra("user", Parcels.wrap(user));
                startActivity(intent);
            }
        });
    }

    // Set up refresh listener which triggers new data loading
    public void setUpSwipeContainer() {
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        // Configure refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }
}