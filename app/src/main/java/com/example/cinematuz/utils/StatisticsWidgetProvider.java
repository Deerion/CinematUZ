package com.example.cinematuz.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.cinematuz.R;

public class StatisticsWidgetProvider extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Odczytujemy zapisane wcześniej dane z Firebase (domyślnie 0)
        SharedPreferences prefs = context.getSharedPreferences("CinematUZ_Stats", Context.MODE_PRIVATE);
        int moviesCount = prefs.getInt("movies_count", 0);
        int seriesCount = prefs.getInt("tv_shows_count", 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_statistics);

        // Ustawiamy odczytane liczby w widoku
        views.setTextViewText(R.id.tvWidgetMovies, String.valueOf(moviesCount));
        views.setTextViewText(R.id.tvWidgetSeries, String.valueOf(seriesCount));

        // Zlecamy odświeżenie ekranu głównego
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}