package com.nhom5.healthtracking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import com.nhom5.healthtracking.data.local.entity.SleepRecord;
import java.util.List;

@Dao
public interface SleepRecordDao {
  @Insert
  void insert(SleepRecord sleepRecord);

  @Query("SELECT * FROM sleep_records WHERE user_id = :userId ORDER BY started_at DESC")
  List<SleepRecord> getAllByUserId(String userId);

  @Query("SELECT * FROM sleep_records WHERE user_id = :userId AND quality = :quality ORDER BY started_at DESC")
  List<SleepRecord> getAllByUserIdAndQuality(String userId, String quality);

  @Delete
  void delete(SleepRecord sleepRecord);
}
