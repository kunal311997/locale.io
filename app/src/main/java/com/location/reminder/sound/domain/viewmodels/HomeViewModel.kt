package com.location.reminder.sound.domain.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.location.reminder.sound.domain.repositories.HomeRepository
import com.location.reminder.sound.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val tasks = MutableLiveData<List<Task>>()
    fun getAddedTasks(): LiveData<List<Task>> = tasks

    fun completeWalkThrough() = repository.completeWalkThrough()

    fun isWalkThroughCompleted() = repository.isWalkThroughCompleted()
    fun setUpdateTime(updateTime: Int) = repository.setUpdateTime(updateTime)
    fun getUpdateTime(): Int = repository.getUpdateTime()
    fun showNotificationDiff(isVisible: Boolean) = repository.showNotificationDiff(isVisible)
    fun isShowNotificationDiff(): Boolean = repository.isShowNotificationDiff()
    fun fetchAddedTasks() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tasks.postValue(repository.getAddedTasks().reversed())
            }
        }
    }

    fun saveTask(task: Task) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.saveTask(task)
                val newTasks = repository.getAddedTasks().reversed()
                tasks.postValue(newTasks)
            }
        }

    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteTask(task)
                fetchAddedTasks()
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.updateTask(task)
                fetchAddedTasks()
            }
        }
    }


}