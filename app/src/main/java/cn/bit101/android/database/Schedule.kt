package cn.bit101.android.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * @author flwfdd
 * @date 2023/3/31 17:54
 * @description _(:з」∠)_
 */


// 课程表课程
@Entity(tableName = "course_schedule")
data class CourseScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val term: String, // 学期
    val name: String, // 课程名
    val teacher: String, // 授课教师 逗号分隔
    val classroom: String, // 教室
    val description: String, // 上课时空描述
    val weeks: String, // 上课周次 形如[1][2][3][4][5][6]
    val weekday: Int, // 星期几
    val start_section: Int, // 开始节次
    val end_section: Int, // 结束节次
    val campus: String, // 校区
    val number: String, // 课程号
    val credit: Int, // 学分
    val hour: Int, // 学时
    val type: String, // 课程性质 必修选修什么的
    val category: String, // 课程类别 文化课实践课什么的
    val department: String, // 开课单位
)

@Dao
interface CourseScheduleDao {
    @Query("SELECT * FROM course_schedule")
    fun getAll(): Flow<List<CourseScheduleEntity>>

    @Query("SELECT * FROM course_schedule WHERE term = :term")
    fun getTerm(term: String): Flow<List<CourseScheduleEntity>>

    @Query("SELECT * FROM course_schedule WHERE term = :term AND weeks LIKE '%[' || :week || ']%'")
    fun getWeek(term: String, week: Int): Flow<List<CourseScheduleEntity>>

    @Insert
    suspend fun insert(course: CourseScheduleEntity)

    @Delete
    suspend fun delete(course: CourseScheduleEntity)

    @Query("DELETE FROM course_schedule")
    suspend fun deleteAll()

    @Query("DELETE FROM course_schedule WHERE term = :term")
    suspend fun deleteTerm(term: String)
}

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

@Dao
interface DDLScheduleDao {
    @Query("SELECT * FROM ddl_schedule ORDER BY time ASC")
    fun getAll(): Flow<List<DDLScheduleEntity>>

    @Query("SELECT * FROM ddl_schedule WHERE time > :time ORDER BY time ASC")
    fun getFuture(time: LocalDateTime): Flow<List<DDLScheduleEntity>>

    @Query("SELECT * FROM ddl_schedule WHERE uid IN (:uid)")
    suspend fun getUIDs(uid: List<String>): List<DDLScheduleEntity>

    @Insert
    suspend fun insert(ddl: DDLScheduleEntity)

    @Update
    suspend fun update(ddl: DDLScheduleEntity)

    @Delete
    suspend fun delete(ddl: DDLScheduleEntity)

    @Query("DELETE FROM ddl_schedule")
    suspend fun deleteAll()

    @Query("DELETE FROM ddl_schedule WHERE id = :id")
    suspend fun deleteID(id: Int)
}
