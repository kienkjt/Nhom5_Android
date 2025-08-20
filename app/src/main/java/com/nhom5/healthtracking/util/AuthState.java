package com.nhom5.healthtracking.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nhom5.healthtracking.data.local.entity.User;

/**
 * AuthState là nguồn trạng thái duy nhất (SSOT) cho UI.
 * - Loading: đang kiểm tra phiên/đăng nhập
 * - Unauthenticated: chưa đăng nhập
 * - Authenticated: đã đăng nhập (kèm user từ Room)
 * - Error: có lỗi khi xác thực (tuỳ chọn)
 */
public abstract class AuthState {

    private AuthState() { /* sealed-like */ }

    // ---------- Loading ----------
    public static final class Loading extends AuthState {
        private static final Loading INSTANCE = new Loading();
        private Loading() {}

        @NonNull
        public static Loading get() { return INSTANCE; }

        @Override public boolean equals(Object obj) { return obj instanceof Loading; }
        @Override public int hashCode() { return 31; }
        @NonNull @Override public String toString() { return "AuthState.Loading"; }
    }

    // ---------- Unauthenticated ----------
    public static final class Unauthenticated extends AuthState {
        private static final Unauthenticated INSTANCE = new Unauthenticated();
        private Unauthenticated() {}

        @NonNull
        public static Unauthenticated get() { return INSTANCE; }

        @Override public boolean equals(Object obj) { return obj instanceof Unauthenticated; }
        @Override public int hashCode() { return 37; }
        @NonNull @Override public String toString() { return "AuthState.Unauthenticated"; }
    }

    // ---------- Authenticated ----------
    public static final class Authenticated extends AuthState {
        private final @NonNull String uid;
        private final @NonNull User profile; // Room entity (có thể null trong lúc upsert đầu tiên)

        public Authenticated(@NonNull String uid, @NonNull User profile) {
            this.uid = uid;
            this.profile = profile;
        }

        @NonNull public String getUid() { return uid; }
        @NonNull public User getProfile() { return profile; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Authenticated)) return false;
            Authenticated other = (Authenticated) o;
            if (!uid.equals(other.uid)) return false;
            return profile.equals(other.profile);
        }

        @Override public int hashCode() {
            int result = uid.hashCode();
            result = 31 * result + profile.hashCode();
            return result;
        }

        @NonNull @Override public String toString() {
            return "AuthState.Authenticated{uid=" + uid + "}";
        }
    }

    // ---------- Error (tuỳ chọn, dùng khi sign-in thất bại/refresh token lỗi) ----------
    public static final class Error extends AuthState {
        private final @NonNull String message;
        private final @Nullable Throwable cause;

        public Error(@NonNull String message, @Nullable Throwable cause) {
            this.message = message;
            this.cause = cause;
        }

        @NonNull public String getMessage() { return message; }
        @Nullable public Throwable getCause() { return cause; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Error)) return false;
            Error other = (Error) o;
            if (!message.equals(other.message)) return false;
            if (cause == null && other.cause == null) return true;
            if (cause == null || other.cause == null) return false;
            return cause.equals(other.cause);
        }

        @Override public int hashCode() {
            int result = message.hashCode();
            result = 31 * result + (cause != null ? cause.hashCode() : 0);
            return result;
        }

        @NonNull @Override public String toString() {
            return "AuthState.Error{message=" + message + ", cause=" + (cause != null ? cause.getClass().getSimpleName() : "null") + "}";
        }
    }

    // ---------- Helpers tiện dụng ----------
    public boolean isLoading() { return this instanceof Loading; }
    public boolean isAuthenticated() { return this instanceof Authenticated; }
    public boolean isUnauthenticated() { return this instanceof Unauthenticated; }
    public boolean isError() { return this instanceof Error; }

    @Nullable
    public String getUidOrNull() {
        if (this instanceof Authenticated) return ((Authenticated) this).getUid();
        return null;
    }

    @Nullable
    public User getProfileOrNull() {
        if (!isAuthenticated()) return null;
        return ((Authenticated) this).getProfile();
    }
}
