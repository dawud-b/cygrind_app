package com.example.androidexample.Friends;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.User;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for displaying a list of users in a RecyclerView for search functionality.
 * <p>
 * Provides functionality to filter users and handle friend request actions.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.UserViewHolder> {

    private List<User> userList;               // Original list of users
    private List<User> userListFiltered;       // Filtered list based on search query
    private OnFriendRequestClickListener listener; // Listener for friend request button click

    /**
     * Constructor to initialize the adapter with a list of users and a listener for friend requests.
     *
     * @param userList List of all available users.
     * @param listener Callback interface for friend request actions.
     */
    public SearchAdapter(List<User> userList, OnFriendRequestClickListener listener) {
        this.userList = userList;
        this.userListFiltered = new ArrayList<>(userList);
        this.listener = listener;
    }

    /**
     * Inflates the layout for each user item in the list.
     *
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view.
     * @return A new UserViewHolder containing the inflated view.
     */
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_user, parent, false);
        return new UserViewHolder(itemView);
    }

    /**
     * Binds the user data to the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the user in the filtered list.
     */
    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userListFiltered.get(position);
        holder.usernameTextView.setText(user.getUsername());

        holder.friendRequestButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRequestClick(user);
            }
        });
    }

    /**
     * Returns the number of users in the filtered list.
     *
     * @return The count of items.
     */
    @Override
    public int getItemCount() {
        return userListFiltered.size();
    }

    /**
     * Filters the user list based on a query string.
     * Updates the filtered list and notifies the adapter of changes.
     *
     * @param query The search query used to filter users.
     */
    public void filterList(String query) {
        userListFiltered.clear();
        if (query.isEmpty()) {
            userListFiltered.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getUsername().toLowerCase().contains(query.toLowerCase())) {
                    userListFiltered.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class to represent each user item in the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        ImageButton friendRequestButton;

        /**
         * Constructor for initializing the views for the user item.
         *
         * @param itemView The view of the user item.
         */
        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            friendRequestButton = itemView.findViewById(R.id.friendRequestButton);
        }
    }

    /**
     * Interface definition for a callback to be invoked when a friend request button is clicked.
     */
    public interface OnFriendRequestClickListener {
        /**
         * Called when the friend request button is clicked.
         *
         * @param user The user associated with the clicked button.
         */
        void onFriendRequestClick(User user);
    }
}
