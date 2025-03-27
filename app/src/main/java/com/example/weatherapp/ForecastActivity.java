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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ForecastActivity extends AppCompatActivity {

    /* Elemente UI */

    private RecyclerView recyclerView;
    private ForecastAdapter adapter;
    private WeatherViewModel viewModel;
    private TextView tvCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /* Initializari */

        tvCity = findViewById(R.id.tvForecastCity);
        recyclerView = findViewById(R.id.rvForecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ForecastAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        /* Config observatori vreme forecast*/

        viewModel.getForecastData().observe(this, forecastResponse -> {
            if (forecastResponse != null) {
                tvCity.setText(forecastResponse.getCity().getName());

                // Datele pentru 5 zile
                List<ForecastResponse.ForecastItem> dailyForecasts = new ArrayList<>();
                Map<String, ForecastResponse.ForecastItem> dayMap = new HashMap<>();

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, 1);

                // Parcurge fiecare zi
                for (int i = 0; i < 5; i++) {
                    String currentDay = getDayString(calendar.getTime());

                    // Gaseste toate prognozele pentru ziua curenta
                    List<ForecastResponse.ForecastItem> dayForecasts = new ArrayList<>();
                    for (ForecastResponse.ForecastItem item : forecastResponse.getList()) {
                        String itemDay = getDayString(new Date(item.getDt() * 1000));
                        if (itemDay.equals(currentDay)) {
                            dayForecasts.add(item);
                        }
                    }

                    // Temperatura minima si maxima pentru ziua curenta
                    if (!dayForecasts.isEmpty()) {
                        double minTemp = Double.MAX_VALUE;
                        double maxTemp = Double.MIN_VALUE;
                        ForecastResponse.ForecastItem representativeItem = dayForecasts.get(0);

                        for (ForecastResponse.ForecastItem item : dayForecasts) {
                            if (item.getMain().getTemp() < minTemp) {
                                minTemp = item.getMain().getTemp();
                            }
                            if (item.getMain().getTemp() > maxTemp) {
                                maxTemp = item.getMain().getTemp();
                            }
                        }
                        representativeItem.getMain().setTempMin(minTemp);
                        representativeItem.getMain().setTempMax(maxTemp);
                        dailyForecasts.add(representativeItem);
                    }

                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                adapter.updateData(dailyForecasts);
            }
        });


        // Tratare erori
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });

        // Obtinere prognoza
        String city = getIntent().getStringExtra("city");
        if (city != null) {
            viewModel.fetchFiveDayForecast(city);
        } else {
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
    private String getDayString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

}