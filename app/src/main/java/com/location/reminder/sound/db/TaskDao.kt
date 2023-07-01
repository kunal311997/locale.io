package com.location.reminder.sound.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.location.reminder.sound.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun getAll(): List<Task>

    @Insert
    fun insertAll(vararg task: Task)

    @Delete
    fun delete(task: Task)
}