package com.example.weatherapp;

import android.Manifest;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    /* Elemente UI */

    private EditText etSearch;
    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private Button btnForecast, btnAddFavorite,  btnFavorites;

    /* Serviciu de locatie si vreme */

    private FusedLocationProviderClient fusedLocationClient; 
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private WeatherViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initializari */

        initViews(); // Initializare UI
        viewModel = new ViewModelProvider(this).get(WeatherViewModel.class); // Initializare ViewModel 
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this); // Initializare serviciu locatie
        setCurrentDate(); // Setare data
        setupWeatherObservers(); // Schimbari de vreme 
        setupButtonListeners(); // Listneri butoane
        loadInitialData(); // Incarcare date initiale 
    }

    /* Inițializare componente UI */

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        tvCity = findViewById(R.id.tvCity);
        tvDate = findViewById(R.id.tvDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        btnForecast = findViewById(R.id.btnForecast);
        btnAddFavorite = findViewById(R.id.btnAddFavorite);
        btnFavorites = findViewById(R.id.btnFavorites);
    }

    /* Afisare data curenta in format dd MM yyyy */

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    /* Config observatori vreme */

    private void setupWeatherObservers() {

        // Observer modificari meteo
        viewModel.getWeatherData().observe(this, weatherResponse -> {
            if (weatherResponse != null) {
                updateUI(weatherResponse); // Actualizarea UI
                saveLastCity(weatherResponse.getName()); // Salvare oras pt. utilizari viitoare
            }
        });

        // Tratare erori
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    /* Config listeneri butoane */

    private void setupButtonListeners() {

        /* Buton Cautare */
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            String city = etSearch.getText().toString().trim();
            if (!city.isEmpty()) {
                viewModel.fetchWeatherData(city);
            } else {
                Toast.makeText(this, "Introdu un oraș!", Toast.LENGTH_SHORT).show();
            }
        });


        /* Cautare din camp */
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

        /* Buton Prognoza */
        btnForecast.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForecastActivity.class);
            if (tvCity.getText() != null && !tvCity.getText().toString().isEmpty()) {
                intent.putExtra("city", tvCity.getText().toString());
            }
            startActivity(intent);
        });

        /* Buton Adaugare Favorite */
        btnAddFavorite.setOnClickListener(v -> {
            if (tvCity.getText() != null && !tvCity.getText().toString().isEmpty()) {
                String cityName = tvCity.getText().toString();
                saveFavoriteCity(cityName);
                Toast.makeText(this, cityName + " a fost adăugat la favorite", Toast.LENGTH_SHORT).show();
            }
        });

        /* Buton Favorite */
        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(this, FavoritesActivity.class));
        });
    }

    /* Incarcare date initiale */

    private void loadInitialData() {
        checkLocationPermission();
    }

    /* Incarcare date initiale in functie de Permisiuni */    

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

    /* Metoda apelata dupa raspuns permisiune locatie */

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

    /* Obtinere ultima locatie */

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Locatie curenta prin permisiune
                            viewModel.fetchWeatherByLocation(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );
                        } else {
                            // Locatie negasita, afisare ultimul oras salvat
                            tryFallbackToSavedCity();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Ultimul oras salvat
                        tryFallbackToSavedCity();
                    });
        } else {
            // 2. Dacă nu ai permisiune, încerci ultimul oraș salvat
            tryFallbackToSavedCity();
        }
    }

    /* Metoda locatie neobtinuta */

    private void tryFallbackToSavedCity() {
        SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
        String lastCity = prefs.getString("lastCity", null);

        if (lastCity != null && !lastCity.isEmpty()) {
            viewModel.fetchWeatherData(lastCity); // Foloseste ultimul oras salvat
        } else {
            viewModel.fetchWeatherData("București"); // Foloseste orasul implicit
        }
    }

    /* Incarcare oras implicit */

    private void loadDefaultCity() {
        viewModel.fetchWeatherData("București");
    }

    /* Salvarea ultimului oras cautat */

    private void saveLastCity(String city) {
        if (!city.equals("București") && !city.isEmpty()) {
            SharedPreferences prefs = getSharedPreferences("weatherPrefs", MODE_PRIVATE);
            prefs.edit().putString("lastCity", city).apply();
        }
    }

    /* Actualizare UI cu datele meteo primite */

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

        ivWeatherIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_unknown); // Setare inconita ? daca nu e gasita
    }

    /* Salvare oras in favorite */

    private void saveFavoriteCity(String cityName) {
        SharedPreferences prefs = getSharedPreferences("WeatherPrefs", MODE_PRIVATE);
        Set<String> existingFavorites = prefs.getStringSet("favoriteCities", new HashSet<>());

        Set<String> updatedFavorites = new HashSet<>(existingFavorites);
        updatedFavorites.add(cityName);

        prefs.edit().putStringSet("favoriteCities", updatedFavorites).apply();
    }
}