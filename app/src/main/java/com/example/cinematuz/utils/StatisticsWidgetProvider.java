package com.example.cinematuz.utils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.cinematuz.R;

/**
 * Provider dla widgetu statystyk na ekranie głównym urządzenia.
 * Obsługuje aktualizację liczników obejrzanych filmów i seriali pobieranych z SharedPreferences.
 */
public class StatisticsWidgetProvider extends AppWidgetProvider {

    /**
     * Odbiera broadcasty systemowe i aplikacyjne. 
     * Reaguje na ACTION_APPWIDGET_UPDATE wysyłany po zmianie danych w aplikacji.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // Nasłuchujemy na broadcast wysłany z MainActivity
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, StatisticsWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, allWidgetIds);
        }
    }

    /**
     * Aktualizuje widok konkretnej instancji widgetu.
     * Pobiera najnowsze statystyki z SharedPreferences i ustawia je w polach tekstowych widgetu.
     * 
     * @param context Kontekst aplikacji.
     * @param appWidgetManager Menedżer widgetów.
     * @param appWidgetId Identyfikator instancji widgetu.
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("CinematUZ_Stats", Context.MODE_PRIVATE);
        int moviesCount = prefs.getInt("movies_count", 0);
        int seriesCount = prefs.getInt("tv_shows_count", 0);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_statistics);

        views.setTextViewText(R.id.tvWidgetMovies, String.valueOf(moviesCount));
        views.setTextViewText(R.id.tvWidgetSeries, String.valueOf(seriesCount));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Wywoływane cyklicznie przez system w celu odświeżenia widgetów.
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
}