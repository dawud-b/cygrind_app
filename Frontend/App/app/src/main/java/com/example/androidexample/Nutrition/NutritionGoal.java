package com.example.androidexample.Nutrition;

/**
 * Model class representing a user's nutrition goals.
 * This mirrors the backend NutritionGoal entity.
 */
public class NutritionGoal {
    private int dailyCalorieGoal;
    private int proteinGoalPercentage;  // Percentage of total calories
    private int carbGoalPercentage;     // Percentage of total calories
    private int fatGoalPercentage;      // Percentage of total calories

    /**
     * Default constructor with a standard macro split (40% carbs, 30% protein, 30% fat)
     */
    public NutritionGoal() {
        this.proteinGoalPercentage = 30;
        this.carbGoalPercentage = 40;
        this.fatGoalPercentage = 30;
    }

    /**
     * Constructor with all parameters
     */
    public NutritionGoal(int dailyCalorieGoal, int proteinGoalPercentage,
                         int carbGoalPercentage, int fatGoalPercentage) {
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.proteinGoalPercentage = proteinGoalPercentage;
        this.carbGoalPercentage = carbGoalPercentage;
        this.fatGoalPercentage = fatGoalPercentage;
    }

    /**
     * Calculate the protein goal in grams
     * @return protein goal in grams
     */
    public double getProteinGoalGrams() {
        // Protein: 4 calories per gram
        return (dailyCalorieGoal * (proteinGoalPercentage / 100.0)) / 4.0;
    }

    /**
     * Calculate the carbohydrate goal in grams
     * @return carb goal in grams
     */
    public double getCarbGoalGrams() {
        // Carbs: 4 calories per gram
        return (dailyCalorieGoal * (carbGoalPercentage / 100.0)) / 4.0;
    }

    /**
     * Calculate the fat goal in grams
     * @return fat goal in grams
     */
    public double getFatGoalGrams() {
        // Fat: 9 calories per gram
        return (dailyCalorieGoal * (fatGoalPercentage / 100.0)) / 9.0;
    }

    // Getters and Setters
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

    /**
     * Calculate recommended calorie intake based on user data
     * This would be called with actual user data in a real implementation
     *
     * @param weight User's weight in pounds
     * @param height User's height in inches
     * @param age User's age in years
     * @param isMale User's gender (true for male, false for female)
     */
    public void calculateRecommendedCalories(double weight, double height, int age, boolean isMale) {
        // Convert to metric
        double weightInKg = weight * 0.453592; // Convert lbs to kg
        double heightInCm = height * 2.54;     // Convert inches to cm

        // Harris-Benedict equation for BMR (Basal Metabolic Rate)
        double bmr;
        if (isMale) {
            // Men: BMR = 88.362 + (13.397 × weight in kg) + (4.799 × height in cm) - (5.677 × age in years)
            bmr = 88.362 + (13.397 * weightInKg) + (4.799 * heightInCm) - (5.677 * age);
        } else {
            // Women: BMR = 447.593 + (9.247 × weight in kg) + (3.098 × height in cm) - (4.330 × age in years)
            bmr = 447.593 + (9.247 * weightInKg) + (3.098 * heightInCm) - (4.330 * age);
        }

        // Assuming moderate activity level (1.55 multiplier)
        this.dailyCalorieGoal = (int) (bmr * 1.55);
    }
}