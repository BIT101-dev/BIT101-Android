package cn.bit101.android.data.eclass

import cn.bit101.api.model.http.eclass.EclassCoursesDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EclassCoursePaginationTest {

    @Test
    fun `按服务端页数收集多页课程`() {
        val pagination = EclassCoursePagination(pageSize = 2)

        assertTrue(
            pagination.addPage(
                requestedPage = 1,
                response = response(page = 1, pages = 2, courses = listOf(course(1), course(2))),
            ),
        )
        assertFalse(
            pagination.addPage(
                requestedPage = 2,
                response = response(page = 2, pages = 2, courses = listOf(course(2), course(3))),
            ),
        )

        assertEquals(listOf(1, 2, 3), pagination.allCourses().map { it.id })
    }

    @Test
    fun `没有总页数时短页结束分页`() {
        val pagination = EclassCoursePagination(pageSize = 2)

        assertFalse(pagination.addPage(1, response(courses = listOf(course(1)))))
        assertEquals(listOf(1), pagination.allCourses().map { it.id })
    }

    @Test
    fun `没有课程时结束分页`() {
        val pagination = EclassCoursePagination(pageSize = 2)

        assertFalse(pagination.addPage(1, response()))
        assertTrue(pagination.allCourses().isEmpty())
    }

    @Test
    fun `重复页不会造成无限翻页`() {
        val pagination = EclassCoursePagination(pageSize = 1)
        pagination.addPage(1, response(courses = listOf(course(1))))

        assertThrows(java.io.IOException::class.java) {
            pagination.addPage(2, response(courses = listOf(course(1))))
        }
    }

    @Test
    fun `拒绝与请求页码不一致的响应`() {
        val pagination = EclassCoursePagination()

        assertThrows(java.io.IOException::class.java) {
            pagination.addPage(2, response(page = 1, courses = listOf(course(1))))
        }
    }

    private fun course(id: Int) = EclassCoursesDataModel.Course(id, "课程$id")

    private fun response(
        page: Int? = null,
        pages: Int? = null,
        courses: List<EclassCoursesDataModel.Course> = emptyList(),
    ) = EclassCoursesDataModel.Response(courses = courses, page = page, pages = pages)
}
