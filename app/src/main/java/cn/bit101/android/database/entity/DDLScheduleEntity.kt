package cn.bit101.android.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// 待办事项
@Entity(
    tableName = "ddl_schedule",
    indices = [Index(value = ["uid"], unique = true), Index(value = ["group"])] //添加索引
)

data class DDLScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val group: String, // 分组
    val uid: String, // 序列号
    val title: String, // 标题
    val text: String, // 内容
    val time: LocalDateTime, // 到期时间
    val done: Boolean // 是否完成
)