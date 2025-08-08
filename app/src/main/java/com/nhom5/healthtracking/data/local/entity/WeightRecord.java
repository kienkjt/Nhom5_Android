package com.nhom5.healthtracking.data.local.entity;

import java.util.Date;

public class WeightRecord {
    private String id;
    private String userId;
    private float weight;
    private Date recordedDate;
    private String notes;

    public WeightRecord(String id, String userId, float weight, Date recordedDate, String notes) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.recordedDate = recordedDate;
        this.notes = notes;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public float getWeight() { return weight; }
    public Date getRecordedDate() { return recordedDate; }
    public String getNotes() { return notes; }
}
