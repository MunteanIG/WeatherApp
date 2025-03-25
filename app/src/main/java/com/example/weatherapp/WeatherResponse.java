package com.example.weatherapp;

public class WeatherResponse {
    private Main main;
    private Weather[] weather;
    private String name;

    // Getteri
    public Main getMain() {
        return main;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public String getName() {
        return name;
    }

    // Clase interne pentru date nested
    public static class Main {
        private double temp;
        private double feels_like;
        private int humidity;

        public double getTemp() {
            return temp;
        }
    }

    public static class Weather {
        private String description;
        private String icon;

        public String getDescription() {
            return description;
        }
    }
}
