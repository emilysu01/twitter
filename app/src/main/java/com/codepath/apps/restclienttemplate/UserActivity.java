package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.User;

import org.parceler.Parcels;

public class UserActivity extends AppCompatActivity {

    TextView tvScreenName;
    TextView tvName;
    TextView tvFollowing;
    TextView tvFollowers;
    ImageView ivProfileImage;

    TwitterClient client;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        client = TwitterApp.getRestClient(this);
        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

        tvScreenName = (TextView) findViewById(R.id.tvName);
        tvName = (TextView) findViewById(R.id.tvScreenName);
        tvFollowing = (TextView) findViewById(R.id.tvBody);
        tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);

        tvFollowing.setText(String.format("%d Following", user.getFollowing()));
        tvFollowers.setText(String.format("%d Followers", user.getFollowers()));
        Glide.with(getApplicationContext())
                .load(user.getProfileImageUrl())
                .into(ivProfileImage);


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
    }
}