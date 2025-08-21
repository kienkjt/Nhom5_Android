package com.nhom5.healthtracking.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.dao.WeightRecordDao;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WeightRecordRepository {
  private final WeightRecordDao weightRecordDao;
  private static final Executor IO = Executors.newSingleThreadExecutor();
  private final FirebaseFirestore fs = FirebaseModule.db();
  private final FirebaseAuth auth = FirebaseModule.auth();

  public WeightRecordRepository(WeightRecordDao weightRecordDao) {
    this.weightRecordDao = weightRecordDao;
  }

  public void insert(String userId, double weight, String notes) {
    WeightRecord wr = new WeightRecord();
    wr.id = UUID.randomUUID().toString();
    wr.userId = userId;
    wr.weight = weight;
    wr.notes = notes;
    wr.recordedAt = new Date();
    wr.createdAt = new Date();
    wr.updatedAt = new Date();
    wr.isSynced = false;
    weightRecordDao.insert(wr);
  }

  public List<WeightRecord> getAllByUserId(String userId) {
    return weightRecordDao.getAllByUserId(userId);
  }
}
