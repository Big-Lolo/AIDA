package com.example.aida.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.Date

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE isArchived = 0 ORDER BY date asc")
    fun getAll(): List<Message>

    @Insert
    fun insert(msg:Message)

    @Query("UPDATE message SET isArchived = 1 WHERE date < :targetDate ")
    fun archiveAll(targetDate: Date)

}