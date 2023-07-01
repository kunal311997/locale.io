package com.location.reminder.sound.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.location.reminder.sound.model.Task
import com.location.reminder.sound.repositories.HomeRepository
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

    fun saveTask(task: Task) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.saveTask(task)
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

    fun fetchAddedTasks() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                tasks.postValue(repository.getAddedTasks())
            }
        }
    }

}