package com.example.androidexample.ExerciseSelection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.Exercise;
import com.example.androidexample.R;

import org.w3c.dom.Text;

import java.util.List;


/**
 * A RecyclerView Adapter that binds a list of Exercise objects to a RecyclerView.
 * It inflates the exercise item layout and handles displaying exercise data and user interactions.
 * The adapter supports adding exercises by implementing the `OnExerciseListener`.
 */
public class ExerciseListAdapter extends RecyclerView.Adapter<ExerciseListAdapter.ExerciseViewHolder> {

    // List of exercises to display in the RecyclerView
    private List<Exercise> exerciseList;

    // Listener for handling click events on the add button
    private OnExerciseListener listener;

    /**
     * Interface for handling click events when an exercise is added.
     */
    public interface OnExerciseListener {
        /**
         * Callback method invoked when the add button for an exercise is clicked.
         *
         * @param position The position of the exercise in the list.
         */
        void onAddClick(int position);
    }

    /**
     * Constructor to initialize the adapter with the list of exercises and the listener.
     *
     * @param exerciseList The list of exercises to display in the RecyclerView.
     * @param listener The listener for handling add button click events.
     */
    public ExerciseListAdapter(List<Exercise> exerciseList, OnExerciseListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder instance for the item layout.
     * This method is called when the RecyclerView needs a new ViewHolder.
     *
     * @param parent The parent ViewGroup where the item layout will be attached.
     * @param viewType The type of the view to create.
     * @return A new ExerciseViewHolder instance.
     */
    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_list, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder. This method is called to display data for a specific item.
     *
     * @param holder The ExerciseViewHolder that will display the data.
     * @param position The position of the exercise in the list.
     */
    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        Exercise exercise = exerciseList.get(position);
        holder.exerciseName.setText(exercise.getName());
        holder.difficultyText.setText(exercise.getDifficultly());
        holder.addButton.setOnClickListener(v -> listener.onAddClick(position));
    }

    /**
     * Returns the total number of items in the data set.
     *
     * @return The number of exercises in the list.
     */
    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    /**
     * ViewHolder class for holding references to the views used to display each exercise item.
     * It is used to efficiently bind data and avoid unnecessary view inflation.
     */
    public class ExerciseViewHolder extends RecyclerView.ViewHolder {

        // UI elements for displaying exercise data
        TextView exerciseName;
        Button addButton;
        TextView difficultyText;

        /**
         * Constructor to initialize the views in the item layout.
         *
         * @param itemView The root view of the item layout.
         */
        public ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            addButton = itemView.findViewById(R.id.addButton);
            difficultyText = itemView.findViewById(R.id.exerciseDifficulty);
        }
    }
}
