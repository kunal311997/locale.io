package com.location.reminder.sound.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "address") var address: String = "",
    @ColumnInfo(name = "sourceSoundMode") var sourceSoundMode: SoundMode = SoundMode.RINGER,
    @ColumnInfo(name = "destinationSoundMode") var destinationSoundMode: SoundMode? = null,
    @ColumnInfo(name = "distance") var distance: Int = 100,
    @ColumnInfo(name = "timeInterval") var timeInterval: Int = 10,
    @ColumnInfo(name = "latitude") var latitude: Double = 0.0,
    @ColumnInfo(name = "longitude") var longitude: Double = 0.0,
    @ColumnInfo(name = "isEnabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "createdAt") val createdAt: LocalDateTime? = LocalDateTime.now(),
) : java.io.Serializable

enum class SoundMode(name: String) {
    RINGER("Ringer"),
    VIBRATE("Vibrate"),
    SILENT("Silent")
}