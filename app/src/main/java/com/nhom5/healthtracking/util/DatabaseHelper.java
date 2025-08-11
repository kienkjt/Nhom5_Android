package com.nhom5.healthtracking.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nhom5.healthtracking.data.local.entity.WeightRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "health_records.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_WEIGHT = "weight_records";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WEIGHT_TABLE = "CREATE TABLE " + TABLE_WEIGHT + " (" +
                "id TEXT PRIMARY KEY," +
                "user_id TEXT," +
                "weight REAL," +
                "recorded_at TEXT," +
                "notes TEXT," +
                "created_at TEXT," +
                "updated_at TEXT," +
                "is_synced INTEGER)";
        db.execSQL(CREATE_WEIGHT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        onCreate(db);
    }

    public void addWeight(String userId, double weight, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put("id", UUID.randomUUID().toString());
        values.put("user_id", userId);
        values.put("weight", weight);
        values.put("recorded_at", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        values.put("notes", notes);
        values.put("created_at", now);
        values.put("updated_at", now);
        values.put("is_synced", 0);
        db.insert(TABLE_WEIGHT, null, values);
        db.close();
    }

    public List<WeightRecord> getAllWeights(String userId) {
        List<WeightRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WEIGHT, null, "user_id=?", new String[]{userId}, null, null, "recorded_at DESC");

        if (cursor.moveToFirst()) {
            do {
                list.add(new WeightRecord(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getFloat(cursor.getColumnIndexOrThrow("weight")),
                        new Date(), // You can parse recorded_at here
                        cursor.getString(cursor.getColumnIndexOrThrow("notes"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
