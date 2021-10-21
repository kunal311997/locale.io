package com.location.reminder.sound.repository

import com.location.reminder.sound.util.SharedPrefClient
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val sharedPrefClient: SharedPrefClient
) : HomeRepository {

    override fun getAddress(): String {
        return sharedPrefClient.getAddress()
    }
}