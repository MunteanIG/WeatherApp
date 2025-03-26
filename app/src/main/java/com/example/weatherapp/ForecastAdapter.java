package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {
    private List<ForecastResponse.ForecastItem> forecastItems;
    private Context context;

    public ForecastAdapter(List<ForecastResponse.ForecastItem> forecastItems, Context context) {
        this.forecastItems = forecastItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = forecastItems.get(position);

        // Formatare data
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault());
        String date = sdf.format(new Date(item.getDt() * 1000));
        holder.tvDate.setText(date);

        // Temperatura
        String temp = String.format(Locale.getDefault(), "%.1f°C", item.getMain().getTemp());
        holder.tvTemp.setText(temp);

        // Descriere și iconiță
        if (!item.getWeather().isEmpty()) {
            WeatherResponse.Weather weather = item.getWeather().get(0);
            holder.tvDescription.setText(weather.getDescription());

            String iconCode = weather.getIcon();
            int iconResId = context.getResources().getIdentifier(
                    "ic_" + iconCode,
                    "drawable",
                    context.getPackageName()
            );
            holder.ivIcon.setImageResource(iconResId != 0 ? iconResId : R.drawable.ic_unknown);
        }
    }

    @Override
    public int getItemCount() {
        return forecastItems.size();
    }

    public void updateData(List<ForecastResponse.ForecastItem> newItems) {
        forecastItems = newItems;
        notifyDataSetChanged();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp, tvDescription;
        ImageView ivIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}