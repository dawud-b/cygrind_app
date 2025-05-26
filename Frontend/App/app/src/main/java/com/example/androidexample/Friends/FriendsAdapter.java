package com.example.androidexample.Friends;

import android.content.Context;
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
 * RecyclerView Adapter for displaying a list of friends in a scrollable view.
 * <p>
 * Each item displays the friend's username along with action buttons for messaging
 * and removing the friend.
 * <p>
 * Utilizes a custom listener interface {@link OnFriendActionListener}
 * to handle user interaction callbacks from the adapter to the host activity or fragment.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private Context context;
    private List<User> friendList;
    private OnFriendActionListener onFriendActionListener;

    /**
     * Constructs a FriendsAdapter.
     *
     * @param context               The context from the parent activity or fragment.
     * @param friendList            List of friends to display.
     * @param listener              Listener to handle friend-related actions (e.g., message, remove).
     */
    public FriendsAdapter(Context context, List<User> friendList, OnFriendActionListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.onFriendActionListener = listener;
    }

    /**
     * Creates a new ViewHolder to display a friend item.
     *
     * @param parent   The parent view that the ViewHolder is attached to.
     * @param viewType The view type of the new View.
     * @return A new FriendViewHolder containing the layout for a single friend item.
     */
    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder at the specified position.
     *
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the friend item in the list.
     */
    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.friendNameTextView.setText(friend.getUsername());

        holder.messageButton.setOnClickListener(v -> {
            if (onFriendActionListener != null) {
                onFriendActionListener.onMessageClicked(friend);
            }
        });

        holder.removeFriend.setOnClickListener(v -> {
            if (onFriendActionListener != null) {
                onFriendActionListener.onRemoveFriendClicked(friend);
            }
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The number of friend items.
     */
    @Override
    public int getItemCount() {
        return friendList.size();
    }

    /**
     * ViewHolder that describes a single friend item view and metadata about its place within the RecyclerView.
     */
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendNameTextView;
        ImageButton messageButton;
        ImageButton removeFriend;

        /**
         * Constructor that initializes the UI elements for the friend item.
         *
         * @param itemView The view of the friend item.
         */
        public FriendViewHolder(View itemView) {
            super(itemView);
            friendNameTextView = itemView.findViewById(R.id.friendName);
            messageButton = itemView.findViewById(R.id.member_messageButton);
            removeFriend = itemView.findViewById(R.id.removeButton);
        }
    }

    /**
     * Interface for handling click actions on friend items.
     * Implemented by the parent activity or fragment to define behavior.
     */
    public interface OnFriendActionListener {
        /**
         * Triggered when the message button is clicked.
         *
         * @param friend The selected friend to message.
         */
        void onMessageClicked(User friend);

        /**
         * Triggered when the remove friend button is clicked.
         *
         * @param friend The selected friend to remove.
         */
        void onRemoveFriendClicked(User friend);
    }
}
