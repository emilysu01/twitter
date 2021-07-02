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

    public static final String TAG = "DetailActivity";

    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvName;
    ImageView ivPostImage;
    Tweet tweet;
    ImageView ivComment;
    ImageView ivRetweet;
    ImageView ivLike;

    TwitterClient client;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        client = TwitterApp.getRestClient(this);

        tweet = (Tweet) Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));
        Log.i("current tweet", tweet.toString());

        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        tvBody = (TextView) findViewById(R.id.tvFollowing);
        tvName = (TextView) findViewById(R.id.tvName);
        tvScreenName = (TextView) findViewById(R.id.tvScreenName);
        ivPostImage = (ImageView) findViewById(R.id.ivPostImage);
        ivComment = (ImageView) findViewById(R.id.ivComment);
        ivRetweet = (ImageView) findViewById(R.id.ivRetweet);
        ivLike = (ImageView) findViewById(R.id.ivLike);
        if (tweet.isLiked()) {
            ivLike.setImageResource(R.drawable.ic_vector_heart);
        }
        if (tweet.isRetweeted()) {
            ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
        }
        // ivShare = (ImageView) findViewById(R.id.ivShare);

        tvScreenName.setText("@" + tweet.getUser().getScreenName());
        tvName.setText(tweet.getUser().getName());
        tvBody.setText(tweet.getBody());
        Glide.with(getApplicationContext())
                .load(tweet.getUser().getProfileImageUrl())
                .circleCrop()
                .into(ivProfileImage);
        Glide.with(getApplicationContext())
                .load(tweet.getMediaUrl())
                .into(ivPostImage);

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = tweet.getUser();
                Intent intent = new Intent(DetailActivity.this, UserActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });

        tvScreenName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = tweet.getUser();
                Intent intent = new Intent(DetailActivity.this, UserActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = tweet.getUser();
                Intent intent = new Intent(DetailActivity.this, UserActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });



        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!tweet.isRetweeted()) {
                    client.retweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to retweet");
                            ivRetweet.setImageResource(R.drawable.ic_vector_retweet);
                            tweet.setRetweeted();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                        }
                    });
                } else {
                    client.unretweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to unretweet");
                            ivRetweet.setImageResource(R.drawable.ic_vector_retweet_stroke);
                            tweet.setRetweeted();
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to unretweet", throwable);
                        }
                    });
                }

            }
        });
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tweet.isLiked()) {
                    client.unlikeTweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to unlike Tweet");
                            ivLike.setImageResource(R.drawable.ic_vector_heart_stroke);
                            tweet.setLiked();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to unlike Tweet", throwable);
                        }
                    });
                } else {
                    client.likeTweet(tweet.getId() + "", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to like Tweet");
                            ivLike.setImageResource(R.drawable.ic_vector_heart);
                            tweet.setLiked();
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to like Tweet", throwable);
                        }
                    });
                }

            }
        });

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ComposeActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                intent.putExtra("previousActivity", "DetailActivity");
                startActivity(intent);
            }
        });

    }
}