package com.nhom5.healthtracking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;
import java.util.List;

@Dao
public interface BloodPressureRecordDao {
  @Insert
  void insert(BloodPressureRecord bloodPressureRecord);

  @Query("SELECT * FROM blood_pressure_records WHERE user_id = :userId ORDER BY measured_at DESC")
  List<BloodPressureRecord> getAllByUserId(String userId);

  @Query("SELECT * FROM blood_pressure_records WHERE user_id = :userId AND measured_at >= :startTime AND measured_at <= :endTime ORDER BY measured_at DESC")
  List<BloodPressureRecord> getRecordsByUserIdAndDateRange(String userId, long startTime, long endTime);

  @Query("SELECT * FROM blood_pressure_records WHERE user_id = :userId ORDER BY measured_at DESC LIMIT :limit")
  List<BloodPressureRecord> getLatestRecordsByUserId(String userId, int limit);

  @Delete
  void delete(BloodPressureRecord bloodPressureRecord);
}
