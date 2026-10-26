package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.CourseScheduleSettings
import cn.bit101.android.data.repo.base.CoursesRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

data class CourseSyncResult(val examFailure: Exception? = null)

class CourseSyncException(val step: String, cause: Exception) : Exception(step, cause)

/** 所有课表入口共用同步顺序；网络阶段不修改缓存，提交与退出/切号互斥。 */
@Singleton
class CourseSync @Inject internal constructor(
    private val courses: CoursesRepo,
    private val login: SchoolLoginRepo,
    private val settings: CourseScheduleSettings,
    private val account: AccountSession,
) {
    private val mutex = Mutex()

    suspend fun refresh(term: String? = null): CourseSyncResult = sync { owner ->
        var stage = "获取当前学期"
        val (selectedTerm, firstDay, records) = step({ stage }) {
            login.withSchoolSession {
                stage = "获取当前学期"
                val selected = term?.takeIf { it.isNotBlank() } ?: courses.getCurrentTermFromNet()
                stage = "获取学期开始日期"
                val firstDay = courses.getFirstDayFromNet(selected)
                stage = "获取课程表"
                Triple(selected, firstDay, courses.getCoursesFromNet(selected))
            }
        }
        var examFailure: Exception? = null
        val exams = try {
            login.withSchoolSession { courses.getExamsFromNet(selectedTerm) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            examFailure = e
            null
        }
        step({ "保存课程表" }) {
            account.commit(owner) {
                courses.saveSchedule(records, exams, selectedTerm, firstDay)
            }
        }
        CourseSyncResult(examFailure)
    }

    suspend fun refreshFirstDay() = sync { owner ->
        val term = selectedTerm()
        val firstDay = login.withSchoolSession { courses.getFirstDayFromNet(term) }
        account.commit(owner) { settings.setTerm(term, firstDay) }
    }

    suspend fun refreshCourses() = sync { owner ->
        val term = selectedTerm()
        val records = login.withSchoolSession { courses.getCoursesFromNet(term) }
        account.commit(owner) { courses.saveSchedule(courses = records) }
    }

    suspend fun refreshExams(): Int = sync { owner ->
        val term = selectedTerm()
        val previous = courses.getExamsFromLocal(term).first()
        val records = login.withSchoolSession { courses.getExamsFromNet(term) }
        val changed = records.count { next -> previous.none { next.copy(id = it.id) == it } }
        account.commit(owner) { courses.saveSchedule(exams = records) }
        changed
    }

    private suspend fun selectedTerm() = settings.term.get().takeIf { it.isNotBlank() }
        ?: throw CourseSyncException("读取学期", IllegalStateException("请先选择学期"))

    private suspend fun <T> sync(block: suspend (AccountSession.Identity) -> T): T {
        val owner = account.identity()
        return mutex.withLock {
            currentCoroutineContext().ensureActive()
            if (owner != account.identity()) throw CancellationException("账号已切换")
            block(owner)
        }
    }

    private suspend fun <T> step(name: () -> String, block: suspend () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw CourseSyncException(name(), e)
    }
}
