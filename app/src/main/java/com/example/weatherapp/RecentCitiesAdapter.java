package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecentCitiesAdapter extends RecyclerView.Adapter<RecentCitiesAdapter.ViewHolder> {
    private List<String> cities;
    private OnCityClickListener listener;


    public interface OnCityClickListener {
        void onCityClick(String city);  // Pentru selectare oraș
        void onCityDeleted(String city); // Pentru ștergere oraș
    }

    public RecentCitiesAdapter(List<String> cities, OnCityClickListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_city, parent, false); // Use your custom layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String city = cities.get(position);
        holder.tvCity.setText(city);

        // Click pe oraș -> încarcă vremea
        holder.itemView.setOnClickListener(v -> listener.onCityClick(city));

        // Click pe butonul de ștergere
        holder.ivDelete.setOnClickListener(v -> {
            listener.onCityDeleted(city); // Anunță Activity-ul
            cities.remove(position); // Elimină din listă
            notifyItemRemoved(position); // Actualizează RecyclerView
        });
    }

    @Override
    public int getItemCount() {
        return cities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tvCity);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }

}
