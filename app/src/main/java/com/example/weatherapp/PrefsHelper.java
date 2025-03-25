package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class PrefsHelper {
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_RECENT_CITIES = "recent_cities";

    public static void saveRecentCity(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cities = getRecentCities(context); // Obține lista existentă
        cities.add(city); // Adaugă noul oraș
        prefs.edit().putStringSet(KEY_RECENT_CITIES, cities).apply(); // Salvează Set-ul actualizat
    }

    public static Set<String> getRecentCities(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_RECENT_CITIES, new HashSet<>());
    }

    public static void removeRecentCity(Context context, String city) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> cities = getRecentCities(context);
        cities.remove(city);
        prefs.edit().putStringSet(KEY_RECENT_CITIES, cities).apply();
    }
}
