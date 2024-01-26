package com.location.reminder.sound.domain.repositories

import com.location.reminder.sound.db.TaskDao
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.util.SharedPrefClient
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val sharedPrefClient: SharedPrefClient, private val database: TaskDao
) : HomeRepository {
    override fun completeWalkThrough() = sharedPrefClient.completeWalkThrough()
    override fun isWalkThroughCompleted() = sharedPrefClient.isWalkThroughCompleted()
    override fun setUpdateTime(updateTime: Int) = sharedPrefClient.setUpdateTime(updateTime)
    override fun getUpdateTime(): Int = sharedPrefClient.getUpdateTime()
    override fun showNotificationDiff(isVisible: Boolean) =
        sharedPrefClient.showNotificationDiff(isVisible)

    override fun isShowNotificationDiff(): Boolean = sharedPrefClient.isShowNotificationDiff()
    override suspend fun saveTask(task: Task) = database.insert(task)
    override fun deleteTask(task: Task) = database.delete(task)
    override fun updateTask(task: Task): Int = database.update(task)
    override suspend fun getAddedTasks(): List<Task> = database.getAll()

}