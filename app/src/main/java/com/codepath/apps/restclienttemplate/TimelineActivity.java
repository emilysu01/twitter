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

    public int lowestMaxId;

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

        // Set up onClickListeners for compose button and swipeContainer
        setUpComposeButton();
        setUpSwipeContainer();

        // Display Tweets
        populateHomeTimeline();
    }

    //TODO
    public void loadNextDataFromApi() {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`


        client.getHomeTimeline(lowestMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i("TimelineActivity", "onSuccess");
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(0, Tweet.fromJsonArray(jsonArray));
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + response);
            }
        });

    }

    //TODO
    public void fetchTimelineAsync(int page) {
        // Retrive user's timeline
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(lowestMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // Remember to CLEAR OUT old items before appending in the new ones
                adapter.clear();
                // ...the data has come back, add new items to your adapter...
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.addAll(tweets);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "JSON exception", e);
                    e.printStackTrace();
                }

                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d("DEBUG", "Fetch timeline error: " + response);
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
            if (lowestMaxId == 0) {
                lowestMaxId = Integer.parseInt(tweet.getId());
            } else {
                if (Integer.parseInt(tweet.getId()) < lowestMaxId) {
                    lowestMaxId = Integer.parseInt(tweet.getId());
                }
            }
            // Update the RecyclerView with the Tweet by modifying data source, notifying adapter, and scrolling to top
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        // Retrieve current user's timeline
        client.getHomeTimeline(lowestMaxId, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONArray jsonArray = json.jsonArray;
                try {
                    // Add to list of Tweets to display
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
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