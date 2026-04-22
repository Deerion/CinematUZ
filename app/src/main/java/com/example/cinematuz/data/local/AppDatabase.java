package com.example.cinematuz.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MovieEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // Singleton
    private static volatile AppDatabase instance;

    // Udostępnia interfejs operacji DAO
    public abstract MovieDao movieDao();

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
