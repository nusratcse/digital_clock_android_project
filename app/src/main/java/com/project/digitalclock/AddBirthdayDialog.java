package com.project.digitalclock;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

public class AddBirthdayDialog extends Dialog {
    private EditText nameEditText;
    private DatePicker datePicker;
    private Button submitButton;
    private AddBirthdayListener listener;

    public AddBirthdayDialog(Context context, AddBirthdayListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_birthday_dialog);

        nameEditText = findViewById(R.id.nameEditText);
        datePicker = findViewById(R.id.datePicker);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String date = datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear();
            if (!name.isEmpty()) {
                listener.onBirthdayAdded(name, date);
                dismiss();
            } else {
                nameEditText.setError("Name is required");
            }
        });
    }

    public interface AddBirthdayListener {
        void onBirthdayAdded(String name, String date);
    }
}
