package cn.bit101.android.features.schedule.course

import androidx.compose.ui.graphics.Color

/**
 * 显示在日程表上的物体
 * 主要充当排版时的占位符, 便于扩展
 * 为当周的日程, 所以用周几表示日期
 */
data class ScheduleItem(
    /**
     * 周几, 1 ~ 7
     */
    val dayOfWeek: Int,

    /**
     * 日程开始节次
     * 是浮点数, 用于表达“在节次中间”
     * 精确节次和数据库中记录的时间表的节次对应关系为:
     * 0.0 --- 1.0 --- 2.0 --...    <- 精确
     *  |^^^1^^^|^^^2^^^|^^^3...    <- 数据库
     */
    val startSection : Float,

    /**
     * 日程结束节次
     * 是浮点数, 用于表达“在节次中间”
     * 精确节次和数据库中记录的时间表的节次对应关系为:
     * 0.0 --- 1.0 --- 2.0 --...    <- 精确
     *  |^^^1^^^|^^^2^^^|^^^3...    <- 数据库
     */
    val endSection : Float,

    val title : String,

    val subtitle : String,

    /**
     * 点击时触发的事件, 查看详情等
     */
    val onClick: () -> Unit,

    /**
     * 显示在课程表上的颜色
     * 这里只记录一个配色代码, 具体配色信息存储在 Compose 层
     */
    val color : ScheduleColorEnum
)

enum class ScheduleColorEnum {
    // 这里的顺序不能乱动, 它们会被用作数组下标
    Course,
    Exam
}