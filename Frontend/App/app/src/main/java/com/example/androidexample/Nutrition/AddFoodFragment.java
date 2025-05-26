package com.example.androidexample.Nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidexample.R;

import java.time.LocalDate;

/**
 * Fragment for adding new custom food items to the nutrition tracker.
 */
public class AddFoodFragment extends Fragment {

    private static final String TAG = "AddFoodFragment";
    private EditText nameEditText;
    private EditText servingSizeEditText;
    private EditText servingUnitEditText;
    private EditText caloriesEditText;
    private EditText proteinEditText;
    private EditText carbsEditText;
    private EditText fatEditText;
    private EditText fiberEditText;
    private EditText sugarEditText;
    private Button addButton;
    private NutritionRepository repository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_food, container, false);

        // Initialize repository
        repository = new NutritionRepository(requireContext());

        try {
            // Initialize views
            nameEditText = view.findViewById(R.id.edit_food_name);
            servingSizeEditText = view.findViewById(R.id.edit_serving_size);
            servingUnitEditText = view.findViewById(R.id.edit_serving_unit);
            caloriesEditText = view.findViewById(R.id.edit_calories);
            proteinEditText = view.findViewById(R.id.edit_protein);
            carbsEditText = view.findViewById(R.id.edit_carbs);
            fatEditText = view.findViewById(R.id.edit_fat);
            fiberEditText = view.findViewById(R.id.edit_fiber);
            sugarEditText = view.findViewById(R.id.edit_sugar);
            addButton = view.findViewById(R.id.btn_add_food);

            // Check that all views were found successfully
            if (nameEditText == null || servingSizeEditText == null ||
                    servingUnitEditText == null || caloriesEditText == null) {
                Log.e(TAG, "One or more required views not found in layout");
                Toast.makeText(getContext(), "Error initializing layout", Toast.LENGTH_SHORT).show();
                return view;
            }

            // Set up button click listener
            addButton.setOnClickListener(v -> {
                try {
                    if (validateInputs()) {
                        createFoodItem();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing form submission", e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            Toast.makeText(getContext(), "Error initializing form", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    /**
     * Validate all input fields
     * @return true if all inputs are valid, false otherwise
     */
    private boolean validateInputs() {
        boolean isValid = true;

        try {
            // Validate name
            if (nameEditText.getText().toString().trim().isEmpty()) {
                nameEditText.setError("Food name is required");
                isValid = false;
            }

            // Validate serving size
            if (servingSizeEditText.getText().toString().trim().isEmpty()) {
                servingSizeEditText.setError("Serving size is required");
                isValid = false;
            } else {
                try {
                    double size = Double.parseDouble(servingSizeEditText.getText().toString().trim());
                    if (size <= 0) {
                        servingSizeEditText.setError("Serving size must be positive");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    servingSizeEditText.setError("Invalid number format");
                    isValid = false;
                }
            }

            // Validate serving unit
            if (servingUnitEditText.getText().toString().trim().isEmpty()) {
                servingUnitEditText.setError("Serving unit is required");
                isValid = false;
            }

            // Validate calories
            if (caloriesEditText.getText().toString().trim().isEmpty()) {
                caloriesEditText.setError("Calories are required");
                isValid = false;
            } else {
                try {
                    double calories = Double.parseDouble(caloriesEditText.getText().toString().trim());
                    if (calories < 0) {
                        caloriesEditText.setError("Calories cannot be negative");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    caloriesEditText.setError("Invalid number format");
                    isValid = false;
                }
            }

            // Optional fields don't need validation, but check for valid format if provided
            checkOptionalField(proteinEditText);
            checkOptionalField(carbsEditText);
            checkOptionalField(fatEditText);
            checkOptionalField(fiberEditText);
            checkOptionalField(sugarEditText);
        } catch (Exception e) {
            Log.e(TAG, "Error validating inputs", e);
            Toast.makeText(getContext(), "Error validating inputs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        return isValid;
    }

    /**
     * Check optional numeric field for valid format
     */
    private void checkOptionalField(EditText field) {
        String value = field.getText().toString().trim();
        if (!value.isEmpty()) {
            try {
                double numValue = Double.parseDouble(value);
                if (numValue < 0) {
                    field.setError("Value cannot be negative");
                }
            } catch (NumberFormatException e) {
                field.setError("Invalid number format");
            }
        }
    }

    /**
     * Create a new food item from the input fields
     */
    private void createFoodItem() {
        Log.d(TAG, "Creating food item from form inputs");
        try {
            // Extract values from input fields
            String name = nameEditText.getText().toString().trim();
            double servingSize = Double.parseDouble(servingSizeEditText.getText().toString().trim());
            String servingUnit = servingUnitEditText.getText().toString().trim();
            double calories = Double.parseDouble(caloriesEditText.getText().toString().trim());

            // Parse optional fields, defaulting to 0 if empty
            double protein = proteinEditText.getText().toString().trim().isEmpty() ?
                    0 : Double.parseDouble(proteinEditText.getText().toString().trim());

            double carbs = carbsEditText.getText().toString().trim().isEmpty() ?
                    0 : Double.parseDouble(carbsEditText.getText().toString().trim());

            double fat = fatEditText.getText().toString().trim().isEmpty() ?
                    0 : Double.parseDouble(fatEditText.getText().toString().trim());

            double fiber = fiberEditText.getText().toString().trim().isEmpty() ?
                    0 : Double.parseDouble(fiberEditText.getText().toString().trim());

            double sugar = sugarEditText.getText().toString().trim().isEmpty() ?
                    0 : Double.parseDouble(sugarEditText.getText().toString().trim());

            // Create a new food item with generated ID
            FoodItem newFoodItem = new FoodItem();
            newFoodItem.setFdcId("custom_" + System.currentTimeMillis()); // generate pseudo-unique ID
            newFoodItem.setName(name);
            newFoodItem.setServingSize(servingSize);
            newFoodItem.setServingUnit(servingUnit);
            newFoodItem.setCalories(calories);
            newFoodItem.setProtein(protein);
            newFoodItem.setCarbohydrates(carbs);
            newFoodItem.setFat(fat);
            newFoodItem.setFiber(fiber);
            newFoodItem.setSugar(sugar);
            newFoodItem.setConsumptionDate(LocalDate.now());

            Log.d(TAG, "Food item created: " + name + ", " + calories + " calories");

            // Save to backend via repository
            repository.addCustomFoodItem(newFoodItem, new NutritionRepository.OnResponseListener<FoodItem>() {
                @Override
                public void onSuccess(FoodItem result) {
                    // Show success message
                    Log.d(TAG, "Food item added successfully with ID: " + result.getId());
                    Toast.makeText(getContext(), "Food item added to today's log", Toast.LENGTH_SHORT).show();

                    // Clear input fields
                    clearInputFields();

                    // Notify the DailyTrackerFragment to refresh
                    if (getActivity() instanceof NutritionActivity) {
                        NutritionActivity activity = (NutritionActivity) getActivity();
                        activity.refreshDailyTracker();
                    }
                }

                @Override
                public void onError(String message) {
                    Log.e(TAG, "Error adding food: " + message);
                    Toast.makeText(getContext(), "Error adding food: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating food item", e);
            Toast.makeText(getContext(), "Error creating food item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clear all input fields after adding a food item
     */
    private void clearInputFields() {
        try {
            nameEditText.setText("");
            servingSizeEditText.setText("");
            servingUnitEditText.setText("");
            caloriesEditText.setText("");
            proteinEditText.setText("");
            carbsEditText.setText("");
            fatEditText.setText("");
            fiberEditText.setText("");
            sugarEditText.setText("");

            // Focus on the name field for the next item
            nameEditText.requestFocus();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing input fields", e);
        }
    }
}