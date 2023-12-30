package com.location.reminder.sound.domain.repositories

import com.location.reminder.sound.model.Task

interface HomeRepository {
    fun completeWalkThrough()
    fun isWalkThroughCompleted(): Boolean
    fun setUpdateTime(updateTime: Int)
    fun getUpdateTime(): Int
    suspend fun saveTask(task: Task)
    fun deleteTask(task: Task)
    fun updateTask(task: Task): Int
    suspend fun getAddedTasks(): List<Task>
}