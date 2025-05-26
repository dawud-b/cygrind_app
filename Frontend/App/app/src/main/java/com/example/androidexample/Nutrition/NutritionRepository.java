package com.example.androidexample.Nutrition;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.androidexample.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class to handle interactions with the backend for nutrition data.
 */
public class NutritionRepository {
    private static final String TAG = "NutritionRepository";
    private static final String BASE_URL = "http://coms-3090-035.class.las.iastate.edu:8080";
    private final Context context;
    private final String username;

    public NutritionRepository(Context context) {
        this.context = context;
        // Get username from SharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        this.username = sharedPreferences.getString("username", "");
        Log.d(TAG, "Repository initialized for user: " + username);
    }

    /**
     * Interface for handling responses from the repository
     */
    public interface OnResponseListener<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    /**
     * Get recent food items from the backend
     */
    public void getRecentFoods(OnResponseListener<List<FoodItem>> listener) {
        if (username.isEmpty()) {
            Log.e(TAG, "Username is empty, cannot fetch recent foods");
            listener.onError("Username not found");
            return;
        }

        String url = BASE_URL + "/" + username + "/nutrition/history";
        Log.d(TAG, "Fetching recent foods from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Recent foods response received: " + response.toString());
                        List<FoodItem> foodItems = new ArrayList<>();
                        try {
                            // Parse the JSON array into FoodItem objects
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject foodJson = response.getJSONObject(i);
                                FoodItem food = jsonToFoodItem(foodJson);
                                foodItems.add(food);
                            }
                            listener.onSuccess(foodItems);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing food items", e);
                            listener.onError("Failed to parse food items: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching recent foods", error);
                        String errorMsg = error.getMessage();
                        if (errorMsg == null) {
                            errorMsg = "Network error";
                        }
                        listener.onError("Failed to fetch recent foods: " + errorMsg);
                    }
                });

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout in milliseconds
                1,      // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Get daily food items for a specific date
     */
    public void getDailyFoodItems(LocalDate date, OnResponseListener<List<FoodItem>> listener) {
        if (username.isEmpty()) {
            Log.e(TAG, "Username is empty, cannot fetch daily food items");
            listener.onError("Username not found");
            return;
        }

        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        String url = BASE_URL + "/" + username + "/nutrition/daily?date=" + formattedDate;
        Log.d(TAG, "Fetching daily food items from: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "Daily food items response received: " + response.toString());
                        List<FoodItem> foodItems = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject foodJson = response.getJSONObject(i);
                                FoodItem food = jsonToFoodItem(foodJson);
                                foodItems.add(food);
                            }
                            listener.onSuccess(foodItems);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing daily food items", e);
                            listener.onError("Failed to parse daily food items: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching daily food items", error);
                        String errorMsg = error.getMessage();
                        if (errorMsg == null) {
                            errorMsg = "Network error";
                        }
                        listener.onError("Failed to fetch daily food items: " + errorMsg);
                    }
                });

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout in milliseconds
                1,      // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Get nutritional summary for a specific date
     */
    public void getNutritionalSummary(LocalDate date, OnResponseListener<NutritionalSummary> listener) {
        if (username.isEmpty()) {
            Log.e(TAG, "Username is empty, cannot fetch nutritional summary");
            listener.onError("Username not found");
            return;
        }

        String formattedDate = date.format(DateTimeFormatter.ISO_DATE);
        String url = BASE_URL + "/" + username + "/nutrition/summary?date=" + formattedDate;
        Log.d(TAG, "Fetching nutritional summary from: " + url);

        // Use default values for the summary if the request fails
        NutritionalSummary fallbackSummary = new NutritionalSummary();
        fallbackSummary.setCalorieGoal(2000);
        fallbackSummary.setProteinGoal(150);
        fallbackSummary.setCarbGoal(200);
        fallbackSummary.setFatGoal(70);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Nutritional summary response received: " + response.toString());
                        try {
                            NutritionalSummary summary = new NutritionalSummary();
                            summary.setTotalCalories(response.optDouble("totalCalories", 0));
                            summary.setTotalProtein(response.optDouble("totalProtein", 0));
                            summary.setTotalCarbs(response.optDouble("totalCarbs", 0));
                            summary.setTotalFat(response.optDouble("totalFat", 0));

                            // Get goals if they exist
                            if (response.has("calorieGoal")) {
                                summary.setCalorieGoal(response.getDouble("calorieGoal"));
                                summary.setProteinGoal(response.getDouble("proteinGoal"));
                                summary.setCarbGoal(response.getDouble("carbGoal"));
                                summary.setFatGoal(response.getDouble("fatGoal"));
                            } else {
                                // Use fallback values
                                summary.setCalorieGoal(fallbackSummary.getCalorieGoal());
                                summary.setProteinGoal(fallbackSummary.getProteinGoal());
                                summary.setCarbGoal(fallbackSummary.getCarbGoal());
                                summary.setFatGoal(fallbackSummary.getFatGoal());
                            }

                            listener.onSuccess(summary);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing nutritional summary", e);
                            listener.onError("Failed to parse nutritional summary: " + e.getMessage());
                            // Use fallback values
                            listener.onSuccess(fallbackSummary);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching nutritional summary", error);
                        // Use fallback values on error
                        listener.onSuccess(fallbackSummary);
                    }
                });

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout in milliseconds
                1,      // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Add a custom food item to the backend
     */
    public void addCustomFoodItem(FoodItem foodItem, OnResponseListener<FoodItem> listener) {
        if (username.isEmpty()) {
            Log.e(TAG, "Username is empty, cannot add custom food item");
            listener.onError("Username not found");
            return;
        }

        String url = BASE_URL + "/" + username + "/nutrition/custom";
        Log.d(TAG, "Adding custom food item to: " + url);

        // Convert FoodItem to JSONObject
        JSONObject jsonRequest = foodItemToJson(foodItem);
        Log.d(TAG, "Request body: " + jsonRequest.toString());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Add custom food response received: " + response.toString());
                        try {
                            FoodItem addedFood = jsonToFoodItem(response);
                            listener.onSuccess(addedFood);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing added food", e);
                            listener.onError("Failed to parse added food response: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error adding custom food", error);
                        String errorMsg = error.getMessage();
                        if (errorMsg == null) {
                            errorMsg = "Network error";
                        }
                        listener.onError("Failed to add custom food: " + errorMsg);
                    }
                });

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout in milliseconds
                1,      // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Delete a food item from the backend
     */
    public void deleteFoodItem(long id, OnResponseListener<Boolean> listener) {
        if (username.isEmpty()) {
            Log.e(TAG, "Username is empty, cannot delete food item");
            listener.onError("Username not found");
            return;
        }

        String url = BASE_URL + "/" + username + "/nutrition/" + id;
        Log.d(TAG, "Deleting food item from: " + url);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Delete food item response received: " + response);
                        listener.onSuccess(true);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error deleting food item", error);
                        String errorMsg = error.getMessage();
                        if (errorMsg == null) {
                            errorMsg = "Network error";
                        }
                        listener.onError("Failed to delete food item: " + errorMsg);
                    }
                });

        // Set retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // Timeout in milliseconds
                1,      // Max retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(context).addToRequestQueue(request);
    }

    /**
     * Convert JSONObject to FoodItem
     */
    private FoodItem jsonToFoodItem(JSONObject json) throws JSONException {
        FoodItem foodItem = new FoodItem();

        // Set ID
        if (json.has("id")) {
            foodItem.setId(json.getLong("id"));
        }

        // Set other properties
        if (json.has("fdcId")) {
            foodItem.setFdcId(json.getString("fdcId"));
        } else {
            foodItem.setFdcId("custom_" + System.currentTimeMillis());
        }

        if (json.has("name")) {
            foodItem.setName(json.getString("name"));
        }

        if (json.has("servingSize")) {
            foodItem.setServingSize(json.getDouble("servingSize"));
        }

        if (json.has("servingUnit")) {
            foodItem.setServingUnit(json.getString("servingUnit"));
        } else {
            foodItem.setServingUnit("g");
        }

        if (json.has("calories")) {
            foodItem.setCalories(json.getDouble("calories"));
        }

        if (json.has("protein")) {
            foodItem.setProtein(json.getDouble("protein"));
        }

        if (json.has("carbohydrates")) {
            foodItem.setCarbohydrates(json.getDouble("carbohydrates"));
        }

        if (json.has("fat")) {
            foodItem.setFat(json.getDouble("fat"));
        }

        if (json.has("fiber")) {
            foodItem.setFiber(json.getDouble("fiber"));
        }

        if (json.has("sugar")) {
            foodItem.setSugar(json.getDouble("sugar"));
        }

        if (json.has("consumptionDate")) {
            try {
                String dateStr = json.getString("consumptionDate");
                foodItem.setConsumptionDate(LocalDate.parse(dateStr));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing date", e);
                foodItem.setConsumptionDate(LocalDate.now());
            }
        } else {
            foodItem.setConsumptionDate(LocalDate.now());
        }

        return foodItem;
    }

    /**
     * Convert FoodItem to JSONObject
     */
    private JSONObject foodItemToJson(FoodItem foodItem) {
        JSONObject json = new JSONObject();

        try {
            if (foodItem.getId() > 0) {
                json.put("id", foodItem.getId());
            }

            json.put("fdcId", foodItem.getFdcId());
            json.put("name", foodItem.getName());
            json.put("servingSize", foodItem.getServingSize());
            json.put("servingUnit", foodItem.getServingUnit());
            json.put("calories", foodItem.getCalories());
            json.put("protein", foodItem.getProtein());
            json.put("carbohydrates", foodItem.getCarbohydrates());
            json.put("fat", foodItem.getFat());
            json.put("fiber", foodItem.getFiber());
            json.put("sugar", foodItem.getSugar());

            if (foodItem.getConsumptionDate() != null) {
                json.put("consumptionDate", foodItem.getConsumptionDate().toString());
            } else {
                json.put("consumptionDate", LocalDate.now().toString());
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON from FoodItem", e);
        }

        return json;
    }
}