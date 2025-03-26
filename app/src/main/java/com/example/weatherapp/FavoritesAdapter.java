package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {
    private List<FavoriteCity> favoriteCities;
    private Context context;

    public FavoritesAdapter(List<FavoriteCity> favoriteCities, Context context) {
        this.favoriteCities = favoriteCities;
        this.context = context;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteCity city = favoriteCities.get(position);

        holder.tvCityName.setText(city.getName());
        holder.tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", city.getTemperature()));

        // Capitalize first letter of description
        String description = city.getDescription();
        if (!description.isEmpty()) {
            description = description.substring(0, 1).toUpperCase() + description.substring(1);
        }
        holder.tvDescription.setText(description);

        // Load weather icon
        int iconResId = context.getResources().getIdentifier(
                "ic_" + city.getIconCode(),
                "drawable",
                context.getPackageName()
        );
        holder.ivWeatherIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_unknown);
    }

    @Override
    public int getItemCount() {
        return favoriteCities.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvTemperature, tvDescription;
        ImageView ivWeatherIcon;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvFavoriteCityName);
            tvTemperature = itemView.findViewById(R.id.tvFavoriteTemperature);
            tvDescription = itemView.findViewById(R.id.tvFavoriteDescription);
            ivWeatherIcon = itemView.findViewById(R.id.ivFavoriteWeatherIcon);
        }
    }
}