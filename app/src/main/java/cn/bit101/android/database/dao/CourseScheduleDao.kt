package cn.bit101.android.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import cn.bit101.android.database.entity.FirstDayEntity
import cn.bit101.android.database.entity.TermEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CourseScheduleDao {
    @Query("SELECT term FROM terms")
    fun getTerms(): List<String>

    @Insert
    suspend fun insertTerms(terms: List<TermEntity>)

    @Query("DELETE FROM terms")
    suspend fun deleteAll()


    @Query("SELECT firstDay FROM first_day WHERE term = :term")
    fun getFirstDayOf(term: String): LocalDate

    @Upsert
    suspend fun upsertFirstDay(firstDayEntity: FirstDayEntity)
}