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
        private static final int SECOND_MILLIS = 1000;
        private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

        public static final String TAG = "TweetsAdapter";

        // UI components
        TextView tvName;
        TextView tvScreenName;
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvTime;
        ImageView ivPostImage;
        ImageView ivComment;
        ImageView ivRetweet;
        ImageView ivLike;

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

            // Set up onClickListeners for UI components
            setUpUIComponents();
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
            setOnClickListeners();
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
                // Get the Tweet at position in the list
                Tweet tweet = tweets.get(position);
                // Change activity to Tweet detail activity
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                context.startActivity(intent);
            }
        }

        private class ProfileClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    User user = tweets.get(position).getUser();
                    Intent intent = new Intent(context, UserActivity.class);
                    intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                    context.startActivity(intent);
                }
            }
        }

        private class TweetClickListener implements View.OnClickListener {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Tweet tweet = tweets.get(position);
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    context.startActivity(intent);
                }
            }
        }
        public Tweet tweet;
        private void setOnClickListeners() {

            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                tweet = tweets.get(position);
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
                        Intent intent = new Intent(context, ComposeActivity.class);
                        intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                        intent.putExtra("previousActivity", "DetailActivity");
                        context.startActivity(intent);
                    }
                });
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

}
