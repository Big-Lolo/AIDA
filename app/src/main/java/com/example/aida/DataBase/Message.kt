package com.example.aida.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Message (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo (name = "content") val content: String,
    @ColumnInfo (name = "isUser") val isUser: Boolean,
    @ColumnInfo (name = "date") val date: Long,
    @ColumnInfo (name = "isArchived") val isArchived: Boolean


)