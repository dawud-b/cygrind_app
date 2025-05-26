package com.example.androidexample.Nutrition;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.androidexample.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays recently used food items.
 * Allows the user to search and select from previously logged food items.
 */
public class RecentFoodsFragment extends Fragment implements SearchFoodsAdapter.OnFoodActionListener {

    private static final String TAG = "RecentFoodsFragment";
    private RecyclerView recyclerView;
    private SearchFoodsAdapter adapter;
    private EditText searchEditText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NutritionRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent_foods, container, false);

        try {
            // Initialize repository
            repository = new NutritionRepository(requireContext());

            // Initialize views
            recyclerView = view.findViewById(R.id.recyclerViewFoodResults);
            searchEditText = view.findViewById(R.id.searchBar);
            swipeRefreshLayout = view.findViewById(R.id.food_search_layout);

            // Set up RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            // Initialize adapter with empty list, will be populated from the repository
            adapter = new SearchFoodsAdapter(requireContext(), new ArrayList<>(), this);
            recyclerView.setAdapter(adapter);

            // Set up search functionality
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Filter the adapter as text changes
                    adapter.filterList(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Not used
                }
            });

            // Set up swipe to refresh
            swipeRefreshLayout.setOnRefreshListener(this::loadRecentFoods);

            // Fetch recent food items from the repository
            loadRecentFoods();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error initializing Recent Foods", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Load recent food items from the repository
     */
    private void loadRecentFoods() {
        swipeRefreshLayout.setRefreshing(true);

        repository.getRecentFoods(new NutritionRepository.OnResponseListener<List<FoodItem>>() {
            @Override
            public void onSuccess(List<FoodItem> result) {
                Log.d(TAG, "Loaded " + result.size() + " food items");

                if (getContext() == null) {
                    Log.e(TAG, "Context is null in onSuccess callback");
                    return;
                }

                adapter.updateData(result);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error loading foods: " + message);

                if (getContext() == null) {
                    Log.e(TAG, "Context is null in onError callback");
                    return;
                }

                Toast.makeText(getContext(), "Error loading foods: " + message, Toast.LENGTH_SHORT).show();

                // If we can't load from the repository, use dummy data as fallback
                List<FoodItem> dummyItems = getDummyFoodItems();
                adapter.updateData(dummyItems);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onAddFoodClick(FoodItem foodItem) {
        // Add the selected food item to today's log
        addFoodToDailyLog(foodItem);
    }

    /**
     * Method to handle adding a food item to today's daily log.
     */
    private void addFoodToDailyLog(FoodItem item) {
        Log.d(TAG, "Adding food to daily log: " + item.getName());

        // Create a new FoodItem instance with today's date
        FoodItem foodToAdd = new FoodItem();
        foodToAdd.setFdcId(item.getFdcId());
        foodToAdd.setName(item.getName());
        foodToAdd.setServingSize(item.getServingSize());
        foodToAdd.setServingUnit(item.getServingUnit());
        foodToAdd.setCalories(item.getCalories());
        foodToAdd.setProtein(item.getProtein());
        foodToAdd.setCarbohydrates(item.getCarbohydrates());
        foodToAdd.setFat(item.getFat());
        foodToAdd.setFiber(item.getFiber());
        foodToAdd.setSugar(item.getSugar());
        foodToAdd.setConsumptionDate(LocalDate.now());

        // Add to backend via repository
        repository.addCustomFoodItem(foodToAdd, new NutritionRepository.OnResponseListener<FoodItem>() {
            @Override
            public void onSuccess(FoodItem result) {
                if (getContext() == null) return;

                Toast.makeText(getContext(), "Food added to today's log", Toast.LENGTH_SHORT).show();

                // Notify the DailyTrackerFragment to refresh
                if (getActivity() instanceof NutritionActivity) {
                    NutritionActivity activity = (NutritionActivity) getActivity();
                    activity.refreshDailyTracker();
                }
            }

            @Override
            public void onError(String message) {
                if (getContext() == null) return;

                Log.e(TAG, "Error adding food: " + message);
                Toast.makeText(getContext(), "Error adding food: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Generate dummy food items for the initial display if repository fails.
     * Will be replaced with real data from the database.
     */
    private List<FoodItem> getDummyFoodItems() {
        List<FoodItem> items = new ArrayList<>();

        // Create some sample food items
        items.add(new FoodItem("1", "Apple", 100, "g", 52, 0.3, 14, 0.2, 2.4, 10.3));
        items.add(new FoodItem("2", "Banana", 120, "g", 105, 1.3, 27, 0.4, 3.1, 14.4));
        items.add(new FoodItem("3", "Chicken Breast", 100, "g", 165, 31, 0, 3.6, 0, 0));
        items.add(new FoodItem("4", "Brown Rice", 100, "g", 112, 2.6, 23, 0.9, 1.8, 0.4));
        items.add(new FoodItem("5", "Broccoli", 100, "g", 34, 2.8, 7, 0.4, 2.6, 1.7));
        items.add(new FoodItem("6", "Salmon", 100, "g", 206, 22, 0, 13, 0, 0));
        items.add(new FoodItem("7", "Greek Yogurt", 100, "g", 59, 10, 3.6, 0.4, 0, 3.6));
        items.add(new FoodItem("8", "Eggs", 50, "g", 78, 6.3, 0.6, 5.3, 0, 0.6));
        items.add(new FoodItem("9", "Spinach", 100, "g", 23, 2.9, 3.6, 0.4, 2.2, 0.4));
        items.add(new FoodItem("10", "Sweet Potato", 100, "g", 86, 1.6, 20, 0.1, 3.0, 4.2));

        return items;
    }
}