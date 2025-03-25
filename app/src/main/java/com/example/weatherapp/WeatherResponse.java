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

    private Wind wind;

    public static class Wind {
        private double speed; // viteza în m/s

        public double getSpeed() {
            return speed;
        }
    }

    // Getter pentru wind
    public Wind getWind() {
        return wind;
    }
}
