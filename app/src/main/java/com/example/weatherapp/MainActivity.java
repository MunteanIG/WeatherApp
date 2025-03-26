package com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private EditText etSearch;
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private Button btnForecast, btnSettings;



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