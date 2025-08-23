package com.nhom5.healthtracking.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.dao.WeightRecordDao;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

  public void insertAsync(String userId, double weight, String notes, Runnable onComplete) {
    IO.execute(() -> {
      WeightRecord wr = new WeightRecord();
      wr.id = UUID.randomUUID().toString();
      wr.userId = userId;
      wr.weight = weight;
      wr.notes = notes;
      wr.recordedAt = new Date();
      wr.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
      wr.updatedAt = wr.createdAt;
      wr.isSynced = false;
      
      weightRecordDao.insert(wr);
      
      if (onComplete != null) {
        onComplete.run();
      }
    });
  }

  public void getAllByUserIdAsync(String userId, WeightListCallback callback) {
    IO.execute(() -> {
      List<WeightRecord> records = weightRecordDao.getAllByUserId(userId);
      callback.onResult(records);
    });
  }

  public LiveData<List<WeightRecord>> getAllByUserIdLiveData(String userId) {
    MutableLiveData<List<WeightRecord>> liveData = new MutableLiveData<>();
    IO.execute(() -> {
      List<WeightRecord> records = weightRecordDao.getAllByUserId(userId);
      liveData.postValue(records);
    });
    return liveData;
  }

  public LiveData<WeightRecord> getLatestByUserIdLiveData(String userId) {
    MutableLiveData<WeightRecord> liveData = new MutableLiveData<>();
    IO.execute(() -> {
      WeightRecord record = weightRecordDao.getLatestByUserId(userId);
      liveData.postValue(record);
    });
    return liveData;
  }

  // Deprecated - for backward compatibility
  @Deprecated
  public void insert(String userId, double weight, String notes) {
    WeightRecord wr = new WeightRecord();
    wr.id = UUID.randomUUID().toString();
    wr.userId = userId;
    wr.weight = weight;
    wr.notes = notes;
    wr.recordedAt = new Date();
    wr.createdAt = String.valueOf(new Date());
    wr.updatedAt = String.valueOf(new Date());
    wr.isSynced = false;
    weightRecordDao.insert(wr);
  }

  // Deprecated - for backward compatibility
  @Deprecated
  public List<WeightRecord> getAllByUserId(String userId) {
    return weightRecordDao.getAllByUserId(userId);
  }

  public interface WeightListCallback {
    void onResult(List<WeightRecord> records);
  }
}
