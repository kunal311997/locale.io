package com.location.reminder.sound.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.location.reminder.sound.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    suspend fun getAll(): List<Task>

    @Insert
    suspend fun insert(vararg task: Task)

    @Delete
    fun delete(task: Task)

    @Update
    fun update(vararg task: Task): Int

}