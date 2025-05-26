package com.example.androidexample.Challenges;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * Adapter for displaying challenge sets in a RecyclerView
 */
public class ChallengeSetAdapter extends RecyclerView.Adapter<ChallengeSetAdapter.ChallengeSetViewHolder> {

    public interface OnChallengeCompleteListener {
        void onChallengeComplete(long challengeSetId, int position);
    }

    private Context context;
    private List<ChallengeSetModel> challengeSets;
    private OnChallengeCompleteListener completeListener;

    public ChallengeSetAdapter(Context context, List<ChallengeSetModel> challengeSets,
                               OnChallengeCompleteListener completeListener) {
        this.context = context;
        this.challengeSets = challengeSets;
        this.completeListener = completeListener;
    }

    @NonNull
    @Override
    public ChallengeSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge_set, parent, false);
        return new ChallengeSetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeSetViewHolder holder, int position) {
        ChallengeSetModel challengeSet = challengeSets.get(position);

        holder.titleText.setText(challengeSet.getTitle());
        holder.progressText.setText(String.format("Progress: %d/%d",
                challengeSet.getChallengesCompleted(), challengeSet.getStages()));

        // Set progress bar
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress((int)(challengeSet.getProgress() * 100));

        // Set up challenges adapter for nested RecyclerView
        ChallengeAdapter challengeAdapter = new ChallengeAdapter(context, challengeSet.getChallenges());
        holder.challengesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.challengesRecyclerView.setAdapter(challengeAdapter);

        // Set up click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChallengeDetailActivity.class);
            intent.putExtra("CHALLENGE_SET_ID", challengeSet.getId());
            context.startActivity(intent);
        });

        // Set up complete button
        ChallengeModel nextChallenge = challengeSet.getNextChallenge();
        if (nextChallenge != null && !nextChallenge.isCompleted()) {
            holder.completeButton.setEnabled(true);
            holder.completeButton.setText("Complete: " + nextChallenge.getTitle());
        } else {
            holder.completeButton.setEnabled(false);
            holder.completeButton.setText("All challenges completed!");
        }

        // Set complete button click listener
        holder.completeButton.setOnClickListener(v -> {
            if (nextChallenge != null && !nextChallenge.isCompleted()) {
                completeListener.onChallengeComplete(challengeSet.getId(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return challengeSets.size();
    }

    static class ChallengeSetViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView progressText;
        ProgressBar progressBar;
        RecyclerView challengesRecyclerView;
        Button completeButton;

        public ChallengeSetViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.challenge_set_title);
            progressText = itemView.findViewById(R.id.challenge_set_progress_text);
            progressBar = itemView.findViewById(R.id.challenge_set_progress_bar);
            challengesRecyclerView = itemView.findViewById(R.id.challenges_recycler_view);
            completeButton = itemView.findViewById(R.id.complete_challenge_button);
        }
    }
}