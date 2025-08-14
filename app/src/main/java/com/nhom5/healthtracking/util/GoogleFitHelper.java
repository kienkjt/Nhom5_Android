package com.nhom5.healthtracking.util;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class GoogleFitHelper {

    public static final int REQUEST_OAUTH_REQUEST_CODE = 1001;
    private final Activity activity;
    private final FitnessOptions fitnessOptions;

    public GoogleFitHelper(Activity activity) {
        this.activity = activity;
        this.fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
                .build();
    }

    public boolean hasPermission() {
        return GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(activity),
                fitnessOptions
        );
    }

    public void requestPermission() {
        GoogleSignIn.requestPermissions(
                activity,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(activity),
                fitnessOptions
        );
    }

    public void readStepsToday(OnStepsReadListener listener) {
        // Cách 1: Dùng readDailyTotal()
        Task<DataSet> response = Fitness.getHistoryClient(activity,
                        GoogleSignIn.getLastSignedInAccount(activity))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA);

        response.addOnSuccessListener(dataSet -> {
            int total = dataSet.isEmpty()
                    ? 0
                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

            if (total > 0) {
                listener.onStepsRead(total);
            } else {
                // Fallback sang aggregate
                readStepsWithAggregate(listener);
            }
        }).addOnFailureListener(e -> {
            // Nếu lỗi thì cũng fallback
            readStepsWithAggregate(listener);
        });
    }

    private void readStepsWithAggregate(OnStepsReadListener listener) {
        Calendar cal = Calendar.getInstance();
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                .readData(readRequest)
                .addOnSuccessListener(dataReadResponse -> {
                    int total = 0;
                    if (!dataReadResponse.getBuckets().isEmpty()) {
                        for (DataPoint dp : dataReadResponse.getBuckets().get(0).getDataSet(DataType.AGGREGATE_STEP_COUNT_DELTA).getDataPoints()) {
                            total += dp.getValue(Field.FIELD_STEPS).asInt();
                        }
                    }
                    listener.onStepsRead(total);
                })
                .addOnFailureListener(listener::onError);
    }

    public interface OnStepsReadListener {
        void onStepsRead(int steps);
        void onError(Exception e);
    }
}
