package com.nhom5.healthtracking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import java.util.List;

@Dao
public interface WeightRecordDao {
  @Insert
  void insert(WeightRecord weightRecord);

  @Query("SELECT * FROM weight_records WHERE user_id = :userId ORDER BY recorded_at DESC")
  List<WeightRecord> getAllByUserId(String userId);
  
  @Query("SELECT * FROM weight_records WHERE user_id = :userId ORDER BY recorded_at DESC LIMIT 1")
  WeightRecord getLatestByUserId(String userId);
}