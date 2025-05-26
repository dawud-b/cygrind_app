package com.example.androidexample.Trainers;

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
 * RecyclerView Adapter for displaying a list of followed trainers.
 * This adapter binds the trainer data to the view elements in the RecyclerView.
 * It also handles the interactions for the "view profile" and "unfollow" actions.
 * <p>
 * The adapter uses a custom ViewHolder to hold references to the views and implements click listeners
 * for actions like viewing the profile or unfollowing a trainer. The interactions are passed to the
 * provided `OnTrainerActionListener` interface.
 */
public class FollowedTrainersAdapter extends RecyclerView.Adapter<FollowedTrainersAdapter.TrainerViewHolder> {

    private Context context;                          // Context for accessing system resources
    private List<Trainer> trainerList;                // List of trainers to be displayed
    private OnTrainerActionListener onTrainerActionListener; // Listener for trainer-related actions

    /**
     * Constructor for initializing the adapter with the context, trainer list, and action listener.
     *
     * @param context The context to access system resources.
     * @param trainerList The list of trainers to be displayed in the RecyclerView.
     * @param listener The listener to handle actions like viewing the profile and unfollowing a trainer.
     */
    public FollowedTrainersAdapter(Context context, List<Trainer> trainerList, OnTrainerActionListener listener) {
        this.context = context;
        this.trainerList = trainerList;
        this.onTrainerActionListener = listener;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder to represent an item.
     * Inflates the layout for each item in the RecyclerView.
     *
     * @param parent The parent view that the new item view will be attached to.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder instance for the item.
     */
    @Override
    public TrainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(context).inflate(R.layout.item_trainer, parent, false);
        return new TrainerViewHolder(view);
    }

    /**
     * Called when RecyclerView binds the data to a ViewHolder.
     * Binds the trainer data to the corresponding views in the ViewHolder.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position of the item within the list.
     */
    @Override
    public void onBindViewHolder(TrainerViewHolder holder, int position) {
        Trainer trainer = trainerList.get(position);

        // Bind trainer data to TextViews
        holder.trainerNameTextView.setText(trainer.getDisplayName());
        holder.trainerSpecializationTextView.setText(trainer.getSpecialization());

        // Set up the "view profile" button click listener
        holder.viewProfileButton.setOnClickListener(v -> {
            if (onTrainerActionListener != null) {
                onTrainerActionListener.onViewProfileClicked(trainer);
            }
        });

        // Set up the "unfollow" button click listener
        holder.unfollowButton.setOnClickListener(v -> {
            if (onTrainerActionListener != null) {
                onTrainerActionListener.onUnfollowClicked(trainer);
            }
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The number of trainers in the list.
     */
    @Override
    public int getItemCount() {
        return trainerList.size();
    }

    /**
     * ViewHolder class for holding views for each item in the RecyclerView.
     * This class holds references to the TextViews and ImageButtons used in the item layout.
     */
    public static class TrainerViewHolder extends RecyclerView.ViewHolder {
        TextView trainerNameTextView;                  // TextView for displaying trainer's name
        TextView trainerSpecializationTextView;        // TextView for displaying trainer's specialization
        ImageButton viewProfileButton;                 // Button for viewing the trainer's profile
        ImageButton unfollowButton;                    // Button for unfollowing the trainer

        /**
         * Constructor to initialize the ViewHolder with views from the item layout.
         *
         * @param itemView The view representing a single item in the RecyclerView.
         */
        public TrainerViewHolder(View itemView) {
            super(itemView);
            trainerNameTextView = itemView.findViewById(R.id.trainerName);
            trainerSpecializationTextView = itemView.findViewById(R.id.trainerSpecialization);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
            unfollowButton = itemView.findViewById(R.id.unfollowButton);
        }
    }

    /**
     * Interface for handling trainer actions such as viewing a profile and unfollowing a trainer.
     */
    public interface OnTrainerActionListener {
        /**
         * Called when the "view profile" button is clicked for a trainer.
         *
         * @param trainer The trainer whose profile is to be viewed.
         */
        void onViewProfileClicked(Trainer trainer);

        /**
         * Called when the "unfollow" button is clicked for a trainer.
         *
         * @param trainer The trainer to be unfollowed.
         */
        void onUnfollowClicked(Trainer trainer);
    }
}
