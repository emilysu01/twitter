package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.User;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder>  {

    Context context;
    List<User> users;

    // Pass in the context and list of Users
    public UsersAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    // For each row, inflate the layout
    @NonNull
    @NotNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull @NotNull UsersAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        // Bind the Tweet with the ViewHolder
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // UI components
        TextView tvName;
        TextView tvScreenName;
        TextView tvBio;
        ImageView ivProfileImage;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            // Retrieve UI components
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the click
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Get the user that was clicked
                User user = users.get(position);
                // Create intent to pass user to user activity
                Intent intent = new Intent(context, UserActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                context.startActivity(intent);
            }
        }

        public void bind(User user) {
            // Set text for UI components
            tvName.setText(user.getName());
            tvScreenName.setText("@" + user.getScreenName());
            tvBio.setText(user.getBio());
            // Generate profile image
            Glide.with(context)
                    .load(user.getProfileImageUrl())
                    .circleCrop()
                    .into(ivProfileImage);
        }
    }

}