package com.project.digitalclock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder> {
    private final List<Birthday> birthdayList;

    public BirthdayAdapter(List<Birthday> birthdayList) {
        this.birthdayList = birthdayList;
    }

    @NonNull
    @Override
    public BirthdayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.birthday_item, parent, false);
        return new BirthdayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BirthdayViewHolder holder, int position) {
        Birthday birthday = birthdayList.get(position);
        holder.nameTextView.setText(birthday.getName());
        holder.dateTextView.setText(birthday.getDate());
    }

    @Override
    public int getItemCount() {
        return birthdayList.size();
    }

    public static class BirthdayViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, dateTextView;

        public BirthdayViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}
