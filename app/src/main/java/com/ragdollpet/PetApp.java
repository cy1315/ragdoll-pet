package com.ragdollpet;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class PetApp extends Application {
    public static final String CHANNEL_ID = "ragdoll_pet";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel c = new NotificationChannel(
                CHANNEL_ID, "桌宠", NotificationManager.IMPORTANCE_LOW);
            c.setDescription("布偶猫前台服务");
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }
}