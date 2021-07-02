package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    Context context;
    List<Tweet> tweets;

    TwitterClient client;

    // Pass in the context and list of Tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
        client = TwitterApp.getRestClient(context);
    }

    // For each row, inflate the layout
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet = tweets.get(position);
        // Bind the Tweet with the ViewHolder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Define a ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Constants
        public static final String TAG = "TweetsAdapter";
        // Constants for parsing reltive time
        private static final int SECOND_MILLIS = 1000;
        private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

        // UI components
        public TextView tvName;
        public TextView tvScreenName;
        public ImageView ivProfileImage;
        public TextView tvBody;
        public TextView tvTime;
        public ImageView ivPostImage;
        public ImageView ivComment;
        public ImageView ivRetweet;
        public ImageView ivLike;

        public Tweet tweet;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            // Retrieve UI components
            tvName = itemView.findViewById(R.id.tvScreenName);
            tvScreenName = itemView.findViewById(R.id.tvName);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvFollowing);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            ivComment = itemView.findViewById(R.id.ivComment);
            ivRetweet = itemView.findViewById(R.id.ivRetweet);
            ivLike = itemView.findViewById(R.id.ivLike);
        }

        public void setUpUIComponents() {
            // Set up onClickListeners for UI components that lead to a profile page
            tvName.setOnClickListener(new ProfileClickListener());
            tvScreenName.setOnClickListener(new ProfileClickListener());
            ivProfileImage.setOnClickListener(new ProfileClickListener());
            // Set up onClickListeners for UI components that lead to a Tweet page
            tvBody.setOnClickListener(new TweetClickListener());
            tvTime.setOnClickListener(new TweetClickListener());
            ivPostImage.setOnClickListener(new TweetClickListener());
            // Set up onClickListeners for comment, retweet and like
            setOnClickListeners();
        }

        public void bind(Tweet tweet) {
            // Set TextView components text
            tvName.setText(tweet.user.getName());
            tvScreenName.setText("@" + tweet.user.screenName);
            tvBody.setText(tweet.body);
            tvTime.setText("• " + getRelativeTimeAgo(tweet.createdAt));
            // Generate profile image and post image
            Glide.with(context)
                    .load(tweet.user.profileImageUrl)
                    .circleCrop()
                    .into(ivProfileImage);
            if (tweet.mediaUrl != null) {
                ivPostImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(tweet.mediaUrl)
                        .into(ivPostImage);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }
            // Set up onClickListeners for UI components
            setUpUIComponents();
        }

        // Generate relative time for Tweets (e.g.: Tweet was made 1 min ago)
        public String getRelativeTimeAgo(String rawJsonDate) {
            String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
            sf.setLenient(true);
            try {
                long time = sf.parse(rawJsonDate).getTime();
                long now = System.currentTimeMillis();
                final long diff = now - time;
                if (diff < MINUTE_MILLIS) {
                    return "just now";
                } else if (diff < 2 * MINUTE_MILLIS) {
                    return "a minute ago";
                } else if (diff < 50 * MINUTE_MILLIS) {
                    return diff / MINUTE_MILLIS + "m";
                } else if (diff < 90 * MINUTE_MILLIS) {
                    return "an hour ago";
                } else if (diff < 24 * HOUR_MILLIS) {
                    return diff / HOUR_MILLIS + "h";
                } else if (diff < 48 * HOUR_MILLIS) {
                    return "yesterday";
                } else {
                    return diff / DAY_MILLIS + "d";
                }
            } catch (ParseException e) {
                Log.i("TweetsAdapter", "getRelativeTimeAgo failed");
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void onClick(View v) {
            // Get the location of click
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Get the Tweet that was clicked
                Tweet tweet = tweets.get(position);
                // Create intent to pass activity to detail activity
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }

        // onClickListener for transitioning to user pages
        private class ProfileClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                // Get the location of click
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Get the user that was clicked
                    User user = tweets.get(position).getUser();
                    // Create intent to pass activity to user activity
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                    context.startActivity(intent);
                }
            }
        }

        // onClickListener for transitioning to Tweet pages
        private class TweetClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                // Get the location of click
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Get the Tweet that was clicked
                    Tweet tweet = tweets.get(position);
                    // Create intent to pass activity to detail activity
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    context.startActivity(intent);
                }
            }
        }

        private void setOnClickListeners() {
            // Get the location of click
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Get the Tweet that was clicked
                tweet = tweets.get(position);
                // Reply to a tWEET
                ivComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create intent to pass the Tweet being replied to
                        Intent intent = new Intent(context, ComposeActivity.class);
                        intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        // Send previous activity so compose activity knows whether or not to show a username
                        intent.putExtra("previousActivity", "DetailActivity");
                        context.startActivity(intent);
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
        }
    }

    // Clean all elements of the RecyclerView
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items to the RecyclerView
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }
}
