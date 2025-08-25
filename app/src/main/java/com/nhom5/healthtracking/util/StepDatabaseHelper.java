package com.nhom5.healthtracking.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nhom5.healthtracking.data.local.entity.StepRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StepDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "step_tracker.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_STEPS = "steps";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_DATE = "date"; // yyyy-MM-dd
    private static final String COLUMN_STEPS = "steps";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_CALORIES = "calories";

    public StepDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_STEPS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_DATE + " TEXT UNIQUE, "
                + COLUMN_STEPS + " INTEGER, "
                + COLUMN_DISTANCE + " REAL, "
                + COLUMN_CALORIES + " REAL)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        onCreate(db);
    }

    /** Thêm hoặc cập nhật dữ liệu ngày hôm nay */
    public void insertOrUpdateStep(StepRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, record.getUserId());
        values.put(COLUMN_DATE, record.getDate());
        values.put(COLUMN_STEPS, record.getStepCount());
        values.put(COLUMN_DISTANCE, record.getDistance());
        values.put(COLUMN_CALORIES, record.getCalories());

        // Nếu ngày đã tồn tại -> update
        int rows = db.update(TABLE_STEPS, values, COLUMN_DATE + "=?", new String[]{record.getDate()});
        if (rows == 0) {
            db.insert(TABLE_STEPS, null, values);
        }
        db.close();
    }

    /** Lấy bản ghi hôm nay */
    public StepRecord getTodayStep(int userId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STEPS, null,
                COLUMN_DATE + "=?", new String[]{today},
                null, null, null);

        StepRecord record = null;
        if (cursor.moveToFirst()) {
            record = new StepRecord(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES))
            );
        }
        cursor.close();
        db.close();
        return record;
    }
    public int getTodaySteps(int userId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int steps = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_STEPS + " FROM " + TABLE_STEPS + " WHERE " + COLUMN_USER_ID + "=? AND " + COLUMN_DATE + "=?",
                new String[]{String.valueOf(userId), today}
        );

        if (cursor.moveToFirst()) {
            steps = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return steps;
    }


    /** Lấy 7 ngày gần nhất */
    public List<StepRecord> getLast7Days(int userId) {
        List<StepRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STEPS, null,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                COLUMN_DATE + " DESC",
                "7");

        if (cursor.moveToFirst()) {
            do {
                records.add(new StepRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return records;
    }

    /** Lấy tất cả dữ liệu */
    public List<StepRecord> getAllSteps(int userId) {
        List<StepRecord> records = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_STEPS, null,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                COLUMN_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                records.add(new StepRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_DISTANCE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CALORIES))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return records;
    }

    /** Xóa 1 ngày */
    public void deleteDay(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STEPS, COLUMN_DATE + "=?", new String[]{date});
        db.close();
    }

    /** Reset toàn bộ */
    public void resetAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STEPS, null, null);
        db.close();
    }
}