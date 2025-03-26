package com.example.weatherapp;

import java.util.List;

public class ForecastResponse {
    private List<ForecastItem> list;
    private City city;

    // Getters
    public List<ForecastItem> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }

    public static class ForecastItem {
        private long dt;
        private WeatherResponse.Main main;
        private List<WeatherResponse.Weather> weather;
        private WeatherResponse.Wind wind;

        // Getters
        public long getDt() {
            return dt;
        }

        public WeatherResponse.Main getMain() {
            return main;
        }

        public List<WeatherResponse.Weather> getWeather() {
            return weather;
        }

        public WeatherResponse.Wind getWind() {
            return wind;
        }
    }

    public static class City {
        private String name;

        public String getName() {
            return name;
        }
    }
}
