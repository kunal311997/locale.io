package com.location.reminder.sound.model

import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*


@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int? = null,
    @ColumnInfo(name = "title") var title: String? = null,
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "address") var address: String? = null,
    @ColumnInfo(name = "sourceSoundMode") var sourceSoundMode: String? = null,
    @ColumnInfo(name = "destinationSoundMode") var destinationSoundMode: String? = null,
    @ColumnInfo(name = "distance") var distance: Int? = 0,
    @ColumnInfo(name = "timeInterval") var timeInterval: Int? = 0,
    @ColumnInfo(name = "latitude") var latitude: Double? = 0.0,
    @ColumnInfo(name = "longitude") var longitude: Double? = 0.0,
    @ColumnInfo(name = "isEnabled") val isEnabled: Boolean? = true,
) : java.io.Serializable

