package com.nhom5.healthtracking.data.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.dao.BloodPressureRecordDao;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;
import com.nhom5.healthtracking.util.DateUtils;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BloodPressureRecordRepository {
  private final BloodPressureRecordDao bloodPressureRecordDao;
  private static final Executor IO = Executors.newSingleThreadExecutor();
  private final FirebaseFirestore fs = FirebaseModule.db();
  private final FirebaseAuth auth = FirebaseModule.auth();

  public BloodPressureRecordRepository(BloodPressureRecordDao bloodPressureRecordDao) {
    this.bloodPressureRecordDao = bloodPressureRecordDao;
  }

  public void insert(String userId, int systolic, int diastolic, int pulse, Date measuredAt, String notes) {
    BloodPressureRecord bpr = new BloodPressureRecord();
    bpr.id = UUID.randomUUID().toString();
    bpr.userId = userId;
    bpr.systolic = systolic;
    bpr.diastolic = diastolic;
    bpr.pulse = pulse;
    bpr.measuredAt = measuredAt;
    bpr.notes = notes;
    bpr.createdAt = new Date();
    bpr.updatedAt = new Date();
    bpr.isSynced = false;

    bloodPressureRecordDao.insert(bpr);
  }

  public List<BloodPressureRecord> getAllByUserId(String userId) {
    return bloodPressureRecordDao.getAllByUserId(userId);
  }

  public List<BloodPressureRecord> getLatestRecordsByUserId(String userId, int limit) {
    return bloodPressureRecordDao.getLatestRecordsByUserId(userId, limit);
  }

  /**
   * Lấy tất cả records huyết áp trong cùng một ngày
   * @param userId ID của user
   * @param date Ngày cần lấy (chỉ lấy phần ngày, bỏ qua giờ phút giây)
   * @return Danh sách các records huyết áp trong ngày đó
   */
  public List<BloodPressureRecord> getRecordsByUserIdAndDay(String userId, Date date) {
    Date startOfDay = DateUtils.getStartOfDay(date);
    Date endOfDay = DateUtils.getEndOfDay(date);

    return bloodPressureRecordDao.getRecordsByUserIdAndDateRange(userId, startOfDay.getTime(), endOfDay.getTime());
  }

  /**
   * Lấy records huyết áp trong khoảng thời gian
   * @param userId ID của user
   * @param startDate Ngày bắt đầu
   * @param endDate Ngày kết thúc
   * @return Danh sách các records huyết áp trong khoảng thời gian
   */
  public List<BloodPressureRecord> getRecordsByUserIdAndDateRange(String userId, Date startDate, Date endDate) {
    Date startOfDay = DateUtils.getStartOfDay(startDate);
    Date endOfDay = DateUtils.getEndOfDay(endDate);

    return bloodPressureRecordDao.getRecordsByUserIdAndDateRange(userId, startOfDay.getTime(), endOfDay.getTime());
  }
}