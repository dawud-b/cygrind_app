package onetoone.nutrition;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import onetoone.users.User;

@Entity
public class NutritionGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int dailyCalorieGoal;
    private int proteinGoalPercentage;  // Percentage of total calories
    private int carbGoalPercentage;     // Percentage of total calories
    private int fatGoalPercentage;      // Percentage of total calories

    @OneToOne
    @JsonIgnore
    private User user;

    public NutritionGoal() {
        // Default macro split (40% carbs, 30% protein, 30% fat)
        this.proteinGoalPercentage = 30;
        this.carbGoalPercentage = 40;
        this.fatGoalPercentage = 30;
    }

    public NutritionGoal(User user, int dailyCalorieGoal, int proteinGoalPercentage,
                         int carbGoalPercentage, int fatGoalPercentage) {
        this.user = user;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.proteinGoalPercentage = proteinGoalPercentage;
        this.carbGoalPercentage = carbGoalPercentage;
        this.fatGoalPercentage = fatGoalPercentage;
    }

    // Auto-calculate recommended daily calorie intake based on user data
    public void calculateRecommendedCalories() {
        if (user == null) {
            return;
        }

        // Basic Harris-Benedict equation for BMR (Basal Metabolic Rate)
        // Men: BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677 × age in years)
        // Women: BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) - (4.330 × age in years)

        // For simplicity, we'll use a general formula and activity factor
        double weightInKg = user.getWeight() * 0.453592; // Convert lbs to kg
        double heightInCm = user.getHeight() * 2.54;     // Convert inches to cm
        int age = user.getAge();

        // Using male formula as default (can be enhanced with gender info)
        double bmr = 88.362 + (13.397 * weightInKg) + (4.799 * heightInCm) - (5.677 * age);

        // Assuming moderate activity level (1.55 multiplier)
        this.dailyCalorieGoal = (int) (bmr * 1.55);
    }

    // Calculate macro nutrient goals in grams
    public double getProteinGoalGrams() {
        // Protein: 4 calories per gram
        return (dailyCalorieGoal * (proteinGoalPercentage / 100.0)) / 4.0;
    }

    public double getCarbGoalGrams() {
        // Carbs: 4 calories per gram
        return (dailyCalorieGoal * (carbGoalPercentage / 100.0)) / 4.0;
    }

    public double getFatGoalGrams() {
        // Fat: 9 calories per gram
        return (dailyCalorieGoal * (fatGoalPercentage / 100.0)) / 9.0;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
    }

    public int getProteinGoalPercentage() {
        return proteinGoalPercentage;
    }

    public void setProteinGoalPercentage(int proteinGoalPercentage) {
        this.proteinGoalPercentage = proteinGoalPercentage;
    }

    public int getCarbGoalPercentage() {
        return carbGoalPercentage;
    }

    public void setCarbGoalPercentage(int carbGoalPercentage) {
        this.carbGoalPercentage = carbGoalPercentage;
    }

    public int getFatGoalPercentage() {
        return fatGoalPercentage;
    }

    public void setFatGoalPercentage(int fatGoalPercentage) {
        this.fatGoalPercentage = fatGoalPercentage;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}