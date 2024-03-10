package cn.bit101.android.data.database.entity

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

    /**
     * 分组
     */
    val group: String,

    /**
     * 序列号
     */
    val uid: String,

    /**
     * 标题
     */
    val title: String,

    /**
     * 内容
     */
    val text: String,

    /**
     * 到期时间
     */
    val time: LocalDateTime,

    /**
     * 是否完成
     */
    val done: Boolean
)