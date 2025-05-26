package com.example.androidexample.Nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A RecyclerView Adapter that binds a list of food items to the views in a RecyclerView.
 * It supports filtering the list of foods based on the search query and allows
 * adding food items to the daily log.
 */
public class SearchFoodsAdapter extends RecyclerView.Adapter<SearchFoodsAdapter.FoodViewHolder> {

    private static final String TAG = "SearchFoodsAdapter";
    private Context context;                 // Context for inflating views and handling resources
    private List<FoodItem> foodList;         // Original list of food items
    private List<FoodItem> foodListFiltered; // Filtered list of food items based on search query
    private OnFoodActionListener listener;   // Listener for handling button clicks

    /**
     * Constructor to initialize the adapter with a list of food items and a listener.
     *
     * @param context The context used for inflating the views.
     * @param foodList The list of food items to be displayed.
     * @param listener The listener to handle add food action.
     */
    public SearchFoodsAdapter(Context context, List<FoodItem> foodList, OnFoodActionListener listener) {
        this.context = context;
        this.foodList = foodList != null ? foodList : new ArrayList<>();
        this.foodListFiltered = new ArrayList<>(this.foodList);
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder instance for a food item.
     *
     * @param parent The parent view group where the item will be added.
     * @param viewType The view type of the item.
     * @return The newly created ViewHolder.
     */
    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each food item in the RecyclerView
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(itemView);
    }

    /**
     * Binds the food item data to the views for the item at the specified position.
     *
     * @param holder The ViewHolder that holds the views.
     * @param position The position of the item in the filtered list.
     */
    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        // Get the food item at the current position in the filtered list
        FoodItem foodItem = foodListFiltered.get(position);

        // Set the food item's name
        holder.nameTextView.setText(foodItem.getName());

        // Set the calories
        holder.caloriesTextView.setText(String.format(Locale.getDefault(), "%.0f cal", foodItem.getCalories()));

        // Set the macros
        String macros = String.format(Locale.getDefault(),
                "P: %.1fg | C: %.1fg | F: %.1fg",
                foodItem.getProtein(),
                foodItem.getCarbohydrates(),
                foodItem.getFat());
        holder.macrosTextView.setText(macros);

        // Set the serving information
        String servingText = String.format(Locale.getDefault(),
                "Serving: %.0f %s",
                foodItem.getServingSize(),
                foodItem.getServingUnit());
        holder.servingTextView.setText(servingText);

        // Set whole item click listener to add this food to today's log
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddFoodClick(foodItem);
            }
        });
    }

    /**
     * Returns the number of items in the filtered list.
     *
     * @return The size of the filtered food list.
     */
    @Override
    public int getItemCount() {
        return foodListFiltered.size();
    }

    /**
     * Updates the food list with new data.
     *
     * @param newFoodList The new list of food items to display.
     */
    public void updateData(List<FoodItem> newFoodList) {
        this.foodList = newFoodList != null ? newFoodList : new ArrayList<>();
        filterList(""); // Reset filter to show all items
    }

    /**
     * ViewHolder class to hold the views for each food item.
     */
    public static class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;      // TextView for displaying the food's name
        TextView caloriesTextView;  // TextView for displaying the food's calories
        TextView macrosTextView;    // TextView for displaying the food's macros
        TextView servingTextView;   // TextView for displaying the food's serving info

        /**
         * Initializes the views for each individual food item.
         *
         * @param itemView The view for the individual item.
         */
        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTextView = itemView.findViewById(R.id.food_item_name);
            caloriesTextView = itemView.findViewById(R.id.food_item_calories);
            macrosTextView = itemView.findViewById(R.id.food_item_macros);
            servingTextView = itemView.findViewById(R.id.food_item_serving);
        }
    }

    /**
     * Filters the food list based on the provided search query.
     * It updates the filtered list and notifies the adapter of the change.
     *
     * @param query The search query to filter the foods by name.
     */
    public void filterList(String query) {
        foodListFiltered.clear();
        if (query.isEmpty()) {
            foodListFiltered.addAll(foodList); // If the query is empty, show all foods
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (FoodItem food : foodList) {
                // Filter by name
                if (food.getName().toLowerCase().contains(lowerCaseQuery)) {
                    foodListFiltered.add(food);
                }
            }
        }
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    /**
     * Interface to handle click for adding food items to daily log.
     */
    public interface OnFoodActionListener {
        /**
         * Called when a food item is clicked to add it to daily log.
         *
         * @param foodItem The food item to be added.
         */
        void onAddFoodClick(FoodItem foodItem);
    }
}