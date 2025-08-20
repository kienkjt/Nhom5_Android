package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid; // Firebase UID

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "date_of_birth")
    public Date dateOfBirth;

    @ColumnInfo(name = "gender")
    public String gender;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "height")
    public Long height;

    @ColumnInfo(name = "onboarding_step")
    // 1: Personal Info, 2: Body Metrics, 3: Health Goals, 4: Finish
    public Long onboardingStep;

    @ColumnInfo(name = "created_at")
    public Date createdAt;

    @ColumnInfo(name = "updated_at")
    public Date updatedAt;

    @ColumnInfo(name = "is_synced")
    public boolean isSynced;

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", height='" + height + '\'' +
                ", onboardingStep='" + onboardingStep + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", isSynced='" + isSynced + '\'' +
                '}';
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public Long getHeight() {
        return height;
    }

    public Long getAge() {
        // Dùng cách khác như LocalDate thì sẽ bị require sdk version, không thích hợp với nhiều thiết bị
        if (dateOfBirth == null) return null;
        Calendar today = java.util.Calendar.getInstance();
        Calendar birth = java.util.Calendar.getInstance();

        int years = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR);

        int todayMonth = today.get(java.util.Calendar.MONTH);
        int birthMonth = birth.get(java.util.Calendar.MONTH);
        int todayDay = today.get(java.util.Calendar.DAY_OF_MONTH);
        int birthDay = birth.get(java.util.Calendar.DAY_OF_MONTH);

        if (todayMonth < birthMonth || (todayMonth == birthMonth && todayDay < birthDay)) {
            years--;
        }

        return (long) Math.max(years, 0);
    }

    public Long getOnboardingStep() {
        return onboardingStep;
    }

    public boolean hasCompletedOnboarding() {
        if (onboardingStep == null) return false;
        return onboardingStep == 4;
    }
}
