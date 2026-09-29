package cn.bit101.android.data.repo

import android.util.Log
import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.eclass.EclassCoursePagination
import cn.bit101.android.data.eclass.EclassDdlItem
import cn.bit101.android.data.eclass.EclassDdlLogic
import cn.bit101.android.data.net.WebViewCookieSync
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.EclassRepo
import cn.bit101.api.model.http.eclass.EclassCoursesDataModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultEclassRepo @Inject constructor(
    private val apiManager: APIManager,
    private val loginStatus: LoginStatus,
) : EclassRepo {

    private companion object {
        const val MAX_ACTIVITY_REQUESTS = 8
        const val TAG = "DefaultEclassRepo"
    }

    private val activitySemaphore = Semaphore(MAX_ACTIVITY_REQUESTS)

    override suspend fun isSessionAlive(): Boolean {
        syncCookies()
        repeat(2) {
            val alive = try {
                val response = apiManager.api.eclass.getMyCourses(
                    EclassCoursesDataModel.Request(page = 1),
                )
                response.isSuccessful && response.body() != null
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            if (alive) return true
        }
        return false
    }

    override suspend fun fetchHomework(now: LocalDateTime): List<EclassDdlItem> {
        syncCookies()
        val courses = fetchAllCourses()
        if (courses.isEmpty()) return emptyList()

        return coroutineScope {
            courses.map { course ->
                async {
                    try {
                        activitySemaphore.withPermit {
                            val response = apiManager.api.eclass.getActivities(course.id)
                            if (!response.isSuccessful) {
                                throw IOException("activities HTTP ${response.code()}")
                            }
                            val activities = response.body()?.activities
                                ?: throw IOException("Empty activities response for course ${course.id}")
                            EclassDdlLogic.toDdlItems(activities, course.name, now)
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch activities for course ${course.id}", e)
                        emptyList()
                    }
                }
            }.awaitAll()
                .flatten()
                .distinctBy { it.uid }
                .sortedBy { it.time }
        }
    }

    private suspend fun fetchAllCourses(): List<EclassCoursesDataModel.Course> {
        val pagination = EclassCoursePagination()
        for (page in 1..EclassCoursePagination.MAX_PAGES) {
            val response = apiManager.api.eclass.getMyCourses(
                EclassCoursesDataModel.Request(page = page),
            )
            if (!response.isSuccessful) {
                throw IOException("my-courses HTTP ${response.code()} on page $page")
            }
            val body = response.body()
                ?: throw IOException("Empty my-courses response on page $page")
            if (!pagination.addPage(page, body)) return pagination.allCourses()
        }
        throw IOException("my-courses exceeded ${EclassCoursePagination.MAX_PAGES} pages")
    }

    override fun syncCookies() {
        WebViewCookieSync.sync(loginStatus.cookieManager, listOf(EclassDdlLogic.BASE_URL))
    }
}
