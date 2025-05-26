package com.example.androidexample.WorkoutTemplate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.Exercise;
import com.example.androidexample.R;

import java.util.List;

/**
 * {@code WorkoutTemplateExerciseAdapter} is a custom {@link RecyclerView.Adapter} used to display
 * a list of {@link TemplateExercise} items inside a workout template.
 * <p>
 * Each item in the list includes the exercise name, reps, sets, weight, and buttons to edit or delete.
 */
public class WorkoutTemplateExerciseAdapter extends RecyclerView.Adapter<WorkoutTemplateExerciseAdapter.ExerciseViewHolder> {

    private List<TemplateExercise> exerciseList;
    private OnExerciseListener listener;

    /**
     * Interface to handle user actions on exercise items such as editing or deleting.
     */
    public interface OnExerciseListener {
        /**
         * Triggered when the delete button is clicked for an exercise item.
         *
         * @param position The position of the item in the list.
         */
        void onDeleteClick(int position);

        /**
         * Triggered when the edit button is clicked for an exercise item.
         *
         * @param position The position of the item in the list.
         */
        void onEditClick(int position);
    }

    /**
     * Constructs a new adapter with a list of exercises and a listener for item actions.
     *
     * @param exerciseList The list of exercises to be displayed.
     * @param listener     The listener for handling edit/delete actions.
     */
    public WorkoutTemplateExerciseAdapter(List<TemplateExercise> exerciseList, OnExerciseListener listener) {
        this.exerciseList = exerciseList;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single exercise item and returns a new {@link ExerciseViewHolder}.
     *
     * @param parent   The parent view that this view will be attached to.
     * @param viewType The view type of the new view.
     * @return A new view holder for an exercise item.
     */
    @Override
    public ExerciseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout_template_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    /**
     * Binds a {@link TemplateExercise} to the given view holder.
     *
     * @param holder   The holder to bind data to.
     * @param position The position in the dataset of the item to bind.
     */
    @Override
    public void onBindViewHolder(ExerciseViewHolder holder, int position) {
        TemplateExercise exercise = exerciseList.get(position);
        holder.exerciseName.setText(exercise.getExercise().getName());
        holder.repsText.setText("Reps: " + exercise.getRepCount());
        holder.setsText.setText("Sets: " + exercise.getSetCount());
        holder.weightText.setText("Weight: " + exercise.getWeight());

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(position));
        holder.editButton.setOnClickListener(v -> listener.onEditClick(position));
    }

    /**
     * Returns the number of exercise items in the dataset.
     *
     * @return The size of the exercise list.
     */
    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    /**
     * {@code ExerciseViewHolder} holds the views for each exercise item in the list.
     */
    public class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView exerciseName;
        Button deleteButton;
        Button editButton;
        TextView weightText;
        TextView setsText;
        TextView repsText;

        /**
         * Initializes the view holder with the provided view, and binds its views.
         *
         * @param itemView The view for a single exercise item.
         */
        public ExerciseViewHolder(View itemView) {
            super(itemView);
            exerciseName = itemView.findViewById(R.id.exerciseName);
            deleteButton = itemView.findViewById(R.id.removeExerciseButton);
            editButton = itemView.findViewById(R.id.editButton);
            repsText = itemView.findViewById(R.id.repsText);
            setsText = itemView.findViewById(R.id.setsText);
            weightText = itemView.findViewById(R.id.weightText);
        }
    }
}
