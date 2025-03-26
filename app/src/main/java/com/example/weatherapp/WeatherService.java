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

    @GET("weather?units=metric")
    Call<WeatherResponse> getWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey
    );

    @GET("forecast?units=metric")
    Call<ForecastResponse> getFiveDayForecast(
            @Query("q") String city,
            @Query("appid") String apiKey
    );

    @GET("forecast?units=metric")
    Call<ForecastResponse> getFiveDayForecastByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey
    );
}
