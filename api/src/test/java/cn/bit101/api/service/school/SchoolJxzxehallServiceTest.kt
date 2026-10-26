package cn.bit101.api.service.school

import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.Config
import cn.bit101.bitlogin.http.CookieDetail
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class SchoolJxzxehallServiceTest {
    private val cookies = listOf(SchoolCookie("SESSION", "value", "jxzxehallapp.bit.edu.cn", "/", true, null))
    private val client = mockk<HttpClient>(relaxed = true).also { client ->
        every { client.headers } returns mutableMapOf()
        every { client.cookieDetails() } returns listOf(CookieDetail("SESSION", "value", "jxzxehallapp.bit.edu.cn", "/", true, null))
        coEvery { client.get(any(), any(), any()) } coAnswers {
            val url = firstArg<String>()
            HttpResponse(200, emptyMap(), if (url.endsWith("dqxnxq.do"))
                """{"datas":{"dqxnxq":{"rows":[{"DM":"2026-2027-1"}]}}}""" else "{}", url)
        }
        coEvery { client.post(any(), any(), any(), any(), any()) } coAnswers {
            val url = firstArg<String>()
            val body = when {
                url.endsWith("cxzkbrq.do") -> """{"data":[{"XQ":1,"RQ":"2026-09-07"}]}"""
                url.endsWith("cxxszhxqkb.do") -> """{"datas":{"cxxszhxqkb":{"rows":[{"KCM":"数学","SKZC":"1100","SKXQ":1,"KSJC":3,"JSJC":4,"JASMC":"A101"}]}}}"""
                else -> """{"datas":{"cxxsksap":{"rows":[{"JASMC":"A102","KSSJMS":"09:00","KSRQ":"2027-01-01","ZWH":"12","KSMC":"数学考试","KCM":"数学","KCH":"MATH"}]}}}"""
            }
            HttpResponse(200, emptyMap(), body, url)
        }
    }
    private val service = SchoolJxzxehallService(emptyLogger, TeachingCenterSession { client })

    @Test fun `term dates courses and exams preserve existing field mappings`() = runBlocking<Unit> {
        assertEquals("2026-2027-1", service.getCurrentTerm(cookies, false))
        assertEquals("2026-09-07", service.getFirstDay(cookies, false, "2026-2027-1"))
        val course = service.getCourseSchedule(cookies, false, "2026-2027-1").single()
        assertEquals("数学", course.KCM)
        assertEquals("1100", course.SKZC)
        assertEquals(3, course.KSJC)
        assertEquals(4, course.JSJC)
        val exam = service.getExamList(cookies, false, "2026-2027-1").single()
        assertEquals("数学", exam.courseCode)
        assertEquals("MATH", exam.kch)
        assertEquals("12", exam.seatId)
        coVerify(exactly = 1) { client.get(match { it.contains("getAppConfig/wdkbby-") }, any(), any()) }
        coVerify(exactly = 1) { client.post(match { it.endsWith("cxzkbrq.do") }, any(), any(), any(), any()) }
        coVerify { client.get(match { it.endsWith("dqxnxq.do") }, Config.Headers.base, any()) }
        coVerify { client.post(match { it.endsWith("cxxszhxqkb.do") }, any(), mapOf("XNXQDM" to "2026-2027-1"), any(), any()) }
    }

    @Test fun `missing current term reports malformed data without guessing account type`() = runBlocking<Unit> {
        coEvery { client.get(match { it.endsWith("dqxnxq.do") }, any(), any()) } returns HttpResponse(200, emptyMap(), "{}", "https://jxzxehallapp.bit.edu.cn/api")
        val error = assertThrows(SchoolResponseError::class.java) { runBlocking { service.getCurrentTerm(cookies, false) } }
        assertFalse(error.message.orEmpty().contains("账号"))
    }

    private fun courseResponse(row: String) {
        coEvery { client.post(match { it.endsWith("cxxszhxqkb.do") }, any(), any(), any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), """{"datas":{"cxxszhxqkb":{"rows":[$row]}}}""", firstArg())
        }
    }

    @Test fun `fractional credits and numeric strings do not break the whole timetable`() = runBlocking<Unit> {
        for (credit in listOf("1.5", "\"1.5\"")) {
            courseResponse("""{"KCM":"数学","SKZC":"1100","SKXQ":"1","KSJC":"3","JSJC":"4","XF":$credit}""")
            val course = service.getCourseSchedule(cookies, false, "2026-2027-1").single()
            assertEquals(1.5, course.XF!!, 0.0)
            assertEquals("2026-2027-1", course.XNXQDM)
        }
    }

    @Test fun `invalid course rows fail before replacing the cached timetable`() = runBlocking<Unit> {
        for (row in listOf("null", "{}",
            """{"KCM":"数学","SKZC":"1100","SKXQ":8,"KSJC":3,"JSJC":4}""",
            """{"KCM":"数学","SKZC":"1100","SKXQ":1,"KSJC":4,"JSJC":3}""",
            """{"KCM":"数学","SKZC":"bad","SKXQ":1,"KSJC":3,"JSJC":4}""")) {
            courseResponse(row)
            assertThrows(SchoolResponseError::class.java) { runBlocking {
                service.getCourseSchedule(cookies, false, "2026-2027-1")
            } }
        }
    }

    @Test fun `empty timetables and courses awaiting placement remain valid`() = runBlocking<Unit> {
        courseResponse("")
        assertTrue(service.getCourseSchedule(cookies, false, "2026-2027-1").isEmpty())
        courseResponse("""{"KCH":"MATH","SKZC":"0000"}""")
        assertEquals("MATH", service.getCourseSchedule(cookies, false, "2026-2027-1").single().KCM)
    }
}
