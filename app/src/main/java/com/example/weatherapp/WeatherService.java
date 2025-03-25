package com.example.weatherapp;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather?units=metric")  // Unități metrice (°C)
    Call<WeatherResponse> getCurrentWeather(
            @Query("q") String city,  // Orașul (ex: "București")
            @Query("appid") String apiKey  // Cheia ta de API
    );
}
