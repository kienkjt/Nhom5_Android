package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "sleep_records")
public class SleepRecord {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "started_at")
    public Date startedAt;

    @ColumnInfo(name = "ended_at")
    public Date endedAt;

    @ColumnInfo(name = "quality")
    public String quality;

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

    public Date getStartedAt() { return startedAt; }

    public Date getEndedAt() { return endedAt; }

    public String getQuality() { return quality; }

    public String getNotes() { return notes; }

    public Date getCreatedAt() { return createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public boolean isSynced() { return isSynced; }
}
