package cn.bit101.android.status

import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.repo.base.CoursesRepo
import cn.bit101.android.status.base.CourseScheduleStatusManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultCourseScheduleStatusManager @Inject constructor(
) : CourseScheduleStatusManager {
    override suspend fun getCurrentTerm() = withContext(Dispatchers.IO) {
        SettingDataStore.courseScheduleTerm.get()
    }

    override suspend fun setCurrentTerm(
        term: String
    ) = withContext(Dispatchers.IO) {
        SettingDataStore.courseScheduleTerm.set(term)
    }

}