package com.nhom5.healthtracking.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.dao.WaterIntakeDao;
import com.nhom5.healthtracking.data.local.entity.WaterIntake;
import com.nhom5.healthtracking.util.DateUtils;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class WaterIntakeRepository {
  private final WaterIntakeDao waterIntakeDao;
  private static final Executor IO = Executors.newSingleThreadExecutor();
  private final FirebaseFirestore fs = FirebaseModule.db();
  private final FirebaseAuth auth = FirebaseModule.auth();

  public WaterIntakeRepository(WaterIntakeDao waterIntakeDao) {
    this.waterIntakeDao = waterIntakeDao;
  }

  public void insert(String userId, int amountMl, Date intakeAt) {
    WaterIntake wi = new WaterIntake();
    wi.id = UUID.randomUUID().toString();
    wi.userId = userId;
    wi.amountMl = amountMl;
    wi.intakeAt = intakeAt;
    wi.createdAt = new Date();
    wi.updatedAt = new Date();
    wi.isSynced = false;
    
    IO.execute(() -> waterIntakeDao.insert(wi));
  }

  public List<WaterIntake> getAllByUserId(String userId) {
    return waterIntakeDao.getAllByUserId(userId);
  }

  public List<WaterIntake> getByUserIdAndDate(String userId, Date intakeDate) {
    return waterIntakeDao.getByUserIdAndDate(userId, intakeDate.getTime());
  }

  public Integer getTotalAmountByUserIdAndDate(String userId, Date intakeDate) {
    return waterIntakeDao.getTotalAmountByUserIdAndDate(userId, intakeDate.getTime());
  }

  /**
   * Lấy tất cả records uống nước trong cùng một ngày
   * @param userId ID của user
   * @param date Ngày cần lấy (chỉ lấy phần ngày, bỏ qua giờ phút giây)
   * @return Danh sách các records uống nước trong ngày đó
   */
  public List<WaterIntake> getRecordsByUserIdAndDay(String userId, Date date) {
    Date startOfDay = DateUtils.getStartOfDay(date);
    Date endOfDay = DateUtils.getEndOfDay(date);
    
    return waterIntakeDao.getRecordsByUserIdAndDateRange(userId, startOfDay.getTime(), endOfDay.getTime());
  }

  /**
   * Tính tổng lượng nước uống trong một ngày cụ thể
   * @param userId ID của user
   * @param date Ngày cần tính (chỉ lấy phần ngày, bỏ qua giờ phút giây)
   * @return Tổng lượng nước uống trong ngày (ml)
   */
  public Integer getTotalAmountByUserIdAndDay(String userId, Date date) {
    Date startOfDay = DateUtils.getStartOfDay(date);
    Date endOfDay = DateUtils.getEndOfDay(date);
    
    return waterIntakeDao.getTotalAmountByUserIdAndDateRange(userId, startOfDay.getTime(), endOfDay.getTime());
  }
}
