package onetoone.nutrition;

import onetoone.users.User;
import onetoone.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionService {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private NutritionGoalRepository nutritionGoalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodDataCentralClient fdcClient;

    /**
     * Search for foods using the FDC API
     */
    public List<Map<String, Object>> searchFoods(String query, int pageSize) {
        return fdcClient.searchFoods(query, pageSize);
    }

    /**
     * Get detailed food information by FDC ID
     */
    public Map<String, Object> getFoodDetails(String fdcId) {
        return fdcClient.getFoodDetails(fdcId);
    }

    /**
     * Add a food item to a user's daily log
     */
    public FoodItem addFoodItem(String username, String fdcId, double servingSize) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        Map<String, Object> foodDetails = fdcClient.getFoodDetails(fdcId);
        if (foodDetails.containsKey("error")) {
            return null;
        }

        // Calculate nutrition based on serving size
        double defaultServingSize = (double) foodDetails.getOrDefault("servingSize", 100.0);
        double ratio = servingSize / defaultServingSize;

        FoodItem foodItem = new FoodItem();
        foodItem.setFdcId(fdcId);
        foodItem.setName((String) foodDetails.getOrDefault("description", "Unknown Food"));
        foodItem.setServingSize(servingSize);
        foodItem.setServingUnit((String) foodDetails.getOrDefault("servingUnit", "g"));
        foodItem.setCalories(((Number) foodDetails.getOrDefault("calories", 0.0)).doubleValue() * ratio);
        foodItem.setProtein(((Number) foodDetails.getOrDefault("protein", 0.0)).doubleValue() * ratio);
        foodItem.setCarbohydrates(((Number) foodDetails.getOrDefault("carbohydrates", 0.0)).doubleValue() * ratio);
        foodItem.setFat(((Number) foodDetails.getOrDefault("fat", 0.0)).doubleValue() * ratio);
        foodItem.setFiber(((Number) foodDetails.getOrDefault("fiber", 0.0)).doubleValue() * ratio);
        foodItem.setSugar(((Number) foodDetails.getOrDefault("sugar", 0.0)).doubleValue() * ratio);
        foodItem.setUser(user);
        foodItem.setConsumptionDate(LocalDate.now());

        return foodItemRepository.save(foodItem);
    }

    /**
     * Add a custom food item
     */
    public FoodItem addCustomFoodItem(String username, FoodItem foodItem) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        foodItem.setUser(user);
        if (foodItem.getConsumptionDate() == null) {
            foodItem.setConsumptionDate(LocalDate.now());
        }

        return foodItemRepository.save(foodItem);
    }

    /**
     * Update a food item
     */
    public FoodItem updateFoodItem(long id, FoodItem updatedItem) {
        FoodItem existingItem = foodItemRepository.findById(id);
        if (existingItem == null) {
            return null;
        }

        // Update fields if provided
        if (updatedItem.getName() != null) {
            existingItem.setName(updatedItem.getName());
        }

        if (updatedItem.getServingSize() > 0) {
            existingItem.setServingSize(updatedItem.getServingSize());
        }

        if (updatedItem.getServingUnit() != null) {
            existingItem.setServingUnit(updatedItem.getServingUnit());
        }

        if (updatedItem.getCalories() > 0) {
            existingItem.setCalories(updatedItem.getCalories());
        }

        if (updatedItem.getProtein() > 0) {
            existingItem.setProtein(updatedItem.getProtein());
        }

        if (updatedItem.getCarbohydrates() > 0) {
            existingItem.setCarbohydrates(updatedItem.getCarbohydrates());
        }

        if (updatedItem.getFat() > 0) {
            existingItem.setFat(updatedItem.getFat());
        }

        if (updatedItem.getFiber() > 0) {
            existingItem.setFiber(updatedItem.getFiber());
        }

        if (updatedItem.getSugar() > 0) {
            existingItem.setSugar(updatedItem.getSugar());
        }

        if (updatedItem.getConsumptionDate() != null) {
            existingItem.setConsumptionDate(updatedItem.getConsumptionDate());
        }

        return foodItemRepository.save(existingItem);
    }

    /**
     * Delete a food item
     */
    public boolean deleteFoodItem(long id) {
        FoodItem item = foodItemRepository.findById(id);
        if (item == null) {
            return false;
        }

        foodItemRepository.delete(item);
        return true;
    }

    /**
     * Get all food items for a user on a specific date
     */
    public List<FoodItem> getUserFoodItemsByDate(String username, LocalDate date) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        return foodItemRepository.findByUserAndConsumptionDate(user, date);
    }

    /**
     * Get all food items for a user
     */
    public List<FoodItem> getUserFoodItems(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        return foodItemRepository.findByUser(user);
    }

    /**
     * Get nutritional summary for a user on a specific date
     */
    public Map<String, Object> getNutritionalSummary(String username, LocalDate date) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        List<FoodItem> foodItems = foodItemRepository.findByUserAndConsumptionDate(user, date);

        // Calculate totals
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalSugar = 0;

        for (FoodItem item : foodItems) {
            totalCalories += item.getCalories();
            totalProtein += item.getProtein();
            totalCarbs += item.getCarbohydrates();
            totalFat += item.getFat();
            totalFiber += item.getFiber();
            totalSugar += item.getSugar();
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("date", date);
        summary.put("totalCalories", totalCalories);
        summary.put("totalProtein", totalProtein);
        summary.put("totalCarbs", totalCarbs);
        summary.put("totalFat", totalFat);
        summary.put("totalFiber", totalFiber);
        summary.put("totalSugar", totalSugar);

        // Get user's nutrition goals
        NutritionGoal goal = nutritionGoalRepository.findByUser(user);
        if (goal != null) {
            summary.put("calorieGoal", goal.getDailyCalorieGoal());
            summary.put("proteinGoal", goal.getProteinGoalGrams());
            summary.put("carbGoal", goal.getCarbGoalGrams());
            summary.put("fatGoal", goal.getFatGoalGrams());

            // Calculate percentage of goals met
            summary.put("caloriePercentage", (totalCalories / goal.getDailyCalorieGoal()) * 100);
            summary.put("proteinPercentage", (totalProtein / goal.getProteinGoalGrams()) * 100);
            summary.put("carbPercentage", (totalCarbs / goal.getCarbGoalGrams()) * 100);
            summary.put("fatPercentage", (totalFat / goal.getFatGoalGrams()) * 100);
        }

        return summary;
    }

    /**
     * Create or update nutrition goals for a user
     */
    public NutritionGoal setNutritionGoals(String username, NutritionGoal newGoal) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        // Check if user already has goals
        NutritionGoal existingGoal = nutritionGoalRepository.findByUser(user);
        if (existingGoal == null) {
            // Create new goal
            existingGoal = new NutritionGoal();
            existingGoal.setUser(user);
        }

        // Update fields
        if (newGoal.getDailyCalorieGoal() > 0) {
            existingGoal.setDailyCalorieGoal(newGoal.getDailyCalorieGoal());
        } else {
            // Calculate recommended calories if not provided
            existingGoal.calculateRecommendedCalories();
        }

        if (newGoal.getProteinGoalPercentage() > 0) {
            existingGoal.setProteinGoalPercentage(newGoal.getProteinGoalPercentage());
        }

        if (newGoal.getCarbGoalPercentage() > 0) {
            existingGoal.setCarbGoalPercentage(newGoal.getCarbGoalPercentage());
        }

        if (newGoal.getFatGoalPercentage() > 0) {
            existingGoal.setFatGoalPercentage(newGoal.getFatGoalPercentage());
        }

        // Ensure the percentages add up to 100%
        int total = existingGoal.getProteinGoalPercentage() +
                existingGoal.getCarbGoalPercentage() +
                existingGoal.getFatGoalPercentage();

        if (total != 100) {
            // Adjust the carb percentage to make it add up to 100%
            existingGoal.setCarbGoalPercentage(
                    existingGoal.getCarbGoalPercentage() + (100 - total)
            );
        }

        return nutritionGoalRepository.save(existingGoal);
    }

    /**
     * Get nutrition goals for a user
     */
    public NutritionGoal getNutritionGoals(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return null;
        }

        NutritionGoal goal = nutritionGoalRepository.findByUser(user);
        if (goal == null) {
            // Create default goals if none exist
            goal = new NutritionGoal();
            goal.setUser(user);
            goal.calculateRecommendedCalories();
            nutritionGoalRepository.save(goal);
        }

        return goal;
    }

    /**
     * Scheduled task to clean up old food entries (optional)
     * Runs at midnight every night
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldEntries() {
        // This is an optional feature to remove entries older than X days
        // For example, to keep only the last 30 days of data
        LocalDate cutoffDate = LocalDate.now().minusDays(30);

        // For each user, remove old entries
        for (User user : userRepository.findAll()) {
            foodItemRepository.deleteByUserAndConsumptionDateBefore(user, cutoffDate);
        }
    }
}