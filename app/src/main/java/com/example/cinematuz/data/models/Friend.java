package com.example.cinematuz.data.models;

public class Friend {
    private String name;
    private boolean isOnline;

    public Friend(String name, boolean isOnline) {
        this.name = name;
        this.isOnline = isOnline;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return isOnline;
    }
}