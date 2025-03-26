package com.example.weatherapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText etSearch;
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private Button btnForecast, btnSettings;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private WeatherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inițializare componente UI
        initViews();

        // Inițializare ViewModel
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

        // Inițializare serviciu locație
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setare data curentă
        setCurrentDate();

        // Observare schimbări în datele de vreme
        setupWeatherObservers();

        // Setare listener butoane
        setupButtonListeners();

        // Încărcare ultim oraș sau locație curentă
        loadInitialData();
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        Button btnSearch = findViewById(R.id.btnSearch);
        tvCity = findViewById(R.id.tvCity);
        tvDate = findViewById(R.id.tvDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        btnForecast = findViewById(R.id.btnForecast);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    private void setupWeatherObservers() {
        viewModel.getWeatherData().observe(this, weatherResponse -> {
            if (weatherResponse != null) {
                updateUI(weatherResponse);
                saveLastCity(weatherResponse.getName());
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupButtonListeners() {
        // Buton căutare
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            String city = etSearch.getText().toString().trim();
            if (!city.isEmpty()) {
                viewModel.fetchWeatherData(city);
            } else {
                Toast.makeText(this, "Introdu un oraș!", Toast.LENGTH_SHORT).show();
            }
        });

        // Căutare din tastatură
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String city = etSearch.getText().toString().trim();
                if (!city.isEmpty()) {
                    viewModel.fetchWeatherData(city);
                }
                return true;
            }
            return false;
        });

        // Buton prognoză
        btnForecast.setOnClickListener(v -> {
            startActivity(new Intent(this, ForecastActivity.class));
        });

        // Buton setări
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });
    }

    private void loadInitialData() {
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                loadDefaultCity();
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // 1. Încerci să obții locația curentă
                            viewModel.fetchWeatherByLocation(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                        } else {
                            // 2. Dacă locația curentă nu e disponibilă, încerci ultimul oraș salvat
                            tryFallbackToSavedCity();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 2. Dacă apare eroare, încerci ultimul oraș salvat
                        tryFallbackToSavedCity();
                    });
        } else {
            // 2. Dacă nu ai permisiune, încerci ultimul oraș salvat
            tryFallbackToSavedCity();
        }
    }

    private void tryFallbackToSavedCity() {
        SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
        String lastCity = prefs.getString("lastCity", null);

        if (lastCity != null && !lastCity.isEmpty()) {
            // 2a. Folosești ultimul oraș salvat
            viewModel.fetchWeatherData(lastCity);
        } else {
            // 3. Dacă nici oraș salvat nu există, folosești București
            viewModel.fetchWeatherData("București");
        }
    }

    private void loadDefaultCity() {
        viewModel.fetchWeatherData("București");
    }

    private void saveLastCity(String city) {
        // Salvezi doar dacă orașul a fost căutat manual
        if (!city.equals("București") && !city.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
            prefs.edit().putString("lastCity", city).apply();
        }
    }

    private void updateUI(WeatherResponse data) {
        tvCity.setText(data.getName());
        tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", data.getMain().getTemp()));
        tvDescription.setText(data.getWeather()[0].getDescription());
        tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", data.getMain().getHumidity()));
        tvWind.setText(String.format(Locale.getDefault(), "%.1f km/h", data.getWind().getSpeed() * 3.6));
        etSearch.setText("");

        String iconCode = data.getWeather()[0].getIcon();
        int iconResId = getResources().getIdentifier(
                "ic_" + iconCode,
                "drawable",
                getPackageName()
        );

        ivWeatherIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_unknown);
    }
}