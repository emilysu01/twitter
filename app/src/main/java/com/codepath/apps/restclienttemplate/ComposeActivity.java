package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    // Constants
    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    // UI components
    Button btnCancel;
    Button btnTweet;
    EditText etCompose;
    ImageView ivProfileImage;

    User user;
    Tweet tweet;

    // API client object
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Set up API client object
        client = TwitterApp.getRestClient(this);

        // Retrieve UI components
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        btnCancel = findViewById(R.id.btnCancel);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        /** Check the previous activity:
            - If the previous activity is the timeline, then the user is creating a new Tweet
            - If the previous activity is a Tweet details page, then the user is replying to a Tweet
              (will include the original Tweet's username in the editText field) **/
        String previousActivity = getIntent().getStringExtra("previousActivity");
        if (previousActivity.equals("DetailActivity")) {
            tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
            etCompose.setText("@" + tweet.getUser().getScreenName() + " ");
        } else {
            etCompose.setHint("What's happening?");
        }

        // Retrieve the user that's currently logged in
        client.getProfile(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonArray = json.jsonObject;
                try {
                    user = User.fromJson(jsonArray);
                    // Displays the user's profile picture
                    // Update ivProfileImage in the JSON response handler because of asynchronous API calls
                    Glide.with(getApplicationContext())
                            .load(user.getProfileImageUrl())
                            .circleCrop()
                            .into(ivProfileImage);
                    Log.i(TAG, "onSuccess to retrieve the current user");
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException: ", e);
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "onFailure to retrieve current user: " + response, throwable);
            }
        });

        // Set onClickListeners for cancel and tweet buttons
        setUpCancelButton();
        setUpTweetButton();

    }

    // Set up setOnClickListener for cancel button
    public void setUpCancelButton() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    // Set up setOnClickListener for tweet button
    public void setUpTweetButton() {
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = etCompose.getText().toString();
                // Case 1: Tweet is empty
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your Tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                // Case 2: Tweet is too long
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your Tweet is too long", Toast.LENGTH_LONG).show();
                    return;
                }

                // Case 3: Tweet is ok
                // Make an API call to publish the Tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            // Make a new intent to pass information back to parent activity
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // Set result code and bundle data for response
                            setResult(RESULT_OK, intent);
                            finish();
                            Log.i(TAG, "onSuccess: Published Tweet says: " + tweet.body);
                        } catch (JSONException e) {
                            Log.e(TAG, "JSONException: ", e);
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish Tweet: " + response, throwable);
                    }
                });
            }
        });
    }
}