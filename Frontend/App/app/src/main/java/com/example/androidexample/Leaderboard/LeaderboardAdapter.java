package com.example.androidexample.Leaderboard;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;
/**
 * Adapter for displaying leaderboard entries in a RecyclerView.
 * This adapter binds leaderboard data to a view, displaying the user's rank, name, score, and progress
 * in the leaderboard. It dynamically calculates progress bar percentages based on the highest score.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<LeaderboardEntry> leaderboardEntries;  // List of leaderboard entries
    private Context context;  // Context for inflating layouts
    private int maxScore;  // Maximum score in the leaderboard to calculate progress bar percentages

    /**
     * Constructor to initialize the leaderboard adapter with context and leaderboard entries.
     * It calculates the maximum score for progress bar percentage calculations.
     *
     * @param context The context used to inflate the leaderboard item views.
     * @param leaderboardEntries The list of leaderboard entries to display.
     */
    public LeaderboardAdapter(Context context, List<LeaderboardEntry> leaderboardEntries) {
        this.context = context;
        this.leaderboardEntries = leaderboardEntries;

        // Calculate max score for progress bars
        maxScore = 0;
        for (LeaderboardEntry entry : leaderboardEntries) {
            if (entry.getScore() > maxScore) {
                maxScore = entry.getScore();
            }
        }

        // Ensure maxScore is not zero to avoid division by zero errors
        if (maxScore == 0) maxScore = 1;
    }

    /**
     * Creates a new ViewHolder instance to hold the views for each leaderboard entry.
     *
     * @param parent The parent view group to which the item view will be attached.
     * @param viewType The view type of the new view.
     * @return A new ViewHolder for the leaderboard item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the leaderboard entry data to the given ViewHolder.
     * It sets the rank, user name, score, progress bar, and adjusts the background color for the rank.
     *
     * @param holder The ViewHolder to bind data to.
     * @param position The position in the data set for the item.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardEntries.get(position);

        // Set rank (position + 1)
        holder.userRank.setText(String.valueOf(position + 1));

        // Set background color for rank based on position
        if (position == 0) {
            holder.userRank.setBackgroundColor(Color.parseColor("#FFD700")); // Gold
            holder.userRank.setTextColor(Color.BLACK);
        } else if (position == 1) {
            holder.userRank.setBackgroundColor(Color.parseColor("#C0C0C0")); // Silver
            holder.userRank.setTextColor(Color.BLACK);
        } else if (position == 2) {
            holder.userRank.setBackgroundColor(Color.parseColor("#CD7F32")); // Bronze
            holder.userRank.setTextColor(Color.WHITE);
        } else {
            holder.userRank.setBackgroundColor(Color.parseColor("#E53935")); // Red
            holder.userRank.setTextColor(Color.WHITE);
        }

        // Set user name using display name method from LeaderboardEntry
        holder.userName.setText(entry.getDisplayName());

        // Set score text
        holder.userScore.setText(entry.getScore() + " pts");

        // Calculate and set progress bar value
        int progress = (int)((entry.getScore() * 100.0f) / maxScore);
        holder.scoreProgress.setProgress(progress);
    }

    /**
     * Returns the total number of items in the leaderboard.
     *
     * @return The number of leaderboard entries.
     */
    @Override
    public int getItemCount() {
        return leaderboardEntries.size();
    }

    /**
     * Updates the leaderboard data with new entries and recalculates the maximum score.
     *
     * @param newEntries The new list of leaderboard entries to be displayed.
     */
    public void updateData(List<LeaderboardEntry> newEntries) {
        this.leaderboardEntries = newEntries;

        // Recalculate max score for progress bars
        maxScore = 0;
        for (LeaderboardEntry entry : leaderboardEntries) {
            if (entry.getScore() > maxScore) {
                maxScore = entry.getScore();
            }
        }

        if (maxScore == 0) maxScore = 1;  // Avoid division by zero

        notifyDataSetChanged();  // Notify the adapter that data has changed
    }

    /**
     * ViewHolder class to represent individual leaderboard items.
     * Holds references to the views in each leaderboard item layout.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userRank;  // Text view to display the user's rank
        TextView userName;  // Text view to display the user's name
        TextView userScore; // Text view to display the user's score
        ProgressBar scoreProgress;  // Progress bar to display the user's score progress

        /**
         * Constructor to initialize the view holder and find the views in the layout.
         *
         * @param itemView The item view representing a single leaderboard entry.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userRank = itemView.findViewById(R.id.user_rank);
            userName = itemView.findViewById(R.id.user_name);
            userScore = itemView.findViewById(R.id.user_score);
            scoreProgress = itemView.findViewById(R.id.score_progress);
        }
    }
}
