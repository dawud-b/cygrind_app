package com.example.androidexample.Challenges;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;

/**
 * Adapter for displaying individual challenges in a RecyclerView
 */
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private Context context;
    private List<ChallengeModel> challenges;

    public ChallengeAdapter(Context context, List<ChallengeModel> challenges) {
        this.context = context;
        this.challenges = challenges;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        ChallengeModel challenge = challenges.get(position);

        holder.titleText.setText(challenge.getTitle());
        holder.descriptionText.setText(challenge.getDescription());
        holder.pointsText.setText("+" + challenge.getPoints() + " points");

        // Set status icon based on completion status
        if (challenge.isCompleted()) {
            holder.statusIcon.setImageResource(R.drawable.ic_check_circle);
            holder.statusText.setText("Completed");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.green));
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_pending);
            holder.statusText.setText("Pending");
            holder.statusText.setTextColor(context.getResources().getColor(R.color.yellow));
        }
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView descriptionText;
        TextView pointsText;
        TextView statusText;
        ImageView statusIcon;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.challenge_title);
            descriptionText = itemView.findViewById(R.id.challenge_description);
            pointsText = itemView.findViewById(R.id.challenge_points);
            statusText = itemView.findViewById(R.id.challenge_status);
            statusIcon = itemView.findViewById(R.id.challenge_status_icon);
        }
    }
}