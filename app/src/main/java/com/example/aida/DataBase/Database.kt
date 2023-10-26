package com.example.aida.DataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(entities = [Message::class], version = 1)
@TypeConverters(Converters::class)
abstract class Database: RoomDatabase() {

    abstract fun messageDao(): MessageDao




}

