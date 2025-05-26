package onetoone.nutrition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "Nutrition Controller", description = "REST APIs related to Nutrition Entity")
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    @Operation(summary = "Search foods", description = "Search for foods in the FDC database with pagination support.")
    @GetMapping("/nutrition/search")
    public List<Map<String, Object>> searchFoods(
            @RequestParam String query,
            @RequestParam(defaultValue = "25") int pageSize) {
        return nutritionService.searchFoods(query, pageSize);
    }

    @Operation(summary = "Get food details", description = "Retrieve detailed information about a specific food by its FDC ID.")
    @GetMapping("/nutrition/food/{fdcId}")
    public Map<String, Object> getFoodDetails(@PathVariable String fdcId) {
        return nutritionService.getFoodDetails(fdcId);
    }

    @Operation(summary = "Add food item", description = "Add a food item from the FDC database to a user's daily nutrition log.")
    @PostMapping("/{username}/nutrition/add")
    public FoodItem addFoodItem(
            @PathVariable String username,
            @RequestParam String fdcId,
            @RequestParam double servingSize) {
        return nutritionService.addFoodItem(username, fdcId, servingSize);
    }
    @Operation(summary = "Add custom food", description = "Add a user-defined custom food item to a user's daily nutrition log.")
    @PostMapping("/{username}/nutrition/custom")
    public ResponseEntity<?> addCustomFoodItem(
            @PathVariable String username,
            @RequestBody FoodItem foodItem) {
        FoodItem savedItem = nutritionService.addCustomFoodItem(username, foodItem);
        if (savedItem == null) {
            return ResponseEntity.badRequest().body("Failed to add food item. User not found.");
        }
        return ResponseEntity.ok(savedItem);
    }


    @Operation(summary = "Update food item", description = "Modify the details of an existing food item in a user's nutrition log.")
    @PutMapping("/{username}/nutrition/{id}")
    public FoodItem updateFoodItem(
            @PathVariable String username,
            @PathVariable long id,
            @RequestBody FoodItem foodItem) {
        return nutritionService.updateFoodItem(id, foodItem);
    }

    @Operation(summary = "Delete food item", description = "Remove a food item from a user's nutrition log.")
    @DeleteMapping("/{username}/nutrition/{id}")
    public String deleteFoodItem(
            @PathVariable String username,
            @PathVariable long id) {
        boolean success = nutritionService.deleteFoodItem(id);
        if (success) {
            return "Food item deleted successfully";
        } else {
            return "Failed to delete food item";
        }
    }

    @Operation(summary = "Get daily food items", description = "Retrieve all food items logged by a user on a specific date.")
    @GetMapping("/{username}/nutrition/daily")
    public List<FoodItem> getUserDailyFoodItems(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Use current date if not specified
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return nutritionService.getUserFoodItemsByDate(username, targetDate);
    }

    @Operation(summary = "Get nutrition history", description = "Retrieve the complete history of food items logged by a user.")
    @GetMapping("/{username}/nutrition/history")
    public List<FoodItem> getUserFoodHistory(@PathVariable String username) {
        return nutritionService.getUserFoodItems(username);
    }

    @Operation(summary = "Get nutrition summary", description = "Generate a nutritional summary for a user on a specific date.")
    @GetMapping("/{username}/nutrition/summary")
    public Map<String, Object> getNutritionalSummary(
            @PathVariable String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Use current date if not specified
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return nutritionService.getNutritionalSummary(username, targetDate);
    }

    @Operation(summary = "Set nutrition goals", description = "Create or update a user's nutrition goals.")
    @PostMapping("/{username}/nutrition/goals")
    public NutritionGoal setNutritionGoals(
            @PathVariable String username,
            @RequestBody NutritionGoal nutritionGoal) {
        return nutritionService.setNutritionGoals(username, nutritionGoal);
    }

    @Operation(summary = "Get nutrition goals", description = "Retrieve a user's current nutrition goals.")
    @GetMapping("/{username}/nutrition/goals")
    public NutritionGoal getNutritionGoals(@PathVariable String username) {
        return nutritionService.getNutritionGoals(username);
    }
}