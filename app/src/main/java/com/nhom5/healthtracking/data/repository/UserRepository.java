package com.nhom5.healthtracking.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
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
    private final FirebaseAuth auth = FirebaseModule.auth();

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public LiveData<User> observeCurrentUser() {
        FirebaseUser fUser = FirebaseModule.auth().getCurrentUser();
        if (fUser == null) return new MutableLiveData<>(null);
        return userDao.observeByUid(fUser.getUid());
    }

    /**
     * Flow:
     * - Tạo user mới bằng email và password bằng Firebase Auth
     * - Lấy user từ Firebase Auth
     * - Lưu vào Room database (không quan tâm đã lưu vào firestore chưa)
     * - Lưu vào Firestore
     */

    public Task<Void> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(t -> {
                    if (!t.isSuccessful())
                        return Tasks.forException(t.getException());
                    FirebaseUser fu = t.getResult().getUser();
                    if (fu == null)
                        return Tasks.forException(new IllegalStateException("No FirebaseUser"));
                    final String uid = fu.getUid();

                    User ue = mapFirebaseAuthUser(fu);

                    IO.execute(() -> {
                        userDao.upsert(ue);
                    });

                    DocumentReference docRef = fs.collection("users").document(uid);
                    return docRef.set(toCreateMapWithServerTimestamps(ue), SetOptions.merge())
                            .addOnSuccessListener(v -> {
                                ue.isSynced = true;
                                ue.updatedAt = new Date();
                                IO.execute(() -> userDao.upsert(ue));
                            });
                });
    }
    /**
     * Flow:
     * - Đăng nhập bằng email và password bằng Firebase Auth
     * - Đăng nhập thành công -> lấy dữ liệu user từ Firestore
     * - Nếu Firestore có dữ liệu user -> cập nhật dữ liệu user vào Room database (upsert)
     * - Nếu Firestore không có dữ liệu user -> throw exception
     */
    public Task<Void> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password)
                .continueWithTask(t -> {
                    if (!t.isSuccessful()) return Tasks.forException(t.getException());
                    FirebaseUser fu = t.getResult().getUser();
                    if (fu == null) return Tasks.forException(new IllegalStateException("No FirebaseUser"));
                    final String uid = fu.getUid();
                    DocumentReference docRef = fs.collection("users").document(uid);

                    return docRef.get().continueWithTask(docTask -> {
                        if (!docTask.isSuccessful())
                            return Tasks.forException(new IllegalStateException("Firestore fetch user failed. Please try again later."));

                        DocumentSnapshot doc = docTask.getResult();
                        if(doc == null || !doc.exists())
                            return Tasks.forException(new IllegalStateException("User not found in Firestore. Please contact administrator."));

                        User ue = mapDocToUser(uid, doc);
                        ue.isSynced = true;
                        IO.execute(() -> userDao.upsert(ue));

                        // create session here
                        return Tasks.forResult(null);
                    });
                });
    }

    private static User mapFirebaseAuthUser(FirebaseUser fu) {
        Date now = new Date();
        User u =  new User();
        u.uid = fu.getUid();
        u.email = fu.getEmail();
        u.createdAt = now;
        u.updatedAt = now;
        u.isSynced = false;
        return u;
    }

    private static User mapDocToUser(String uid, DocumentSnapshot d) {
        User u = new User();
        u.uid = uid;
        u.email = d.getString("email");
        u.name = d.getString("name");
        u.gender = d.getString("gender");
        u.dateOfBirth = d.getString("date_of_birth");
        u.height = d.getLong("height");
        u.createdAt = d.getDate("created_at");
        u.updatedAt = d.getDate("updated_at");
        u.isSynced = true;
        return u;
    }

    private static Map<String, Object> toCreateMapWithServerTimestamps(User ue) {
        Map<String, Object> m = new HashMap<>();
        m.put("email", ue.email.toLowerCase());
        m.put("name", ue.name);
        m.put("gender", ue.gender);
        m.put("date_of_birth", ue.dateOfBirth);
        m.put("height", ue.height);
        m.put("created_at", FieldValue.serverTimestamp());
        m.put("updated_at", FieldValue.serverTimestamp());
        return m;
    }
}
