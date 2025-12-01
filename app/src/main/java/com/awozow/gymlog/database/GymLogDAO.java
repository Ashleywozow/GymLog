package com.awozow.gymlog.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.awozow.gymlog.database.entities.GymLog;

import java.util.List;

@Dao
public interface GymLogDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GymLog gymLog);

    @Query("SELECT * FROM " + GymLogDatabase.GYM_LOG_TABLE +" ORDER BY date DESC")
    List<GymLog> getAllRecords();

    @Query("SELECT * FROM " + GymLogDatabase.GYM_LOG_TABLE +" WHERE userId = :loginUserId ORDER BY date DESC")
    List<GymLog> getRecordsByUserId(int loginUserId);
}
