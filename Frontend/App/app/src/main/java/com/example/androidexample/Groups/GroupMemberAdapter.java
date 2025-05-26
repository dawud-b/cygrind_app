package com.example.androidexample.Groups;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.User;

import java.util.List;

/**
 * Adapter class for displaying group members in a RecyclerView.
 * This adapter manages a list of users and displays them with options to remove or send messages (depending on the role).
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.UserViewHolder> {

    private List<User> userList;   // List of group members
    private boolean isLeader;      // Flag to determine if the current user is the group leader
    private onRemoveClickListener listener; // Listener for button clicks (remove or message)

    /**
     * Interface to handle click events for removing a user or sending a message.
     */
    public interface onRemoveClickListener {
        /**
         * Called when the remove button for a user is clicked.
         *
         * @param user The user to be removed from the group.
         */
        void onRemoveClick(User user);

        /**
         * Called when the message button for a user is clicked.
         *
         * @param user The user to whom a message will be sent.
         */
        void onMessageClick(User user);
    }

    /**
     * Constructor to initialize the adapter with the list of users, leader status, and listener for click events.
     *
     * @param userList List of users to display in the RecyclerView.
     * @param isLeader Boolean flag indicating if the current user is a group leader.
     * @param listener Listener to handle remove and message click events.
     */
    public GroupMemberAdapter(List<User> userList, boolean isLeader, onRemoveClickListener listener) {
        this.userList = userList;
        this.isLeader = isLeader;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder to represent a single item in the RecyclerView.
     *
     * @param parent The parent view that will contain the new ViewHolder.
     * @param viewType The type of the view to create (unused in this case).
     * @return A new ViewHolder that holds the item view for the group member.
     */
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_list, parent, false);
        return new UserViewHolder(itemView);
    }

    /**
     * Binds data to the view for the item at the specified position.
     *
     * @param holder The ViewHolder to update.
     * @param position The position of the item in the data list.
     */
    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userList.get(position);

        // Set the username to the TextView
        holder.nameTextView.setText(user.getUsername());

        // Show or hide the remove button based on whether the user is the group leader
        if (isLeader) {
            holder.removeButton.setVisibility(View.VISIBLE);  // Show the remove button if the user is the leader
        } else {
            holder.removeButton.setVisibility(View.GONE);     // Hide the remove button otherwise
        }

        // Set click listener for remove button
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClick(user);  // Call the listener when remove button is clicked
            }
        });

        // Set click listener for message button
        holder.msgBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(user);  // Call the listener when message button is clicked
            }
        });
    }

    /**
     * Returns the total number of items in the user list.
     *
     * @return The number of items in the user list.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class that represents a single item (group member) in the RecyclerView.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;    // TextView to display the username
        ImageButton removeButton; // Button to remove the user from the group (visible if the user is the leader)
        ImageButton msgBtn;      // Button to send a message to the user

        /**
         * Constructor for initializing the ViewHolder with the views from the item layout.
         *
         * @param itemView The view of the individual item in the RecyclerView.
         */
        public UserViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.memberName);
            removeButton = itemView.findViewById(R.id.member_removeButton);
            msgBtn = itemView.findViewById(R.id.member_messageButton);
        }
    }
}
