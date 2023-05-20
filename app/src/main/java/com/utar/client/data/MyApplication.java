package com.utar.client.data;

import android.app.Application;


import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static MyApplication myApplication;

    //for screen size
    private int displayHeight, displayWidth;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        displayHeight = getResources().getDisplayMetrics().heightPixels;
        displayWidth = getResources().getDisplayMetrics().widthPixels;
    }

    public static MyApplication getInstance(){
        return myApplication;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }
}