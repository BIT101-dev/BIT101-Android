package cn.bit101.android.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import cn.bit101.android.data.database.entity.ExamScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
internal interface ExamsDao {
    /**
     * 获取所有考试安排
     */
    @Query("SELECT * FROM exam_schedule")
    fun getAllExams(): Flow<List<ExamScheduleEntity>>

    /**
     * 获取指定学期的考试安排
     */
    @Query("SELECT * FROM exam_schedule WHERE term = :term")
    fun getExamsByTerm(term: String): Flow<List<ExamScheduleEntity>>

    /**
     * 获取指定日期的考试安排
     */
    @Query("SELECT * FROM exam_schedule WHERE date = :date")
    fun getExamsByTermWeek(date: LocalDate): Flow<List<ExamScheduleEntity>>

    /**
     * 插入考试安排
     */
    @Insert
    suspend fun insertExam(course: ExamScheduleEntity)

    /**
     * 插入若干考试安排
     */
    @Insert
    suspend fun insertExams(courses: List<ExamScheduleEntity>)

    /**
     * 删除考试安排
     */
    @Delete
    suspend fun deleteExam(course: ExamScheduleEntity)

    /**
     * 删除所有考试安排
     */
    @Query("DELETE FROM exam_schedule")
    suspend fun deleteAllExams()

    /**
     * 删除指定学期的所有考试安排
     */
    @Query("DELETE FROM exam_schedule WHERE term = :term")
    suspend fun deleteExamsByTerm(term: String)
}