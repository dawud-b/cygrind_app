package com.example.androidexample.Groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.example.androidexample.User;

import java.util.List;

/**
 * Adapter for displaying a list of group join requests in a RecyclerView.
 * It binds the request data to each item in the list, and handles user interactions such as
 * accepting or declining a request.
 */
public class GroupRequestAdapter extends RecyclerView.Adapter<GroupRequestAdapter.ViewHolder> {

    private List<JoinRequest> requestList;  // List of join requests to be displayed
    private OnRequestClickListener listener;  // Listener to handle accept/decline actions

    /**
     * Constructor for the adapter.
     *
     * @param requestList List of join requests to be displayed
     * @param listener The listener to handle click events for accept/decline buttons
     */
    public GroupRequestAdapter(List<JoinRequest> requestList, OnRequestClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder when there are no existing ViewHolders that can be reused.
     *
     * @param parent The parent ViewGroup into which the new view will be added
     * @param viewType The type of view to create (used for different view types)
     * @return A new ViewHolder instance with the inflated item view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each request item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_request, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the data to the views of a ViewHolder.
     *
     * @param holder The ViewHolder to bind data to
     * @param position The position in the list of requests
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JoinRequest request = requestList.get(position);

        // Bind data to views
        holder.requesterNameTextView.setText(request.getUser().getUsername());
        holder.statusTextView.setText("Status: " + request.getStatus().toString());

        // Handle Accept button click
        holder.acceptButton.setOnClickListener(v -> listener.onAcceptClick(request));

        // Handle Decline button click
        holder.declineButton.setOnClickListener(v -> listener.onDeclineClick(request));
    }

    /**
     * Returns the number of items in the request list.
     *
     * @return The size of the request list
     */
    @Override
    public int getItemCount() {
        return requestList.size();
    }

    /**
     * ViewHolder for individual request items in the RecyclerView.
     * This holds references to the views that make up each item in the list.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView requesterNameTextView;  // TextView to display the name of the user requesting to join
        TextView statusTextView;         // TextView to display the current status of the request
        ImageButton acceptButton;        // Button to accept the join request
        ImageButton declineButton;       // Button to decline the join request

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view that represents each item in the list
         */
        public ViewHolder(View itemView) {
            super(itemView);
            requesterNameTextView = itemView.findViewById(R.id.requester_name);
            statusTextView = itemView.findViewById(R.id.status_text);
            acceptButton = itemView.findViewById(R.id.group_request_acceptButton);
            declineButton = itemView.findViewById(R.id.group_request_declineButton);
        }
    }

    /**
     * Interface for handling accept and decline button clicks for group join requests.
     *
     * This interface should be implemented by the activity or fragment that interacts with the adapter
     * to perform the necessary actions when a request is accepted or declined.
     */
    public interface OnRequestClickListener {
        /**
         * Called when the accept button is clicked for a join request.
         *
         * @param request The join request that is being accepted
         */
        void onAcceptClick(JoinRequest request);

        /**
         * Called when the decline button is clicked for a join request.
         *
         * @param request The join request that is being declined
         */
        void onDeclineClick(JoinRequest request);
    }
}
