package com.project.digitalclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name"); // Get the birthday name from the Intent

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "birthday_channel")
                .setSmallIcon(R.drawable.ic_notification) // Ensure this icon is available
                .setContentTitle("Birthday Reminder")
                .setContentText("It's " + name + "'s birthday today!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Dismiss the notification on click

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // Use unique ID for each notification
    }
}

