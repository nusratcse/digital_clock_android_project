package com.project.digitalclock;

public class Birthday {
    private final String name;
    private final String date; // Ensure this is a String since you're calling setText()

    public Birthday(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }
}
