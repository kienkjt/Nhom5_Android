package com.nhom5.healthtracking.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.nhom5.healthtracking.data.local.dao.UserDao;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.data.local.dao.WeightRecordDao;
import com.nhom5.healthtracking.data.local.entity.WeightRecord;
import com.nhom5.healthtracking.data.local.dao.SleepRecordDao;
import com.nhom5.healthtracking.data.local.entity.SleepRecord;
import com.nhom5.healthtracking.data.local.dao.WaterIntakeDao;
import com.nhom5.healthtracking.data.local.entity.WaterIntake;
import com.nhom5.healthtracking.data.local.dao.BloodPressureRecordDao;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;

@Database(entities = {User.class, WeightRecord.class, SleepRecord.class, WaterIntake.class, BloodPressureRecord.class}, version = 2, exportSchema = false)  // Tăng version lên 2
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract WeightRecordDao weightRecordDao();
    public abstract SleepRecordDao sleepRecordDao();
    public abstract WaterIntakeDao waterIntakeDao();
    public abstract BloodPressureRecordDao bloodPressureRecordDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "health_records.db";

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration()  // Giữ nguyên để xóa và tạo lại
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}

