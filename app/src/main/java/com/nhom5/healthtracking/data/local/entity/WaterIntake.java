package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "water_intake")
public class WaterIntake {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "amount_ml")
    public int amountMl;

    @ColumnInfo(name = "intake_at")
    public Date intakeAt;

    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @NonNull
    public String getId() { return id; }

    public String getUserId() { return userId; }

    public int getAmountMl() { return amountMl; }

    public Date getIntakeAt() { return intakeAt; }

    public Date getCreatedAt() { return createdAt; }

    public Date getUpdatedAt() { return updatedAt; }

    public boolean isSynced() { return isSynced; }
}
