package com.nhom5.healthtracking.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nhom5.healthtracking.data.local.entity.StepRecord;
import com.nhom5.healthtracking.step.StepActivity;

import java.util.ArrayList;
import java.util.List;

public class StepDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "health_records.db";
    private static final int DB_VERSION = 2;

    public StepDatabaseHelper(StepActivity context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE step_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "date TEXT," +
                "step_count INTEGER," +
                "source INTEGER," +
                "is_synced INTEGER," +
                "created_at TEXT," +
                "updated_at TEXT," +
                "UNIQUE(user_id, date))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS step_records");
        onCreate(db);
    }

    public void insertOrUpdateStep(StepRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", record.getUserId());
        values.put("date", record.getDate());
        values.put("step_count", record.getStepCount());
        values.put("source", record.getSource());
        values.put("is_synced", record.isSynced() ? 1 : 0);
        values.put("created_at", record.getCreatedAt());
        values.put("updated_at", record.getUpdatedAt());

        long result = db.insertWithOnConflict("step_records", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<StepRecord> getLast7Days(int userId) {
        List<StepRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM step_records WHERE user_id = ? ORDER BY date DESC LIMIT 7",
                new String[]{String.valueOf(userId)}
        );
        while (cursor.moveToNext()) {
            StepRecord record = new StepRecord(
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("step_count")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("source"))
            );
            list.add(record);
        }
        cursor.close();
        return list;
    }
}
