package com.nhom5.healthtracking.Blood;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom5.healthtracking.data.local.AppDatabase;
import com.nhom5.healthtracking.data.local.dao.BloodPressureRecordDao;
import com.nhom5.healthtracking.data.local.entity.BloodPressureRecord;
import com.nhom5.healthtracking.data.repository.BloodPressureRecordRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BloodPressureViewModel extends AndroidViewModel {

    private final BloodPressureRecordRepository repository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<BloodPressureRecord>> recordsLiveData = new MutableLiveData<>();
    private String currentUserId;

    public BloodPressureViewModel(@NonNull Application application) {
        super(application);
        BloodPressureRecordDao dao = AppDatabase.getDatabase(application).bloodPressureRecordDao();
        repository = new BloodPressureRecordRepository(dao);
    }

    public LiveData<List<BloodPressureRecord>> getAllRecords(String userId) {
        this.currentUserId = userId;
        loadRecords();
        return recordsLiveData;
    }

    private void loadRecords() {
        if (currentUserId != null) {
            executor.execute(() -> {
                List<BloodPressureRecord> list = repository.getAllByUserId(currentUserId);
                recordsLiveData.postValue(list);
            });
        }
    }

    public void insert(String userId, int systolic, int diastolic, int pulse, Date measuredAt, String notes) {
        executor.execute(() -> {
            repository.insert(userId, systolic, diastolic, pulse, measuredAt, notes);
            loadRecords();
        });
    }

    public String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }
}
