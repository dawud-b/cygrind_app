package com.example.androidexample.Nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidexample.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragment for displaying the user's daily food log and nutritional summary.
 * This version connects to the backend via the NutritionRepository.
 */
public class DailyTrackerFragment extends Fragment {

    private static final String TAG = "DailyTrackerFragment";
    private RecyclerView recyclerView;
    private FoodItemAdapter adapter;
    private TextView dateTextView;
    private TextView caloriesTextView;
    private TextView proteinTextView;
    private TextView carbsTextView;
    private TextView fatTextView;
    private TextView goalCaloriesTextView;
    private TextView goalProteinTextView;
    private TextView goalCarbsTextView;
    private TextView goalFatTextView;
    private LocalDate selectedDate = LocalDate.now();
    private NutritionRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_tracker, container, false);

        try {
            // Initialize repository
            repository = new NutritionRepository(requireContext());

            // Initialize views
            recyclerView = view.findViewById(R.id.daily_foods_recycler_view);
            dateTextView = view.findViewById(R.id.daily_date);
            caloriesTextView = view.findViewById(R.id.daily_calories);
            proteinTextView = view.findViewById(R.id.daily_protein);
            carbsTextView = view.findViewById(R.id.daily_carbs);
            fatTextView = view.findViewById(R.id.daily_fat);
            goalCaloriesTextView = view.findViewById(R.id.goal_calories);
            goalProteinTextView = view.findViewById(R.id.goal_protein);
            goalCarbsTextView = view.findViewById(R.id.goal_carbs);
            goalFatTextView = view.findViewById(R.id.goal_fat);

            // Check if all views were found
            if (recyclerView == null || dateTextView == null || caloriesTextView == null) {
                Log.e(TAG, "One or more required views not found in layout");
                Toast.makeText(getContext(), "Error initializing layout", Toast.LENGTH_SHORT).show();
                return view;
            }

            // Set up date selection
            dateTextView.setOnClickListener(v -> showDatePicker());

            // Format and display the current date
            updateDateDisplay();

            // Set up RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(),
                    DividerItemDecoration.VERTICAL));

            // Initialize adapter with empty list (will be populated later)
            adapter = new FoodItemAdapter(new ArrayList<>(), item -> {
                // In a full implementation, we might open a dialog to edit quantities
                Toast.makeText(getContext(), "Tap and hold to remove item", Toast.LENGTH_SHORT).show();
            });

            recyclerView.setAdapter(adapter);

            // Add swipe to delete functionality
            setupSwipeToDelete();

            // Load food items and nutritional summary for today
            loadDailyFoodItems();
            loadNutritionalSummary();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error initializing tracker", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Static method to add a food item to the daily log
     * This allows other fragments to add items
     */
    public static void addFoodItem(FoodItem foodItem) {
        // This method would typically add to local storage or call the repository
        // For now, we'll implement it in the repository in AddFoodFragment
        Log.d(TAG, "Static addFoodItem called - this is not needed anymore");
    }

    /**
     * Display date picker dialog
     */
    private void showDatePicker() {
        try {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                try {
                    // Convert to LocalDate (roughly - ignoring time zone complexities for now)
                    selectedDate = LocalDate.ofEpochDay(selection / (24 * 60 * 60 * 1000));
                    updateDateDisplay();

                    // Load data for the selected date
                    loadDailyFoodItems();
                    loadNutritionalSummary();
                } catch (Exception e) {
                    Log.e(TAG, "Error processing date selection", e);
                    Toast.makeText(getContext(), "Error selecting date", Toast.LENGTH_SHORT).show();
                }
            });

            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
        } catch (Exception e) {
            Log.e(TAG, "Error showing date picker", e);
            Toast.makeText(getContext(), "Error showing date picker", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the date display
     */
    private void updateDateDisplay() {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault());
            dateTextView.setText(selectedDate.format(formatter));
        } catch (Exception e) {
            Log.e(TAG, "Error updating date display", e);
        }
    }

    /**
     * Load food items for the selected date from the repository
     * This method is made public so it can be called from NutritionActivity
     */
    public void loadDailyFoodItems() {
        Log.d(TAG, "Loading daily food items for date: " + selectedDate);
        try {
            repository.getDailyFoodItems(selectedDate, new NutritionRepository.OnResponseListener<List<FoodItem>>() {
                @Override
                public void onSuccess(List<FoodItem> result) {
                    Log.d(TAG, "Loaded " + result.size() + " food items");
                    // Update adapter with the loaded food items
                    if (getContext() == null) {
                        Log.e(TAG, "Context is null in onSuccess callback");
                        return;
                    }

                    adapter = new FoodItemAdapter(result, item ->
                            Toast.makeText(getContext(), "Tap and hold to remove item", Toast.LENGTH_SHORT).show());

                    if (recyclerView != null) {
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "RecyclerView is null in onSuccess callback");
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error loading food items: " + message);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading food items: " + message, Toast.LENGTH_SHORT).show();
                    }

                    // Clear the adapter if there's an error
                    if (getContext() != null) {
                        adapter = new FoodItemAdapter(new ArrayList<>(), item -> {});
                        if (recyclerView != null) {
                            recyclerView.setAdapter(adapter);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error calling loadDailyFoodItems", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading food items", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Load nutritional summary for the selected date from the repository
     * This method is made public so it can be called from NutritionActivity
     */
    public void loadNutritionalSummary() {
        Log.d(TAG, "Loading nutritional summary for date: " + selectedDate);
        try {
            repository.getNutritionalSummary(selectedDate, new NutritionRepository.OnResponseListener<NutritionalSummary>() {
                @Override
                public void onSuccess(NutritionalSummary summary) {
                    Log.d(TAG, "Loaded nutritional summary");
                    if (getContext() == null) {
                        Log.e(TAG, "Context is null in onSuccess callback");
                        return;
                    }

                    // Update UI with the loaded summary
                    try {
                        if (caloriesTextView != null) {
                            caloriesTextView.setText(String.format(Locale.getDefault(), "%.0f", summary.getTotalCalories()));
                        }

                        if (proteinTextView != null) {
                            proteinTextView.setText(String.format(Locale.getDefault(), "%.1f g", summary.getTotalProtein()));
                        }

                        if (carbsTextView != null) {
                            carbsTextView.setText(String.format(Locale.getDefault(), "%.1f g", summary.getTotalCarbs()));
                        }

                        if (fatTextView != null) {
                            fatTextView.setText(String.format(Locale.getDefault(), "%.1f g", summary.getTotalFat()));
                        }

                        // Update goals if available
                        if (summary.getCalorieGoal() > 0) {
                            if (goalCaloriesTextView != null) {
                                goalCaloriesTextView.setText(String.format(Locale.getDefault(), "/ %.0f", summary.getCalorieGoal()));
                            }

                            if (goalProteinTextView != null) {
                                goalProteinTextView.setText(String.format(Locale.getDefault(), "/ %.0f g", summary.getProteinGoal()));
                            }

                            if (goalCarbsTextView != null) {
                                goalCarbsTextView.setText(String.format(Locale.getDefault(), "/ %.0f g", summary.getCarbGoal()));
                            }

                            if (goalFatTextView != null) {
                                goalFatTextView.setText(String.format(Locale.getDefault(), "/ %.0f g", summary.getFatGoal()));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating UI with nutritional summary", e);
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error loading nutritional summary: " + message);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error loading nutritional summary", Toast.LENGTH_SHORT).show();
                    }

                    // Set default values if there's an error
                    try {
                        if (caloriesTextView != null) caloriesTextView.setText("0");
                        if (proteinTextView != null) proteinTextView.setText("0 g");
                        if (carbsTextView != null) carbsTextView.setText("0 g");
                        if (fatTextView != null) fatTextView.setText("0 g");

                        // Set some default goals
                        if (goalCaloriesTextView != null) goalCaloriesTextView.setText("/ 2000");
                        if (goalProteinTextView != null) goalProteinTextView.setText("/ 150 g");
                        if (goalCarbsTextView != null) goalCarbsTextView.setText("/ 200 g");
                        if (goalFatTextView != null) goalFatTextView.setText("/ 70 g");
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting default values", e);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error calling loadNutritionalSummary", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading nutritional summary", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Setup swipe to delete functionality
     */
    private void setupSwipeToDelete() {
        try {
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder,
                                      @NonNull RecyclerView.ViewHolder target) {
                    return false; // We don't support moving items
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    try {
                        int position = viewHolder.getAdapterPosition();

                        // Safety check
                        if (adapter == null || adapter.filteredFoodItems == null ||
                                position >= adapter.filteredFoodItems.size()) {
                            Log.e(TAG, "Invalid adapter state in onSwiped");
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error removing item", Toast.LENGTH_SHORT).show();
                            }
                            return;
                        }

                        // Get the current displayed items
                        List<FoodItem> currentItems = new ArrayList<>();
                        for (int i = 0; i < adapter.getItemCount(); i++) {
                            currentItems.add(adapter.filteredFoodItems.get(i));
                        }

                        // Get the removed item
                        FoodItem removedItem = currentItems.get(position);

                        // Delete from backend via repository
                        repository.deleteFoodItem(removedItem.getId(), new NutritionRepository.OnResponseListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                Log.d(TAG, "Food item deleted successfully");
                                if (getContext() == null) {
                                    Log.e(TAG, "Context is null in delete onSuccess callback");
                                    return;
                                }

                                // Remove from the adapter list
                                currentItems.remove(position);

                                // Update the adapter
                                adapter = new FoodItemAdapter(currentItems, item ->
                                        Toast.makeText(getContext(), "Tap and hold to remove item", Toast.LENGTH_SHORT).show());
                                recyclerView.setAdapter(adapter);

                                // Reload the nutritional summary
                                loadNutritionalSummary();

                                Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Log.e(TAG, "Error removing item: " + message);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Error removing item: " + message, Toast.LENGTH_SHORT).show();
                                }

                                // Refresh the adapter to show the item again
                                if (adapter != null) {
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error in onSwiped", e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error processing swipe", Toast.LENGTH_SHORT).show();
                        }
                        // Refresh adapter to prevent inconsistent state
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }).attachToRecyclerView(recyclerView);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up swipe to delete", e);
        }
    }

    /**
     * Custom Adapter for DailyTracker to fix the error with the filter method
     */
    private static class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder> {

        private List<FoodItem> foodItems;
        public List<FoodItem> filteredFoodItems;
        private OnFoodItemClickListener listener;

        /**
         * Interface for handling food item click events
         */
        public interface OnFoodItemClickListener {
            void onFoodItemClick(FoodItem foodItem);
        }

        public FoodItemAdapter(List<FoodItem> foodItems, OnFoodItemClickListener listener) {
            this.foodItems = foodItems != null ? foodItems : new ArrayList<>();
            this.filteredFoodItems = new ArrayList<>(this.foodItems);
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
            if (position < filteredFoodItems.size()) {
                FoodItem foodItem = filteredFoodItems.get(position);
                holder.bind(foodItem, listener);
            }
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
            try {
                final String lowerCaseQuery = query.toLowerCase(Locale.getDefault());

                filteredFoodItems.clear();

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
            } catch (Exception e) {
                Log.e(TAG, "Error in filter", e);
                // Reset to all items on error
                filteredFoodItems.clear();
                filteredFoodItems.addAll(foodItems);
                notifyDataSetChanged();
            }
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
                if (foodItem == null) {
                    return;
                }

                if (nameTextView != null) {
                    nameTextView.setText(foodItem.getName());
                }

                if (caloriesTextView != null) {
                    caloriesTextView.setText(String.format(Locale.getDefault(), "%.0f cal", foodItem.getCalories()));
                }

                if (macrosTextView != null) {
                    String macros = String.format(Locale.getDefault(),
                            "P: %.1fg | C: %.1fg | F: %.1fg",
                            foodItem.getProtein(),
                            foodItem.getCarbohydrates(),
                            foodItem.getFat());
                    macrosTextView.setText(macros);
                }

                if (servingTextView != null) {
                    String servingText = String.format(Locale.getDefault(),
                            "Serving: %.0f %s",
                            foodItem.getServingSize(),
                            foodItem.getServingUnit());
                    servingTextView.setText(servingText);
                }

                // Set click listener
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFoodItemClick(foodItem);
                    }
                });
            }
        }
    }
}