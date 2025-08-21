package com.nhom5.healthtracking.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom5.healthtracking.data.local.entity.User;
import com.nhom5.healthtracking.util.AuthState;
import com.nhom5.healthtracking.util.FirebaseModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {
    private final FirebaseFirestore fs = FirebaseModule.db();
    private final FirebaseAuth auth;
    private UserRepository userRepository;
    private ExecutorService io;

    private final MutableLiveData<AuthState> state = new MutableLiveData<>(AuthState.Loading.get());
    private volatile boolean initialized = false;
    private LiveData<User> currentUserLiveData;
    private androidx.lifecycle.Observer<User> userObserver;


    private AuthRepository(@NonNull FirebaseAuth auth,
                           @NonNull UserRepository userRepository,
                           @Nullable ExecutorService io) {
        this.auth = auth;
        this.userRepository = userRepository;
        this.io = io != null ? io : Executors.newSingleThreadExecutor();
        
        // Initialize userObserver after auth is set
        this.userObserver = user -> {
            FirebaseUser fu = this.auth.getCurrentUser();
            if (fu != null && user != null) {
                state.postValue(new AuthState.Authenticated(fu.getUid(), user));
            } else if (fu != null) {
                // User is authenticated but profile not found - might be during initial creation
                state.postValue(AuthState.Loading.get());
            }
        };
        
        this.auth.addAuthStateListener(authListener);
    }


    public static AuthRepository create(FirebaseAuth auth, UserRepository userRepository) {
        return new AuthRepository(auth, userRepository, null);
    }

    public void init() {
        if (initialized) return;
        initialized = true;

        state.setValue(AuthState.Loading.get());

        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null) {
            state.setValue(AuthState.Unauthenticated.get());
            return;
        }

        if (currentUserLiveData == null) {
            currentUserLiveData = userRepository.observeCurrentUser();
            currentUserLiveData.observeForever(userObserver);
        }

        // Warm-up token không ép làm mới; nếu hết hạn, lần này online sẽ tự refresh
        fu.getIdToken(false);
    }



    private final FirebaseAuth.AuthStateListener authListener = fbAuth -> {
        FirebaseUser fu = fbAuth.getCurrentUser();
        if (fu == null) {
            // Stop observing user when logged out
            if (currentUserLiveData != null) {
                currentUserLiveData.removeObserver(userObserver);
                currentUserLiveData = null;
            }
            state.postValue(AuthState.Unauthenticated.get());
        } else {
            // Start observing user LiveData when logged in
            if (currentUserLiveData == null) {
                currentUserLiveData = userRepository.observeCurrentUser();
                currentUserLiveData.observeForever(userObserver);
            }
        }
    };

    /**
     * Flow:
     * - Tạo user mới bằng email và password bằng Firebase Auth
     * - Lấy user từ Firebase Auth
     * - Lưu vào Room database (không quan tâm đã lưu vào firestore chưa)
     * - Lưu vào Firestore
     */
    public Task<User> register(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password)
                .continueWithTask(t -> {
                    if (!t.isSuccessful())
                        return Tasks.forException(t.getException());
                    FirebaseUser fu = t.getResult().getUser();
                    if (fu == null)
                        return Tasks.forException(new IllegalStateException("No FirebaseUser"));
                    return userRepository.createNewUserFromFirebaseUser(fu);
                });
    }

    public Task<User> registerWithGoogle(String idToken) {
        return auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .continueWithTask(t -> handleAuthResult(t, "Google register failed"));
    }

    /**
     * Flow:
     * - Đăng nhập bằng email và password bằng Firebase Auth
     * - Đăng nhập thành công -> lấy dữ liệu user từ Firestore
     * - Nếu Firestore có dữ liệu user -> cập nhật dữ liệu user vào Room database (upsert)
     * - Nếu Firestore không có dữ liệu user -> throw exception
     */
    public Task<User> login(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password)
                .continueWithTask(t -> handleAuthResult(t, "Email/password login failed"));
    }

    public Task<User> loginWithGoogle(String idToken) {
        return auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null))
                .continueWithTask(t -> handleAuthResult(t, "Google login failed"));
    }

    private Task<User> handleAuthResult(Task<?> authTask, String errorPrefix) {
        if (!authTask.isSuccessful()) return Tasks.forException(authTask.getException());
        FirebaseUser fu = auth.getCurrentUser();
        if (fu == null)
            return Tasks.forException(new IllegalStateException(errorPrefix + ": No FirebaseUser"));
        final String uid = fu.getUid();
        DocumentReference docRef = fs.collection("users").document(uid);

        return docRef.get().continueWithTask(docTask -> {
            if (!docTask.isSuccessful())
                return Tasks.forException(new IllegalStateException("Firestore fetch user failed. Please try again later."));

            DocumentSnapshot doc = docTask.getResult();
            if (doc == null || !doc.exists()) {
                // For Google login, create new user if not exists
                if (errorPrefix.contains("Google")) {
                    return userRepository.createNewUserFromFirebaseUser(fu)
                            .continueWithTask(userTask -> {
                                if (userTask.isSuccessful()) {
                                    User newUser = userTask.getResult();
                                    // Update AuthState sau khi tạo user thành công
                                    state.postValue(new AuthState.Authenticated(fu.getUid(), newUser));
                                    Log.d("AuthRepository", "New Google user created: " + newUser.toString());
                                }
                                return userTask;
                            });
                }
                return Tasks.forException(new IllegalStateException("User not found in Firestore. Please contact administrator."));
            }

            User ue = UserRepository.mapDocToUser(uid, doc);
            ue.isSynced = true;
            io.execute(() -> {
                userRepository.upsertSync(ue);
                state.postValue(new AuthState.Authenticated(fu.getUid(), ue));
                Log.d("AuthRepository", "AuthState updated: " + ue.toString());
            });

            return Tasks.forResult(ue);
        });
    }

    public String getCurrentUidOrNull() {
        FirebaseUser fu = auth.getCurrentUser();
        return fu != null ? fu.getUid() : null;
    }

    public void logout() {
        try {
            auth.signOut();
        } catch (Exception e) {
            Log.e("AuthRepository", "Logout failed", e);
        } finally {
            state.postValue(AuthState.Unauthenticated.get());
        }
    }

    public LiveData<AuthState> getAuthState() {
        return state;
    }

    public void cleanup() {
        if (currentUserLiveData != null) {
            currentUserLiveData.removeObserver(userObserver);
            currentUserLiveData = null;
        }
        auth.removeAuthStateListener(authListener);
    }
}
