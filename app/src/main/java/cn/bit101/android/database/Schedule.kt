package cn.bit101.android.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * @author flwfdd
 * @date 2023/3/31 17:54
 * @description _(:з」∠)_
 */

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

    @Query("SELECT * FROM course_schedule WHERE term = :term AND weeks LIKE '%[' || :week || ']%'")
    fun getWeek(term: String, week: Int): Flow<List<CourseScheduleEntity>>

    @Insert
    suspend fun insert(course: CourseScheduleEntity)

    @Delete
    suspend fun delete(course: CourseScheduleEntity)

    @Query("DELETE FROM course_schedule")
    suspend fun deleteAll()
}
