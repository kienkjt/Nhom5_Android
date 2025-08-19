package com.nhom5.healthtracking.util;

import android.content.Context;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.nhom5.healthtracking.data.local.dao.WeightRecordDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHelper {
    private final AppDatabase appDatabase;
    private final WeightRecordDao weightRecordDao;

    public DatabaseHelper(Context context) {
        appDatabase = AppDatabase.getDatabase(context);
        weightRecordDao = appDatabase.weightRecordDao();
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public void addWeight(String userId, double weight, String notes) {
        WeightRecord record = new WeightRecord();
        record.id = UUID.randomUUID().toString();
        record.userId = userId;
        record.weight = (float) weight;
        record.recordedAt = new Date();
        record.notes = notes;
        record.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        record.updatedAt = record.createdAt;
        record.isSynced = false;

        // Chèn vào Room
        weightRecordDao.insert(record);
    }

    public List<WeightRecord> getAllWeights(String userId) {
        return weightRecordDao.getAllByUserId(userId);
    }
}