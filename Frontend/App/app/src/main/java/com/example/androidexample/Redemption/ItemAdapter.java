package com.example.androidexample.Redemption;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.List;
import java.util.Set;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public interface OnRedeemClickListener {
        void onRedeemClick(RedeemItem item);
    }

    private List<RedeemItem> items;
    private Set<String> unlockedReactions; // reaction IDs already redeemed
    private OnRedeemClickListener listener;

    public ItemAdapter(List<RedeemItem> items, Set<String> unlockedReactions, OnRedeemClickListener listener) {
        this.items = items;
        this.unlockedReactions = unlockedReactions;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView costText;
        Button redeemBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.itemName);
            costText = itemView.findViewById(R.id.itemCost);
            redeemBtn = itemView.findViewById(R.id.redeemButton);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_redeem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RedeemItem item = items.get(position);
        String itemId = "" + item.getReactionId(); // Assuming RedeemItem has getId()

        holder.nameText.setText(item.getDisplayName());
        holder.costText.setText("Cost: " + item.getCost() + " pts");

        if (unlockedReactions.contains(itemId)) {
            holder.redeemBtn.setEnabled(false);
            holder.redeemBtn.setText("Redeemed");
        } else {
            holder.redeemBtn.setEnabled(true);
            holder.redeemBtn.setText("Redeem");
            holder.redeemBtn.setOnClickListener(v -> listener.onRedeemClick(item));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Optional: Call this to update the list of unlocked reactions dynamically
    public void setUnlockedReactions(Set<String> unlocked) {
        this.unlockedReactions = unlocked;
        notifyDataSetChanged();
    }
}
