package com.location.reminder.sound.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.datatransport.Event
import com.location.reminder.sound.repository.HomeRepository
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
}