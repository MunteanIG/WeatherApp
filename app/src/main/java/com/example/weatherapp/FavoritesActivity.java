package com.example.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private List<FavoriteCity> favoriteCities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize RecyclerView and Adapter FIRST
        recyclerView = findViewById(R.id.rvFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with empty data
        adapter = new FavoritesAdapter(favoriteCities, this);
        recyclerView.setAdapter(adapter);

        // THEN load the data
        loadFavoriteCities();
    }

    private void loadFavoriteCities() {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        Set<String> favoriteCityNames = prefs.getStringSet("favoriteCities", new HashSet<>());

        // Clear existing data
        favoriteCities.clear();

        if (favoriteCityNames.isEmpty()) {
            Toast.makeText(this, "Nu a fost adăugat niciun oraș la favorite", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            return;
        }

        // Show loading indicator
        Toast.makeText(this, "Se încarcă datele...", Toast.LENGTH_SHORT).show();

        // Create a counter to track completed requests
        final int[] completedRequests = {0};
        int totalCities = favoriteCityNames.size();

        for (String cityName : favoriteCityNames) {
            WeatherViewModel viewModel = new ViewModelProvider(this).get(WeatherViewModel.class);

            viewModel.fetchWeatherDataFavorite(cityName, new WeatherViewModel.WeatherCallback() {
                @Override
                public void onSuccess(WeatherResponse response) {
                    // Add the real data to the list
                    favoriteCities.add(new FavoriteCity(
                            response.getName(),
                            response.getMain().getTemp(),
                            response.getWeather()[0].getDescription(),
                            response.getWeather()[0].getIcon()
                    ));

                    completedRequests[0]++;

                    // Update UI when all requests are done
                    if (completedRequests[0] == totalCities) {
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(FavoritesActivity.this,
                                    "Datele au fost actualizate", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onFailure(String error) {
                    completedRequests[0]++;
                    runOnUiThread(() -> {
                        Toast.makeText(FavoritesActivity.this,
                                "Eroare la încărcarea datelor pentru " + cityName,
                                Toast.LENGTH_SHORT).show();

                        if (completedRequests[0] == totalCities) {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }
}