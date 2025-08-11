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

@Database(entities = {User.class, WeightRecord.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract WeightRecordDao weightRecordDao();
    
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
                    .fallbackToDestructiveMigration()
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
