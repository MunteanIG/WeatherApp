package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;

public class MainActivity extends AppCompatActivity {
    private EditText etSearch;
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private Button btnForecast, btnSettings;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inițializare componente
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

        checkLocationPermission();

        // Încarcă ultimul oraș salvat (dacă există)
        SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
        String lastCity = prefs.getString("lastCity", "București"); // "București" e valoarea implicită

        // Încarcă vremea pentru ultimul oraș salvat
        fetchWeatherData(lastCity);

        // Căutare la click pe buton
        btnSearch.setOnClickListener(v -> {
            String city = etSearch.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherData(city);  // Încarcă vremea pentru orașul introdus
            } else {
                Toast.makeText(this, "Introdu un oraș!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setare dată curentă
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));

        // Căutare oraș
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String city = etSearch.getText().toString();
                if (!city.isEmpty()) {
                    fetchWeatherData(city);
                }
                etSearch.setText("");
                return true;
            }
            return false;
        });

        // Butoane navigare
        btnForecast.setOnClickListener(v -> startActivity(new Intent(this, ForecastActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

    }

    private void fetchWeatherData(String city) {

        SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastCity", city); // Cheia este "lastCity", valoarea este orașul introdus
        editor.apply(); // Salvarea este asincronă (folosește apply() în loc de commit() pentru performanță)

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getCurrentWeather(city, "b434ad86a65e25eb722d4e736180dd49");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    updateUI(response.body());
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Eroare: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void updateUI(WeatherResponse data) {
        tvCity.setText(data.getName());
        tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", data.getMain().getTemp()));
        tvDescription.setText(data.getWeather()[0].getDescription());
        tvHumidity.setText(data.getMain().getHumidity() + "% ");
        tvWind.setText(String.format("%.1f km/h", data.getWind().getSpeed())); // Poți adăuga și vântul din răspunsul API
        etSearch.setText("");

        // Obține codul iconiței de la API (ex: "01d")
        String iconCode = data.getWeather()[0].getIcon();

        // Obține ID-ul resursei din drawable (ex: ic_01d)
        int iconResId = getResources().getIdentifier(
                "ic_" + iconCode,  // Numele fișierului (fără extensie)
                "drawable",
                getPackageName()
        );

        // Setează iconița în ImageView
        if (iconResId != 0) {
            ivWeatherIcon.setImageResource(iconResId);
        } else {
            // Fallback dacă iconița nu există
            ivWeatherIcon.setImageResource(R.drawable.ic_unknown);
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Cere permisiunea dacă nu este acordată
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Dacă permisiunea este deja acordată, obține locația
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                // Dacă permisiunea este refuzată, folosește orașul salvat sau București
                loadSavedCity();
            }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Obține orașul din coordonatele GPS
                        getCityFromLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        // Dacă locația nu este disponibilă, folosește orașul salvat
                        loadSavedCity();
                    }
                });
    }

    private void getCityFromLocation(double latitude, double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);
        Call<WeatherResponse> call = service.getWeatherByCoordinates(latitude, longitude, "b434ad86a65e25eb722d4e736180dd49");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    String cityName = response.body().getName();
                    fetchWeatherData(cityName);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                loadSavedCity();
            }
        });
    }

    private void loadSavedCity() {
        SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
        String lastCity = prefs.getString("lastCity", "București");
        fetchWeatherData(lastCity);
    }

}