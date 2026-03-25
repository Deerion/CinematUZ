package com.example.cinematuz;

import android.app.Application;
import com.example.cinematuz.utils.ThemeHelper;

public class CinematUZApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applyTheme(this);
    }
}