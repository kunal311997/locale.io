package com.location.reminder.sound.finalCode.repositories

interface HomeRepository {
    fun getAddress(): String
    fun completeWalkThrough()
    fun isWalkThroughCompleted(): Boolean
}