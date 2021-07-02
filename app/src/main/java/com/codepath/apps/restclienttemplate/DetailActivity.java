package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class DetailActivity extends AppCompatActivity {

    // Constants
    public static final String TAG = "DetailActivity";

    // UI components
    TextView tvName;
    TextView tvScreenName;
    ImageView ivProfileImage;
    TextView tvBody;
    ImageView ivPostImage;
    ImageView ivComment;
    ImageView ivRetweet;
    ImageView ivLike;

    Tweet tweet;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Set up API client object
        client = TwitterApp.getRestClient(this);

        // Unwrap Tweet from previous activity intent
        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        // Retrieve UI components
        tvName = (TextView) findViewById(R.id.tvName);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvBody = (TextView) findViewById(R.id.tvFollowing);
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        ivComment = (ImageView) findViewById(R.id.ivComment);
        ivRetweet = (ImageView) findViewById(R.id.ivRetweet);
        ivLike = (ImageView) findViewById(R.id.ivLike);
        // If Tweet is already liked, display filled heart icon
        if (tweet.isLiked()) {
            ivLike.setImageResource(R.drawable.ic_vector_heart);
        }
        // If Tweet is already retweeted, display bolded heart icon
        if (tweet.isRetweeted()) {
            ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
        }

        // Set up UI components
        setUIComponents();

    }

    public void setUIComponents() {
        // Set TextView components text
        tvName.setText(tweet.getUser().getName());
        tvScreenName.setText("@" + tweet.getUser().getScreenName());
        tvBody.setText(tweet.getBody());

        // Generate images
        Glide.with(getApplicationContext())
                .load(tweet.getUser().getProfileImageUrl())
                .circleCrop()
                .into(ivProfileImage);
        Glide.with(getApplicationContext())
                .load(tweet.getMediaUrl())
                .into(ivPostImage);

        // Set up onClickListeners for UI components that lead to a profile page
        tvName.setOnClickListener(new ProfileClickListener());
        tvScreenName.setOnClickListener(new ProfileClickListener());
        ivProfileImage.setOnClickListener(new ProfileClickListener());

        // Set up onClickListeners for comment, retweet and like
        setOnClickListeners();
    }

    private void setOnClickListeners() {
        // Get the location of click
        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create intent to pass the Tweet being replied to
                Intent intent = new Intent(DetailActivity.this, ComposeActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                // Send previous activity so compose activity knows whether or not to show a username
                intent.putExtra("previousActivity", "DetailActivity");
                startActivity(intent);
            }
        });

        // Retweet or unretweet a Tweet
        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tweet.isRetweeted()) {
                    // Retweet
                    client.retweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to retweet");
                        // Update retweet icon to be bolded
                        ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
                        // Set the retweeted attribute to be true
                        tweet.setRetweeted();
                    }
                    @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to retweet: " + response, throwable);
                        }
                    });
                } else {
                    // Unretweet
                    client.unretweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to unretweet");
                            // Update retweet icon to be unbolded
                            ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
                            // Set the retweeted attribute to be false
                            tweet.setRetweeted();
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to unretweet: " + response, throwable);
                        }
                    });
                }
            }
        });

        // Like or unlike a Tweet
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tweet.isLiked()) {
                    // Like
                    client.likeTweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to like");
                        // Update heart icon to be filled
                        ivLike.setImageResource(R.drawable.ic_vector_heart);
                        // Set the liked attribute to be true
                        tweet.setLiked();
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to like: " + response, throwable);
                    }
                    });
                } else {
                    // Unlike
                    client.unlikeTweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to unlike");
                            // Update heart icon to be empty
                            ivLike.setImageResource(R.drawable.ic_vector_heart_stroke);
                            // Set the liked attribute to be false
                            tweet.setLiked();
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to unlike: " + response, throwable);
                        }
                    });
                }
            }
        });
    }

    // onClickListener for transitioning to user pages
    private class ProfileClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            User user = tweet.getUser();
            Intent intent = new Intent(DetailActivity.this, UserActivity.class);
            intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
            startActivity(intent);
        }
    }
}