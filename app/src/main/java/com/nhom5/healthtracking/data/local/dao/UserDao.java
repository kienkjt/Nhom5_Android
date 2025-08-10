package com.nhom5.healthtracking.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

import com.nhom5.healthtracking.data.local.entity.User;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(User user);

    @Query("SELECT * FROM users")
    List<User> getAll();

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE uid = :uid")
    LiveData<User> observeByUid(String uid);
}
