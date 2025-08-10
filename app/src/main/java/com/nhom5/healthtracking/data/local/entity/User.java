package com.nhom5.healthtracking.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String uid; // Firebase UID
    
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "date_of_birth")
    public String dateOfBirth;

    @ColumnInfo(name = "gender")
    public String gender;
    
    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "height")
    public Long height;

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
                '}';
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getDateOfBirth() {
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
}
