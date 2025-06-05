package cn.bit101.android.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "custom_schedule")
data class CustomScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    /**
     * 标题
     */
    val title: String,

    /**
     * 副标题
     */
    val subtitle: String,

    /**
     * 描述
     */
    val description: String,

    /**
     * 日期
     */
    val date: LocalDate,

    /**
     * 开始时间
     */
    val beginTime: LocalTime,

    /**
     * 结束时间
     */
    val endTime: LocalTime,
)