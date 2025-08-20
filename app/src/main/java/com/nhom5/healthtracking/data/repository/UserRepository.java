package com.nhom5.healthtracking.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.nhom5.healthtracking.data.local.dao.UserDao;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private static final Executor IO = Executors.newSingleThreadExecutor();
    private final FirebaseFirestore fs = FirebaseModule.db();
    private static volatile UserRepository INSTANCE;

    public static UserRepository getInstance(UserDao userDao) {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) INSTANCE = new UserRepository(userDao);
            }
        }
        return INSTANCE;
    }

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public LiveData<User> observeCurrentUser() {
        FirebaseUser fUser = FirebaseModule.auth().getCurrentUser();
        if (fUser == null) return new MutableLiveData<>(null);
        return userDao.observeByUid(fUser.getUid());
    }

    public User getUserByUid(String uid) {
        return userDao.findByUid(uid);
    }

    public void upsert(User ue) {
        IO.execute(() -> userDao.upsert(ue));
    }

    public void upsertSync(User ue) {
        userDao.upsert(ue);
    }

    public Task<User> createNewUserFromFirebaseUser(FirebaseUser fu) {
        final String uid = fu.getUid();
        User ue = mapFirebaseAuthUser(fu);

        // Tạo TaskCompletionSource để handle async operations properly
        TaskCompletionSource<User> taskSource = new TaskCompletionSource<>();

        // Insert user vào Room database trên background thread
        IO.execute(() -> {
            try {
                userDao.upsert(ue);
                Log.d("UserRepository", "User inserted to Room: " + uid);

                // Sau khi insert Room thành công, save to Firestore
                DocumentReference docRef = fs.collection("users").document(uid);
                docRef.set(toCreateMapWithServerTimestamps(ue), SetOptions.merge())
                        .addOnSuccessListener(v -> {
                            IO.execute(() -> {
                                ue.isSynced = true;
                                ue.updatedAt = new Date();
                                userDao.upsert(ue);
                                Log.d("UserRepository", "User successfully synced to Firestore: " + uid);
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("UserRepository", "Failed to sync user to Firestore: " + uid, e);
                            // User vẫn tồn tại trong Room, chỉ chưa sync
                        });

                // Return user ngay sau khi insert Room thành công
                taskSource.setResult(ue);
            } catch (Exception e) {
                Log.e("UserRepository", "Failed to insert user to Room: " + uid, e);
                taskSource.setException(e);
            }
        });

        return taskSource.getTask();
    }


    private static User mapFirebaseAuthUser(FirebaseUser fu) {
        Date now = new Date();
        User u = new User();
        u.uid = fu.getUid();
        u.email = fu.getEmail();
        u.onboardingStep = 1L;
        u.createdAt = now;
        u.updatedAt = now;
        u.isSynced = false;
        return u;
    }

    public static User mapDocToUser(String uid, DocumentSnapshot d) {
        User u = new User();
        u.uid = uid;
        u.email = d.getString("email");
        u.name = d.getString("name");
        u.gender = d.getString("gender");
        u.dateOfBirth = d.getDate("date_of_birth");
        u.height = d.getLong("height");
        u.onboardingStep = d.getLong("onboarding_step");
        u.createdAt = d.getDate("created_at");
        u.updatedAt = d.getDate("updated_at");
        u.isSynced = true;
        return u;
    }

    private static Map<String, Object> toCreateMapWithServerTimestamps(User ue) {
        Map<String, Object> m = new HashMap<>();
        if (ue.email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        m.put("email", ue.email.toLowerCase());
        m.put("name", ue.name);
        m.put("gender", ue.gender);
        m.put("date_of_birth", ue.dateOfBirth);
        m.put("height", ue.height);
        m.put("onboarding_step", ue.onboardingStep);
        m.put("created_at", FieldValue.serverTimestamp());
        m.put("updated_at", FieldValue.serverTimestamp());
        return m;
    }
}
