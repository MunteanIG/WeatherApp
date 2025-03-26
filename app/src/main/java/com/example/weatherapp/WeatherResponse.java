package com.example.weatherapp;

public class WeatherResponse {
    private Main main;
    private Weather[] weather;
    private Wind wind;
    private String name;

    // Getters
    public Main getMain() {
        return main;
    }

    public Weather[] getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public String getName() {
        return name;
    }

    // Nested classes
    public static class Main {
        private double temp;
        private double feelsLike;  // Changed to camelCase
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public int getHumidity() {
            return humidity;
        }
    }

    public static class Weather {
        private String description;
        private String icon;

        public String getDescription() {
            return description;
        }

        public String getIcon() {
            return icon;
        }
    }

    public static class Wind {
        private double speed; // speed in m/s

        public double getSpeed() {
            return speed;
        }

    }
}