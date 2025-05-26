package com.example.androidexample.Nutrition;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying food items in a RecyclerView.
 */
public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

    private List<FoodItem> foodItems;
    private List<FoodItem> filteredFoodItems;
    private OnFoodItemClickListener listener;

    /**
     * Interface for handling food item click events
     */
    public interface OnFoodItemClickListener {
        void onFoodItemClick(FoodItem foodItem);
    }

    public FoodItemAdapter(List<FoodItem> foodItems, OnFoodItemClickListener listener) {
        this.foodItems = foodItems;
        this.filteredFoodItems = new ArrayList<>(foodItems);
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {
        FoodItem foodItem = filteredFoodItems.get(position);
        holder.bind(foodItem, listener);
    }

    @Override
    public int getItemCount() {
        return filteredFoodItems.size();
    }

    /**
     * Filter the list of food items based on the search query
     * @param query The search query
     */
    public void filter(String query) {
        // Make a final copy of the query to use in the loop
        final String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

        // Clear the filtered list
        filteredFoodItems.clear();

        // Add matching items
        if (lowerCaseQuery.isEmpty()) {
            filteredFoodItems.addAll(foodItems);
        } else {
            for (FoodItem item : foodItems) {
                if (item.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    filteredFoodItems.add(item);
                }
            }
        }

        notifyDataSetChanged();
    }

    /**
     * ViewHolder for food items
     */
    static class FoodItemViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView caloriesTextView;
        private TextView macrosTextView;
        private TextView servingTextView;

        public FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.food_item_name);
            caloriesTextView = itemView.findViewById(R.id.food_item_calories);
            macrosTextView = itemView.findViewById(R.id.food_item_macros);
            servingTextView = itemView.findViewById(R.id.food_item_serving);
        }

        public void bind(FoodItem foodItem, OnFoodItemClickListener listener) {
            nameTextView.setText(foodItem.getName());
            caloriesTextView.setText(String.format(Locale.getDefault(), "%.0f cal", foodItem.getCalories()));

            String macros = String.format(Locale.getDefault(),
                    "P: %.1fg | C: %.1fg | F: %.1fg",
                    foodItem.getProtein(),
                    foodItem.getCarbohydrates(),
                    foodItem.getFat());
            macrosTextView.setText(macros);

            String servingText = String.format(Locale.getDefault(),
                    "Serving: %.0f %s",
                    foodItem.getServingSize(),
                    foodItem.getServingUnit());
            servingTextView.setText(servingText);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodItemClick(foodItem);
                }
            });
        }
    }
}