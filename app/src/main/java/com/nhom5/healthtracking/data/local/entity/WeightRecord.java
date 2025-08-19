package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "weight_records")
public class WeightRecord {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "weight")
    public double weight;

    @ColumnInfo(name = "recorded_at")
    public Date recordedAt;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "created_at")
    public String createdAt;

    @ColumnInfo(name = "updated_at")
    public String updatedAt;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    public WeightRecord() {
    }

    public WeightRecord(String id, String userId, double weight, Date recordedAt, String notes) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.recordedAt = recordedAt;
        this.notes = notes;
    }

    @NonNull
    public String getId() { return id; }

    public String getUserId() { return userId; }

    public double getWeight() { return weight; }

    public Date getRecordedAt() { return recordedAt; }

    public String getNotes() { return notes; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }

    public boolean isSynced() { return isSynced; }
}