package com.example.androidexample.Nutrition;

/**
 * Model class to represent nutritional summary data
 */
public class NutritionalSummary {
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;
    private double totalFiber;
    private double totalSugar;

    private double calorieGoal;
    private double proteinGoal;
    private double carbGoal;
    private double fatGoal;

    // Default constructor
    public NutritionalSummary() {
    }

    // Getters and Setters
    public double getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(double totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(double totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public double getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(double totalFat) {
        this.totalFat = totalFat;
    }

    public double getTotalFiber() {
        return totalFiber;
    }

    public void setTotalFiber(double totalFiber) {
        this.totalFiber = totalFiber;
    }

    public double getTotalSugar() {
        return totalSugar;
    }

    public void setTotalSugar(double totalSugar) {
        this.totalSugar = totalSugar;
    }

    public double getCalorieGoal() {
        return calorieGoal;
    }

    public void setCalorieGoal(double calorieGoal) {
        this.calorieGoal = calorieGoal;
    }

    public double getProteinGoal() {
        return proteinGoal;
    }

    public void setProteinGoal(double proteinGoal) {
        this.proteinGoal = proteinGoal;
    }

    public double getCarbGoal() {
        return carbGoal;
    }

    public void setCarbGoal(double carbGoal) {
        this.carbGoal = carbGoal;
    }

    public double getFatGoal() {
        return fatGoal;
    }

    public void setFatGoal(double fatGoal) {
        this.fatGoal = fatGoal;
    }

    // Calculate percentage of goals met
    public double getCaloriePercentage() {
        return calorieGoal > 0 ? (totalCalories / calorieGoal) * 100 : 0;
    }

    public double getProteinPercentage() {
        return proteinGoal > 0 ? (totalProtein / proteinGoal) * 100 : 0;
    }

    public double getCarbPercentage() {
        return carbGoal > 0 ? (totalCarbs / carbGoal) * 100 : 0;
    }

    public double getFatPercentage() {
        return fatGoal > 0 ? (totalFat / fatGoal) * 100 : 0;
    }
}