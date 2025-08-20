package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "blood_pressure_records")
public class BloodPressureRecord {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "systolic")
    public int systolic;

    @ColumnInfo(name = "diastolic")
    public int diastolic;

    @ColumnInfo(name = "pulse")
    public int pulse;

    @ColumnInfo(name = "measured_at")
    public Date measuredAt;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @NonNull
    public String getId() { return id; }

    public String getUserId() { return userId; }

    public int getSystolic() { return systolic; }

    public int getDiastolic() { return diastolic; }

    public int getPulse() { return pulse; }

    public Date getMeasuredAt() { return measuredAt; }

    public String getNotes() { return notes; }

    public Date getCreatedAt() { return createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public boolean isSynced() { return isSynced; }
}