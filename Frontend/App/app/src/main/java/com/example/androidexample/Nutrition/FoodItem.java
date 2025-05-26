package com.example.androidexample.Nutrition;

import java.time.LocalDate;

/**
 * Model class representing a food item in the application.
 * This mirrors the backend model with relevant fields.
 */
public class FoodItem {
    private long id;              // Database ID
    private String fdcId;          // FDC ID from the API
    private String name;           // Food name
    private double servingSize;    // Serving size in grams
    private String servingUnit;    // Unit (e.g., "g", "oz")
    private double calories;       // Calories per serving
    private double protein;        // Protein in grams
    private double carbohydrates;  // Carbs in grams
    private double fat;            // Fat in grams
    private double fiber;          // Fiber in grams
    private double sugar;          // Sugar in grams
    private LocalDate consumptionDate; // Date when the food was consumed

    // Default constructor
    public FoodItem() {
        this.consumptionDate = LocalDate.now();
    }

    // Constructor with all fields except consumption date
    public FoodItem(String fdcId, String name, double servingSize, String servingUnit,
                    double calories, double protein, double carbohydrates, double fat,
                    double fiber, double sugar) {
        this.fdcId = fdcId;
        this.name = name;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.consumptionDate = LocalDate.now();
    }

    // Constructor with all fields
    public FoodItem(String fdcId, String name, double servingSize, String servingUnit,
                    double calories, double protein, double carbohydrates, double fat,
                    double fiber, double sugar, LocalDate consumptionDate) {
        this.fdcId = fdcId;
        this.name = name;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.fiber = fiber;
        this.sugar = sugar;
        this.consumptionDate = consumptionDate;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFdcId() {
        return fdcId;
    }

    public void setFdcId(String fdcId) {
        this.fdcId = fdcId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getServingSize() {
        return servingSize;
    }

    public void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public double getSugar() {
        return sugar;
    }

    public void setSugar(double sugar) {
        this.sugar = sugar;
    }

    public LocalDate getConsumptionDate() {
        return consumptionDate;
    }

    public void setConsumptionDate(LocalDate consumptionDate) {
        this.consumptionDate = consumptionDate;
    }
}