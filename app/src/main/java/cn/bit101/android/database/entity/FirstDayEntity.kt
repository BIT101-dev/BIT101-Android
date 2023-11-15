package cn.bit101.android.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity("first_day")
data class FirstDayEntity(
    @PrimaryKey val term: String,
    val firstDay: LocalDate,
)