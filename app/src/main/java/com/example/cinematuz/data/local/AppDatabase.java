package com.example.cinematuz.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Główna klasa bazy danych Room dla aplikacji CinematUZ.
 * Zarządza instancją bazy danych i zapewnia dostęp do obiektów DAO.
 */
@Database(entities = {MovieEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Instancja bazy danych (Singleton).
     */
    private static volatile AppDatabase instance;

    /**
     * Udostępnia dostęp do DAO dla operacji na filmach.
     * 
     * @return Obiekt MovieDao.
     */
    public abstract MovieDao movieDao();

    /**
     * Zwraca instancję bazy danych. Tworzy nową instancję, jeśli jeszcze nie istnieje.
     * 
     * @param context Kontekst aplikacji używany do stworzenia bazy danych.
     * @return Instancja AppDatabase.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "cinematuz_database" // Nazwa pliku bazy na urządzeniu
                            )
                            .fallbackToDestructiveMigration() // Resetuje bazę w przypadku zmiany wersji
                            .build();
                }
            }
        }
        return instance;
    }
}