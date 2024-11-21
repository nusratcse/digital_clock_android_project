package com.project.digitalclock;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BirthdayPageActivity extends AppCompatActivity {

    private RecyclerView birthdayRecyclerView;
    private BirthdayAdapter birthdayAdapter;
    private List<Birthday> birthdayList;
    private Button addBirthdayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.birthday_page);

        createNotificationChannel();

        birthdayRecyclerView = findViewById(R.id.birthdayRecyclerView);
        addBirthdayButton = findViewById(R.id.addBirthdayButton);

        birthdayList = new ArrayList<>();
        birthdayAdapter = new BirthdayAdapter(birthdayList);

        // Set up RecyclerView
        birthdayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        birthdayRecyclerView.setAdapter(birthdayAdapter);

        // Add Birthday button click listener
        addBirthdayButton.setOnClickListener(v -> openAddBirthdayPopup());
    }

    private void openAddBirthdayPopup() {
        AddBirthdayDialog dialog = new AddBirthdayDialog(this, (name, date) -> {
            birthdayList.add(new Birthday(name, date));
            birthdayAdapter.notifyDataSetChanged();

            // Schedule a notification for the added birthday
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar birthdayCalendar = Calendar.getInstance();
                birthdayCalendar.setTime(dateFormat.parse(date));

                // Schedule the notification
                scheduleNotification(name, birthdayCalendar);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        dialog.show();
    }

    private void scheduleNotification(String name, Calendar birthdayCalendar) {
        Calendar today = Calendar.getInstance();

        // Ensure the birthday notification is scheduled for the future
        birthdayCalendar.set(Calendar.YEAR, today.get(Calendar.YEAR));
        if (birthdayCalendar.before(today)) {
            birthdayCalendar.add(Calendar.YEAR, 1);
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("name", name);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) birthdayCalendar.getTimeInMillis(), // Unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Updated flags
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    birthdayCalendar.getTimeInMillis(),
                    pendingIntent
            );
            Log.d("BirthdayPageActivity", "Notification scheduled for: " + birthdayCalendar.getTime());
        } else {
            Log.e("BirthdayPageActivity", "AlarmManager is null");
        }
    }



    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Birthday Reminder Channel";
            String description = "Channel for birthday reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("birthday_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            } else {
                Log.e("BirthdayPageActivity", "NotificationManager is null");
            }
        }
    }



}
