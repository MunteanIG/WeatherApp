package com.example.weatherapp;

public class FavoriteCity {
    private String name;
    private double temperature;
    private String description;
    private String iconCode;

    // Constructor
    public FavoriteCity(String name, double temperature, String description, String iconCode) {
        this.name = name;
        this.temperature = temperature;
        this.description = description;
        this.iconCode = iconCode;
    }

    // Adaugă getters
    public String getName() { return name; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public String getIconCode() { return iconCode; }
}