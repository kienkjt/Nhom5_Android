package com.example.myhealth.models;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "health_records")
public class HealthRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date date;
    private float weight;
    private int systolicPressure;
    private int diastolicPressure;
    private int heartRate;
    private int waterIntake;
    private float sleepHours;
    private String notes;

    // Constructors
    public HealthRecord() {}

    public HealthRecord(Date date, float weight, int systolicPressure, int diastolicPressure,
                        int heartRate, int waterIntake, float sleepHours, String notes) {
        this.date = date;
        this.weight = weight;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.heartRate = heartRate;
        this.waterIntake = waterIntake;
        this.sleepHours = sleepHours;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public int getSystolicPressure() { return systolicPressure; }
    public void setSystolicPressure(int systolicPressure) { this.systolicPressure = systolicPressure; }

    public int getDiastolicPressure() { return diastolicPressure; }
    public void setDiastolicPressure(int diastolicPressure) { this.diastolicPressure = diastolicPressure; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getWaterIntake() { return waterIntake; }
    public void setWaterIntake(int waterIntake) { this.waterIntake = waterIntake; }

    public float getSleepHours() { return sleepHours; }
    public void setSleepHours(float sleepHours) { this.sleepHours = sleepHours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}