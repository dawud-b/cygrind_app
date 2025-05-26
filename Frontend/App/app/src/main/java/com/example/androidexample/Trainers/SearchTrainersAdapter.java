package com.example.androidexample.Trainers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.ArrayList;
import java.util.List;
/**
 * A RecyclerView Adapter that binds a list of trainers to the views in a RecyclerView.
 * It supports filtering the list of trainers based on the search query and allows interactions
 * with each trainer's follow button and view profile button.
 * <p>
 * This adapter handles displaying trainer information including name, specialization, and bio.
 * It also provides functionality to follow a trainer and view their profile.
 */
public class SearchTrainersAdapter extends RecyclerView.Adapter<SearchTrainersAdapter.TrainerViewHolder> {

    private Context context;                    // Context for inflating views and handling resources
    private List<Trainer> trainerList;          // Original list of trainers
    private List<Trainer> trainerListFiltered;  // Filtered list of trainers based on search query
    private OnTrainerActionListener listener;   // Listener for handling button clicks

    /**
     * Constructor to initialize the adapter with a list of trainers and a listener.
     *
     * @param context The context used for inflating the views.
     * @param trainerList The list of trainers to be displayed.
     * @param listener The listener to handle follow and view profile actions.
     */
    public SearchTrainersAdapter(Context context, List<Trainer> trainerList, OnTrainerActionListener listener) {
        this.context = context;
        this.trainerList = trainerList;
        this.trainerListFiltered = new ArrayList<>(trainerList);
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder instance for a trainer item.
     *
     * @param parent The parent view group where the item will be added.
     * @param viewType The view type of the item.
     * @return The newly created ViewHolder.
     */
    @Override
    public TrainerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each trainer item in the RecyclerView
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_search_trainer, parent, false);
        return new TrainerViewHolder(itemView);
    }

    /**
     * Binds the trainer data to the views for the item at the specified position.
     *
     * @param holder The ViewHolder that holds the views.
     * @param position The position of the item in the filtered list.
     */
    @Override
    public void onBindViewHolder(TrainerViewHolder holder, int position) {
        // Get the trainer at the current position in the filtered list
        Trainer trainer = trainerListFiltered.get(position);

        // Set the trainer's name and specialization
        holder.trainerNameTextView.setText(trainer.getDisplayName());
        holder.trainerSpecializationTextView.setText(trainer.getSpecialization());

        // Set the bio if available
        if (trainer.getBio() != null && !trainer.getBio().isEmpty()) {
            holder.trainerBioTextView.setVisibility(View.VISIBLE);
            holder.trainerBioTextView.setText(trainer.getBio());
        } else {
            holder.trainerBioTextView.setVisibility(View.GONE);
        }

        // Set the follow button listener
        holder.followButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFollowClick(trainer);
            }
        });

        // Set the view profile button listener
        holder.viewProfileButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onViewProfileClick(trainer);
            }
        });
    }

    /**
     * Returns the number of items in the filtered list.
     *
     * @return The size of the filtered trainer list.
     */
    @Override
    public int getItemCount() {
        return trainerListFiltered.size();
    }

    /**
     * ViewHolder class to hold the views for each trainer item.
     */
    public static class TrainerViewHolder extends RecyclerView.ViewHolder {

        TextView trainerNameTextView;           // TextView for displaying the trainer's name
        TextView trainerSpecializationTextView; // TextView for displaying the trainer's specialization
        TextView trainerBioTextView;            // TextView for displaying the trainer's bio
        ImageButton followButton;               // Button to follow the trainer
        ImageButton viewProfileButton;          // Button to view the trainer's profile

        /**
         * Initializes the views for each individual trainer item.
         *
         * @param itemView The view for the individual item.
         */
        public TrainerViewHolder(View itemView) {
            super(itemView);

            trainerNameTextView = itemView.findViewById(R.id.trainerName);
            trainerSpecializationTextView = itemView.findViewById(R.id.trainerSpecialization);
            trainerBioTextView = itemView.findViewById(R.id.trainerBio);
            followButton = itemView.findViewById(R.id.followButton);
            viewProfileButton = itemView.findViewById(R.id.viewProfileButton);
        }
    }

    /**
     * Filters the trainer list based on the provided search query.
     * It updates the filtered list and notifies the adapter of the change.
     *
     * @param query The search query to filter the trainers by name or specialization.
     */
    public void filterList(String query) {
        trainerListFiltered.clear();
        if (query.isEmpty()) {
            trainerListFiltered.addAll(trainerList); // If the query is empty, show all trainers
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Trainer trainer : trainerList) {
                // Filter by name or specialization
                if (trainer.getDisplayName().toLowerCase().contains(lowerCaseQuery) ||
                        trainer.getSpecialization().toLowerCase().contains(lowerCaseQuery)) {
                    trainerListFiltered.add(trainer);
                }
            }
        }
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    /**
     * Interface to handle button clicks for interacting with trainers.
     */
    public interface OnTrainerActionListener {
        /**
         * Called when the follow button is clicked for a trainer.
         *
         * @param trainer The trainer that was followed.
         */
        void onFollowClick(Trainer trainer);

        /**
         * Called when the view profile button is clicked for a trainer.
         *
         * @param trainer The trainer whose profile is to be viewed.
         */
        void onViewProfileClick(Trainer trainer);
    }
}
