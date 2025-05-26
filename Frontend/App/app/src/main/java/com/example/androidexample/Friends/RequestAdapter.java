package com.example.androidexample.Friends;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * Adapter for displaying a list of friend requests in a {@link RecyclerView}.
 * <p>
 * This adapter binds {@link FriendRequest} data to views defined in the
 * {@code item_friend_request} layout. It also provides functionality for handling
 * accept and decline actions on each request.
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<FriendRequest> friendRequestList;
    private OnRequestActionListener listener;

    /**
     * Constructs a new {@code RequestAdapter}.
     *
     * @param context     The context used to inflate layouts.
     * @param friendRequestList The list of {@link FriendRequest} objects to display.
     * @param listener    Callback interface for handling accept/decline actions.
     */
    public RequestAdapter(Context context, List<FriendRequest> friendRequestList, OnRequestActionListener listener) {
        this.context = context;
        this.friendRequestList = friendRequestList;
        this.listener = listener;
    }

    /**
     * Creates a new {@link RequestViewHolder} when there are no existing view holders
     * that can be reused.
     *
     * @param parent   The parent view group.
     * @param viewType The view type of the new view.
     * @return A new instance of {@code RequestViewHolder}.
     */
    @Override
    public RequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new RequestViewHolder(view);
    }

    /**
     * Binds the data for a specific position in the {@code requestList} to a {@link RequestViewHolder}.
     *
     * @param holder   The holder to bind data to.
     * @param position The position of the data item in the list.
     */
    @Override
    public void onBindViewHolder(RequestViewHolder holder, int position) {
        FriendRequest friendRequest = friendRequestList.get(position);
        holder.fromUserTextView.setText("From: " + friendRequest.getSender());

        // Set up accept and decline button listeners
        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClick(friendRequest));
        holder.declineButton.setOnClickListener(v -> listener.onDeclineClick(friendRequest));
    }

    /**
     * Returns the number of items in the {@code requestList}.
     *
     * @return The total number of requests.
     */
    @Override
    public int getItemCount() {
        return friendRequestList.size();
    }

    /**
     * ViewHolder class for holding views related to a single friend request item.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView fromUserTextView;
        ImageButton acceptButton, declineButton;

        /**
         * Initializes view references for the ViewHolder.
         *
         * @param itemView The view representing the RecyclerView item.
         */
        public RequestViewHolder(View itemView) {
            super(itemView);
            fromUserTextView = itemView.findViewById(R.id.fromUserTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }
    }

    /**
     * Interface definition for callbacks to be invoked when a friend request
     * is accepted or declined.
     */
    public interface OnRequestActionListener {
        /**
         * Called when the accept button is clicked.
         *
         * @param friendRequest The request being accepted.
         */
        void onAcceptClick(FriendRequest friendRequest);

        /**
         * Called when the decline button is clicked.
         *
         * @param friendRequest The request being declined.
         */
        void onDeclineClick(FriendRequest friendRequest);
    }
}
