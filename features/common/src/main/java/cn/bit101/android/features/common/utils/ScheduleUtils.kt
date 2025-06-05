package cn.bit101.android.features.common.utils

import cn.bit101.android.data.database.entity.CustomScheduleEntity
import java.time.LocalDate
import java.time.LocalTime

class ScheduleCreateInfo(
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val date: LocalDate,
    val beginTime: LocalTime,
    val endTime: LocalTime,
) {
    /**
     * 检验自定义日程参数是否合法
     */
    fun check(): Boolean {
        return endTime > beginTime
    }

    /**
     * 将自定义日程参数转换为数据库 Entity
     * 参数非法时抛出异常
     */
    fun toEntity(): CustomScheduleEntity {
        if(!check()) throw IllegalArgumentException("Invalid Period")

        return CustomScheduleEntity(
            id = 0,
            title = title,
            subtitle = subtitle,
            description = description,
            date = date,
            beginTime = beginTime,
            endTime = endTime,
        )
    }
}