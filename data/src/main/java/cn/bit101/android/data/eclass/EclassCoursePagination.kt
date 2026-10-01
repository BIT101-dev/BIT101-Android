package cn.bit101.android.data.eclass

import cn.bit101.api.model.http.eclass.EclassCoursesDataModel
import java.io.IOException

/** 收集课程分页结果，并判断是否需要继续请求下一页。 */
internal class EclassCoursePagination(
    private val pageSize: Int = DEFAULT_PAGE_SIZE,
    private val maxPages: Int = MAX_PAGES,
) {
    companion object {
        const val MAX_PAGES = 1_000
        private const val DEFAULT_PAGE_SIZE = 100
    }

    private val courses = LinkedHashMap<Int, EclassCoursesDataModel.Course>()

    /** 返回 true 表示调用方需要继续请求下一页。 */
    fun addPage(requestedPage: Int, response: EclassCoursesDataModel.Response): Boolean {
        if (requestedPage !in 1..maxPages) {
            throw IOException("my-courses exceeded $maxPages pages")
        }
        if (response.page != null && response.page != requestedPage) {
            throw IOException("Unexpected my-courses page ${response.page}; requested $requestedPage")
        }

        val previousSize = courses.size
        response.courses
            .filter { it.id > 0 && it.name.isNotBlank() }
            .forEach { courses.putIfAbsent(it.id, it) }
        if (response.courses.isNotEmpty() && courses.size == previousSize) {
            throw IOException("my-courses pagination did not advance on page $requestedPage")
        }

        val pages = response.pages?.takeIf { it > 0 }
        return response.courses.isNotEmpty() && when {
            pages != null -> requestedPage < pages
            else -> response.courses.size >= pageSize
        }
    }

    fun allCourses(): List<EclassCoursesDataModel.Course> = courses.values.toList()
}
