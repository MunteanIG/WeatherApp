package com.example.weatherapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.WeatherResponse;
import com.example.weatherapp.WeatherService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherViewModel extends ViewModel {
    private MutableLiveData<WeatherResponse> weatherData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final String API_KEY = "b434ad86a65e25eb722d4e736180dd49";

    public LiveData<WeatherResponse> getWeatherData() {
        return weatherData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchWeatherData(String city) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(city, API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherData.postValue(response.body());
                } else {
                    errorMessage.postValue("Orașul nu a fost găsit!");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                errorMessage.postValue("Eroare de rețea: " + t.getMessage());
            }
        });
    }

    public void fetchWeatherByLocation(double lat, double lon) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getWeatherByCoordinates(lat, lon, API_KEY);

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherData.postValue(response.body());
                } else {
                    errorMessage.postValue("Locația nu este validă");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                errorMessage.postValue("Eroare la obținerea vremii");
            }
        });
    }
}