package com.nhom5.healthtracking.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.dao.SleepRecordDao;
import com.nhom5.healthtracking.data.local.entity.SleepRecord;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SleepRecordRepository {
  private final SleepRecordDao sleepRecordDao;
  private static final Executor IO = Executors.newSingleThreadExecutor();
  private final FirebaseFirestore fs = FirebaseModule.db();
  private final FirebaseAuth auth = FirebaseModule.auth();

  public SleepRecordRepository(SleepRecordDao sleepRecordDao) {
    this.sleepRecordDao = sleepRecordDao;
  }

  public void insert(String userId, Date startAt, Date endAt, String quality, String notes) {
    SleepRecord sr = new SleepRecord();
    sr.id = UUID.randomUUID().toString();
    sr.userId = userId;
    sr.startedAt = startAt;
    sr.endedAt = endAt;
    sr.quality = quality;
    sr.notes = notes;
    sr.createdAt = new Date();
    sr.updatedAt = new Date();
    sr.isSynced = false;
    
    IO.execute(() -> sleepRecordDao.insert(sr));
  }

  public List<SleepRecord> getAllByUserId(String userId) {
    return sleepRecordDao.getAllByUserId(userId);
  }

  public List<SleepRecord> getAllByUserIdAndQuality(String userId, String quality) {
    return sleepRecordDao.getAllByUserIdAndQuality(userId, quality);
  }
}
