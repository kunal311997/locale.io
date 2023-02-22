package com.location.reminder.sound.finalCode.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.datatransport.Event
import com.location.reminder.sound.finalCode.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    val itemSelectedEvent = MutableLiveData<Event<String>>()

    fun getAddress(): String {
        return repository.getAddress()
    }

    fun completeWalkThrough() {
        repository.completeWalkThrough()
    }

    fun isWalkThroughCompleted() = repository.isWalkThroughCompleted()

}