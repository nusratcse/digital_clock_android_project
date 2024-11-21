package com.project.digitalclock;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import android.widget.Chronometer;
import android.os.SystemClock;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.Manifest;


public class MainActivity extends AppCompatActivity {
    private Spinner countrySpinner;
    private String[] timezones;
    private TextView timeTextView, dateTextView, selectedTimeTextView;
    private String currentTimezone;
    private final Handler handler = new Handler();
    private boolean isTimerRunning = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Dialog timerDialog;
    private Chronometer chronometer;

    SwitchCompat switchCompact;

    boolean isNightMode;


    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the home page layout immediately
        setContentView(R.layout.home_page);

        // Set up button click listener for the start button
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(v -> showMainLayout());

        // Set the theme based on the shared preference
        checkNotificationPermission();

    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showMainLayout() {
        // Now load your main layout
        setContentView(R.layout.activity_main);

        // Initialize your views and start the timers
        timeTextView = findViewById(R.id.timeTextView);
        dateTextView = findViewById(R.id.dateTextView);
        countrySpinner = findViewById(R.id.countrySpinner);
        selectedTimeTextView = findViewById(R.id.selectedTimeTextView);
        switchCompact = findViewById(R.id.switchCompact);


        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isNightMode = sharedPreferences.getBoolean("nightMode",false);

        if(isNightMode){
            switchCompact.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switchCompact.setOnClickListener(v -> toggleDarkMode());
        // Set up the Spinner with data
        setupCountrySpinner();

        updateLocalTime();


        Button startTimerButton = findViewById(R.id.startTimerButton);
        Button showCalendarButton = findViewById(R.id.showCalendarButton);

        // Start updating time
        handler.postDelayed(updateRunnable, 1000);


        // Set button click listeners
        startTimerButton.setOnClickListener(v -> showTimerPopup());
        showCalendarButton.setOnClickListener(v -> showCalendar());


        // Button to go to the Birthday Page
        Button goToBirthdayPageButton = findViewById(R.id.goToBirthdayPageButton);
        goToBirthdayPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start BirthdayPageActivity
                Intent intent = new Intent(MainActivity.this, BirthdayPageActivity.class);
                startActivity(intent);
            }
        });

       Button goToAlarmPageButton = findViewById(R.id.goToAlarmPageButton);
        goToAlarmPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start alramPageActivity
                Intent intent = new Intent(MainActivity.this, AlarmPageActivity.class);
                startActivity(intent);
            }
        });

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("alarmChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void setupCountrySpinner() {
        String[] countries = {"USA", "UK", "Japan", "France", "Australia", "India", "Brazil", "Canada", "Germany", "China", "Mexico", "Russia", "South Africa", "Italy", "South Korea"};
        timezones = new String[]{"America/New_York", "Europe/London", "Asia/Tokyo", "Europe/Paris", "Australia/Sydney",
                "Asia/Kolkata", "America/Sao_Paulo", "America/Toronto", "Europe/Berlin", "Asia/Shanghai",
                "America/Mexico_City", "Europe/Moscow", "Africa/Johannesburg", "Europe/Rome", "Asia/Seoul"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(adapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTimezone = timezones[position];
                updateCountryTime(currentTimezone);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void updateLocalTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

        String currentTime = timeFormat.format(calendar.getTime());
        String currentDate = dateFormat.format(calendar.getTime());

        Log.d("Local Current Time", currentTime); // Log the local time

        // Update TextViews with current local time and date
        timeTextView.setText(currentTime);
        dateTextView.setText(currentDate);
    }

    private void updateCountryTime(String timezone) {
        // Create new SimpleDateFormat objects for each call
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault());

        // Set the timezone for the SimpleDateFormat objects
        timeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(timezone));

        String selectedTime = timeFormat.format(calendar.getTime());

        Log.d("Selected Country Time", selectedTime); // Log the selected country time

        // Update TextViews with selected country's time and date
        selectedTimeTextView.setText("Selected Time: " + selectedTime);
    }



    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the local time every second
            updateLocalTime();
            // Update the time for the currently selected country
            if (currentTimezone != null) {
                updateCountryTime(currentTimezone);
            }
            handler.postDelayed(this, 1000); // Update every second
        }
    };
    // Function to display the timer popup
    private void showTimerPopup() {
        if (!isTimerRunning) {
            // Create a dialog for the timer popup
            timerDialog = new Dialog(this);
            timerDialog.setContentView(R.layout.timer_popup);
            timerDialog.setCancelable(false);

            // Set dialog width and height to make it larger
            Objects.requireNonNull(timerDialog.getWindow()).setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),  // 90% of screen width
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.4)  // 20% of screen height
            );

            // Find views in the popup layout
            chronometer = timerDialog.findViewById(R.id.timerTextView);
            Button stopTimerButton = timerDialog.findViewById(R.id.stopTimerButton);
            Button pauseTimerButton = timerDialog.findViewById(R.id.pauseTimerButton);

            // Start the chronometer (timer)
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            isTimerRunning = true;

            // Stop timer button click listener
            stopTimerButton.setOnClickListener(v -> stopTimer());
            pauseTimerButton.setOnClickListener(v -> pauseTimer());

            // Show the dialog
            timerDialog.show();
        } else {
            Toast.makeText(this, "Timer is already running.", Toast.LENGTH_SHORT).show();
        }
    }

    // Function to pause the timer
    private boolean isPaused = false;
    private long timeWhenPaused = 0;

    private void pauseTimer() {
        if (!isPaused) {
            // Pause the timer and record the time it was paused at
            timeWhenPaused = chronometer.getBase() - SystemClock.elapsedRealtime();
            chronometer.stop();
            isPaused = true;
            Toast.makeText(this, "Timer paused!", Toast.LENGTH_SHORT).show();
        } else {
            // Resume the timer from the paused time
            chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            chronometer.start();
            isPaused = false;
            Toast.makeText(this, "Timer resumed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopTimer() {
        if (isTimerRunning) {
            // Stop the chronometer and close the dialog
            chronometer.stop();
            isTimerRunning = false;
            timerDialog.dismiss();
            Toast.makeText(this, "Timer stopped!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCalendar() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.calendar_popup);
        CalendarView calendarView = dialog.findViewById(R.id.calendarView);
        Button closeCalendarButton = dialog.findViewById(R.id.closeCalendarButton);
        dialog.show();
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            Toast.makeText(MainActivity.this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
        });

        closeCalendarButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void toggleDarkMode() {
       if(isNightMode){
           AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
           editor.putBoolean("nightMode",false);
       }else{
           AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
           editor.putBoolean("nightMode",true);
       }

       editor.apply();
    }

    private void updateUI() {
        // Update your UI elements here if necessary based on the current theme
        // For example, you might want to change text colors, backgrounds, etc.
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the theme based on shared preference
        updateUI(); // Optional: Update UI elements if necessary
    }
}
