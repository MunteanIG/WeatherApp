package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements RecentCitiesAdapter.OnCityClickListener {
    private EditText etSearch;
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private Button btnForecast, btnSettings;

    private RecyclerView rvRecentCities;
    private RecentCitiesAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // RecyclerView pentru orașe recente
        rvRecentCities = findViewById(R.id.rvRecentCities);
        rvRecentCities.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecentCitiesAdapter(new ArrayList<>(), this);
        rvRecentCities.setAdapter(adapter);

        // Afișează orașe recente la focus pe câmpul de căutare
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) loadRecentCities();
        });

        // Căutare la click pe buton
        btnSearch.setOnClickListener(v -> {
            String city = etSearch.getText().toString().trim();
            if (!city.isEmpty()) {
                fetchWeatherData(city);  // Încarcă vremea pentru orașul introdus
                rvRecentCities.setVisibility(View.GONE);  // Ascunde lista de orașe recente
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
                return true;
            }
            return false;
        });

        // Butoane navigare
        btnForecast.setOnClickListener(v -> startActivity(new Intent(this, ForecastActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // Încarcă date inițiale
        fetchWeatherData("București");
    }

    private void loadRecentCities() {
        Set<String> cities = PrefsHelper.getRecentCities(this);
        Log.d("CITIES", "Orașe salvate: " + cities); // Debugging

        if (!cities.isEmpty()) {
            adapter = new RecentCitiesAdapter(new ArrayList<>(cities), this);
            rvRecentCities.setAdapter(adapter);
            rvRecentCities.setVisibility(View.VISIBLE);
        } else {
            rvRecentCities.setVisibility(View.GONE);
        }
    }
    @Override
    public void onCityDeleted(String city) {
        // Șterge orașul din SharedPreferences
        PrefsHelper.removeRecentCity(this, city);
        Toast.makeText(this, "Oraș șters: " + city, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCityClick(String city) {
        etSearch.setText(city);
        fetchWeatherData(city);
        rvRecentCities.setVisibility(View.GONE);
    }

    private void fetchWeatherData(String city) {
        PrefsHelper.saveRecentCity(this, city);

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
                    PrefsHelper.saveRecentCity(MainActivity.this, city); // Salvează doar după succes
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
        tvHumidity.setText(data.getMain().getHumidity() + "%");
        tvWind.setText("10 km/h"); // Poți adăuga și vântul din răspunsul API

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

}