package com.example.androidexample.Groups;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * Adapter for displaying a list of workout groups in a RecyclerView.
 * This adapter binds data from a list of `WorkoutGroup` objects to the UI elements
 * in a `RecyclerView` item view and provides functionality to handle click events
 * on the "Join Request" button.
 */
public class WorkoutGroupAdapter extends RecyclerView.Adapter<WorkoutGroupAdapter.WorkoutGroupViewHolder> {

    private List<WorkoutGroup> workoutGroups;  // List of workout groups to display
    private OnJoinRequestClickListener listener;  // Listener for "Join Request" button click

    /**
     * Constructor for the adapter.
     *
     * @param workoutGroups A list of `WorkoutGroup` objects to be displayed in the RecyclerView.
     * @param listener A listener that handles the "Join Request" button click events.
     */
    public WorkoutGroupAdapter(List<WorkoutGroup> workoutGroups, OnJoinRequestClickListener listener) {
        this.workoutGroups = workoutGroups;
        this.listener = listener;
    }

    /**
     * Creates a new view holder for the workout group item view.
     * This method is called when the RecyclerView needs a new view holder to display an item.
     *
     * @param parent The parent view group into which the item view will be added.
     * @param viewType The view type of the new view.
     * @return A new instance of `WorkoutGroupViewHolder`.
     */
    @Override
    public WorkoutGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for a single workout group item view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_group, parent, false);
        return new WorkoutGroupViewHolder(view);  // Return the new view holder
    }

    /**
     * Binds data to the view holder.
     * This method is called to display the data for a specific position in the RecyclerView.
     *
     * @param holder The view holder that will hold the data for the item.
     * @param position The position of the item in the list of workout groups.
     */
    @Override
    public void onBindViewHolder(WorkoutGroupViewHolder holder, int position) {
        // Get the workout group at the specified position
        WorkoutGroup group = workoutGroups.get(position);

        // Set the text for group name, description, and leader
        holder.groupName.setText(group.getGroupName());
        holder.description.setText(group.getDescription());
        holder.leader.setText(group.getLeader().getUsername());

        // Set the click listener for the "Join Request" button
        holder.joinRequestBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJoinRequestClick(group);  // Notify the listener about the click
            }
        });
    }

    /**
     * Returns the total number of items in the workout groups list.
     *
     * @return The size of the workout groups list.
     */
    @Override
    public int getItemCount() {
        return workoutGroups.size();
    }

    /**
     * ViewHolder for a single workout group item in the RecyclerView.
     * This holds the views that represent a single workout group's details.
     */
    public static class WorkoutGroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;  // Text view to display the group name
        TextView description;  // Text view to display the group description
        TextView leader;  // Text view to display the leader's name
        Button joinRequestBtn;  // Button to trigger the join request action

        /**
         * Constructor for the view holder.
         *
         * @param itemView The item view that represents a single workout group.
         */
        public WorkoutGroupViewHolder(View itemView) {
            super(itemView);
            leader = itemView.findViewById(R.id.leader_txt);  // Initialize leader TextView
            groupName = itemView.findViewById(R.id.textViewGroupName);  // Initialize group name TextView
            description = itemView.findViewById(R.id.textViewDescription);  // Initialize description TextView
            joinRequestBtn = itemView.findViewById(R.id.btnJoinRequest);  // Initialize join request Button
        }
    }

    /**
     * Interface to handle the click event on the "Join Request" button.
     * This interface is implemented by the parent activity or fragment to respond to
     * the user's interaction with the button.
     */
    public interface OnJoinRequestClickListener {
        /**
         * Called when the user clicks the "Join Request" button for a workout group.
         *
         * @param group The workout group for which the join request button was clicked.
         */
        void onJoinRequestClick(WorkoutGroup group);
    }
}
