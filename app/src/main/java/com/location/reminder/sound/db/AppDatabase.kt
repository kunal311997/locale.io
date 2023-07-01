package com.location.reminder.sound.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.location.reminder.sound.model.Task

@Database(entities = [Task::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}