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
        private double tempMin;
        private double tempMax;
        // ... (alte câmpuri existente)

        // Adaugă gettere și settere
        public double getTempMin() {
            return tempMin;
        }

        public void setTempMin(double tempMin) {
            this.tempMin = tempMin;
        }

        public double getTempMax() {
            return tempMax;
        }

        public void setTempMax(double tempMax) {
            this.tempMax = tempMax;
        }



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