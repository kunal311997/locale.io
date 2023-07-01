package com.location.reminder.sound.repositories

import com.location.reminder.sound.db.TaskDao
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.util.SharedPrefClient
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val sharedPrefClient: SharedPrefClient,
    private val database: TaskDao
) : HomeRepository {

    override fun getAddress() = sharedPrefClient.getAddress()
    override fun completeWalkThrough() = sharedPrefClient.completeWalkThrough()
    override fun isWalkThroughCompleted() = sharedPrefClient.isWalkThroughCompleted()
    override fun saveTask(task: Task) {
        database.insertAll(task)
    }

    override fun deleteTask(task: Task) {
       return database.delete(task)
    }

    override fun getAddedTasks(): List<Task> {
        return database.getAll()
    }

}