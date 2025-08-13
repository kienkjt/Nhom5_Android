package com.nhom5.healthtracking.data.local.entity;

public class StepRecord {
    private int id;
    private int userId;
    private String date;
    private int stepCount;
    private int source; // 0 = Local, 1 = Google Fit
    private boolean isSynced;
    private String createdAt;
    private String updatedAt;

    public StepRecord(int userId, String date, int stepCount, int source) {
        this.userId = userId;
        this.date = date;
        this.stepCount = stepCount;
        this.source = source;
        this.isSynced = false;
        this.createdAt = String.valueOf(System.currentTimeMillis());
        this.updatedAt = this.createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
