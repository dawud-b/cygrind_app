package com.example.androidexample.WorkoutTemplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * {@code WorkoutTemplateAdapter} is a custom adapter for displaying a list of
 * {@link WorkoutTemplate} objects in a {@link RecyclerView}. It provides UI elements
 * for editing and deleting templates through interaction callbacks.
 *
 * This adapter uses the {@link WorkoutViewHolder} class to bind data to views.
 */
public class WorkoutTemplateAdapter extends RecyclerView.Adapter<WorkoutTemplateAdapter.WorkoutViewHolder> {

    private List<WorkoutTemplate> workoutList;
    private OnWorkoutActionListener listener;

    /**
     * Interface for handling edit and delete actions on workout templates.
     */
    public interface OnWorkoutActionListener {
        /**
         * Called when the edit button for a workout is clicked.
         *
         * @param position The index of the workout in the list.
         */
        void onEditClick(int position);

        /**
         * Called when the delete button for a workout is clicked.
         *
         * @param position The index of the workout in the list.
         */
        void onDeleteClick(int position);
    }

    /**
     * Constructs a {@code WorkoutTemplateAdapter} with the provided data and action listener.
     *
     * @param workoutList The list of workout templates to display.
     * @param listener    Listener to handle edit and delete actions.
     */
    public WorkoutTemplateAdapter(List<WorkoutTemplate> workoutList, OnWorkoutActionListener listener) {
        this.workoutList = workoutList;
        this.listener = listener;
    }

    /**
     * Inflates the view for a single workout template item and returns a {@link WorkoutViewHolder}.
     *
     * @param parent   The parent view group.
     * @param viewType The view type.
     * @return A new {@link WorkoutViewHolder}.
     */
    @Override
    public WorkoutViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workout_template_item, parent, false);
        return new WorkoutViewHolder(view);
    }

    /**
     * Binds data from a {@link WorkoutTemplate} to the view holder.
     *
     * @param holder   The view holder to bind data to.
     * @param position The position of the data item in the list.
     */
    @Override
    public void onBindViewHolder(WorkoutViewHolder holder, int position) {
        WorkoutTemplate workout = workoutList.get(position);
        holder.title.setText(workout.getTitle());

        holder.editButton.setOnClickListener(v -> listener.onEditClick(position));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(position));
    }

    /**
     * Returns the number of items in the dataset.
     *
     * @return The size of the workout list.
     */
    @Override
    public int getItemCount() {
        return workoutList.size();
    }

    /**
     * {@code WorkoutViewHolder} holds references to the views for each workout template item.
     */
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        Button editButton;
        Button deleteButton;

        /**
         * Initializes the view holder with the provided item view.
         *
         * @param itemView The view representing a single item.
         */
        public WorkoutViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.workoutTitle);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
