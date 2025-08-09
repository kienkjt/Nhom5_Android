package com.nhom5.healthtracking.water;


import android.content.Context;
import android.content.SharedPreferences;

public class WaterDataManager {
    private static final String PREF_NAME = "WaterPrefs";
    private static final String TOTAL_WATER_KEY = "totalWater";
    private static final String GOAL_WATER_KEY = "goalWater";
    private static final String WEEKLY_WATER_KEY_PREFIX = "weeklyWater_";
    private static final String LAST_INPUT_WATER_KEY = "lastInputWater";
    private static final String LAST_DAY_KEY = "lastDay";
    private static final String LAST_YEAR_KEY = "lastYear";

    public static void saveTotalWater(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(TOTAL_WATER_KEY, value).apply();
    }

    public static int getTotalWater(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(TOTAL_WATER_KEY, 0);
    }

    public static void saveGoalWater(Context context, int value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(GOAL_WATER_KEY, value).apply();
    }

    public static int getGoalWater(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(GOAL_WATER_KEY, 2000);
    }

    public static void saveWeeklyWater(Context context, int[] data) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < 7; i++) {
            editor.putInt(WEEKLY_WATER_KEY_PREFIX + i, data[i]);
        }
        editor.apply();
    }

    public static int[] getWeeklyWater(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int[] data = new int[7];
        for (int i = 0; i < 7; i++) {
            data[i] = prefs.getInt(WEEKLY_WATER_KEY_PREFIX + i, 0);
        }
        return data;
    }

    public static void saveLastInputWater(Context context, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(LAST_INPUT_WATER_KEY, value).apply();
    }

    public static String getLastInputWater(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LAST_INPUT_WATER_KEY, "");
    }

    // Thêm phần lưu và đọc ngày/năm
    public static int getLastDay(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(LAST_DAY_KEY, -1);
    }

    public static int getLastYear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(LAST_YEAR_KEY, -1);
    }

    public static void saveLastDay(Context context, int day) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(LAST_DAY_KEY, day).apply();
    }

    public static void saveLastYear(Context context, int year) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(LAST_YEAR_KEY, year).apply();
    }
}
