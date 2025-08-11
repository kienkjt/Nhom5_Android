package com.nhom5.healthtracking.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import com.nhom5.healthtracking.data.local.entity.WaterIntake;
import java.util.List;

@Dao
public interface WaterIntakeDao {
  @Insert
  void insert(WaterIntake waterIntake);

  @Query("SELECT * FROM water_intake WHERE user_id = :userId ORDER BY intake_at DESC")
  List<WaterIntake> getAllByUserId(String userId);

  @Query("SELECT * FROM water_intake WHERE user_id = :userId AND intake_at = :intakeAt ORDER BY intake_at DESC")
  List<WaterIntake> getByUserIdAndDate(String userId, long intakeAt);

  @Query("SELECT SUM(amount_ml) FROM water_intake WHERE user_id = :userId AND intake_at = :intakeAt")
  Integer getTotalAmountByUserIdAndDate(String userId, long intakeAt);

  @Query("SELECT * FROM water_intake WHERE user_id = :userId AND intake_at >= :startTime AND intake_at <= :endTime ORDER BY intake_at DESC")
  List<WaterIntake> getRecordsByUserIdAndDateRange(String userId, long startTime, long endTime);

  @Query("SELECT SUM(amount_ml) FROM water_intake WHERE user_id = :userId AND intake_at >= :startTime AND intake_at <= :endTime")
  Integer getTotalAmountByUserIdAndDateRange(String userId, long startTime, long endTime);

  @Delete
  void delete(WaterIntake waterIntake);
}
