package cn.bit101.android.status.base

interface CourseScheduleStatusManager {
    suspend fun getCurrentTerm(): String
    suspend fun setCurrentTerm(term: String)
}