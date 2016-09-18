package com.facebook.quire;

import android.app.Application;
import android.content.Context;

import com.firebase.client.Firebase;

public class QuireApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

    }




}












