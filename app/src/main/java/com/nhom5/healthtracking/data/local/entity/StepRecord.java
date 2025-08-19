package com.nhom5.healthtracking.data.local.entity;

public class StepRecord {
    private String userId;
    private String date;
    private int stepCount;
    private double distance;
    private double calories;

    public StepRecord(String userId, String date, int stepCount, double distance, double calories) {
        this.userId = userId;
        this.date = date;
        this.stepCount = stepCount;
        this.distance = distance;
        this.calories = calories;
    }

    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public int getStepCount() { return stepCount; }
    public double getDistance() { return distance; }
    public double getCalories() { return calories; }
}
