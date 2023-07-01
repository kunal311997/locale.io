package com.location.reminder.sound.repositories

import com.location.reminder.sound.model.Task

interface HomeRepository {
    fun getAddress(): String
    fun completeWalkThrough()
    fun isWalkThroughCompleted(): Boolean
    fun saveTask(task: Task)
    fun deleteTask(task: Task)
    fun getAddedTasks(): List<Task>
}