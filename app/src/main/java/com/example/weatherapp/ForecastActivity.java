package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ForecastActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private WeatherViewModel viewModel;
    private TextView tvCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        // Inițializare componente
        tvCity = findViewById(R.id.tvForecastCity);
        recyclerView = findViewById(R.id.rvForecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForecastAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Inițializare ViewModel
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // Observă datele de prognoză
        viewModel.getForecastData().observe(this, forecastResponse -> {
            if (forecastResponse != null) {
                tvCity.setText(forecastResponse.getCity().getName());

                // Filtrează pentru a afișa doar o prognoză pe zi (la ora 12:00 de exemplu)
                List<ForecastResponse.ForecastItem> dailyForecasts = new ArrayList<>();
                for (ForecastResponse.ForecastItem item : forecastResponse.getList()) {
                    // Verifică dacă este ora 12:00
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(item.getDt() * 1000);
                    if (calendar.get(Calendar.HOUR_OF_DAY) == 12) {
                        dailyForecasts.add(item);
                    }
                }
                adapter.updateData(dailyForecasts);
            }
        });

        // Observă mesajele de eroare
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });

        // Obține prognoza
        String city = getIntent().getStringExtra("city");
        if (city != null) {
            viewModel.fetchFiveDayForecast(city);
        } else {
            // Încearcă să obții locația curentă sau folosește orașul implicit
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                viewModel.fetchFiveDayForecastByLocation(
                                        location.getLatitude(),
                                        location.getLongitude()
                                );
                            } else {
                                viewModel.fetchFiveDayForecast("București");
                            }
                        });
            } else {
                viewModel.fetchFiveDayForecast("București");
            }
        }
    }
}