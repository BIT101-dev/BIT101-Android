package cn.bit101.api.service.school

import cn.bit101.api.helper.emptyLogger
import cn.bit101.api.model.common.SchoolCookie
import cn.bit101.bitlogin.http.HttpClient
import cn.bit101.bitlogin.http.HttpResponse
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import java.net.ConnectException

class SchoolClassroomServiceTest {
    private val cookies = listOf(SchoolCookie("SESSION", "value", "jxzxehallapp.bit.edu.cn", "/", true, null))
    private val client = mockk<HttpClient>(relaxed = true).also { client ->
        every { client.headers } returns mutableMapOf()
        coEvery { client.get(any(), any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), "<html>空教室</html>", firstArg())
        }
    }
    private val service = SchoolClassroomService(emptyLogger, TeachingCenterSession { client })

    private fun response(name: String, body: String) {
        coEvery { client.post(match { it.endsWith("/$name.do") }, any(), any(), any(), any()) } coAnswers {
            HttpResponse(200, emptyMap(), """{"datas":{"$name":$body}}""", firstArg())
        }
    }

    @Before fun calendar() {
        response("dqxnxqcx", """{"rows":[{"DM":"2026-2027-1","XNDM":"2026-2027","XQDM":"1"}]}""")
        response("rqzhzcjc", """{"ZC":"2","XQJ":"1"}""")
    }

    @Test fun `occupancy mapping and pagination preserve the existing query semantics`() = runBlocking {
        coEvery { client.post(match { it.endsWith("cxjsqk.do") }, any(), any(), any(), any()) } coAnswers {
            val form = thirdArg<Map<String, String>>()
            val room = if (form["pageNumber"] == "1") """{"JASMC":"A101","JC1":"1_1,0_2","JC3":"1_9"}"""
                else """{"JASMC":"A102","JC1":"0_1"}"""
            HttpResponse(200, emptyMap(), """{"datas":{"cxjsqk":{"rows":[$room],"totalSize":2}}}""", firstArg())
        }
        val rooms = service.getClassroomOccupancy(cookies, true, "A", "2026-09-14")
        assertEquals(listOf("A101", "A102"), rooms.map { it.classroomName })
        assertEquals("1,3", rooms.first().busyTimeStr)
        assertNull(rooms.last().busyTimeStr)
        coVerify(exactly = 1) { client.get(match { it.contains("webvpn.bit.edu.cn") && it.endsWith("kxjas/*default/index.do") }, any(), any()) }
        coVerify(exactly = 2) { client.post(any(), any(), match { it?.get("XNXQDM") == "2026-2027-1" && it["ZC"] == "2" && it["RQ"] == "2026-09-14" }, any(), any()) }
    }

    @Test fun `missing local term requests current term and date conversion before occupancy`() = runBlocking {
        response("dqxnxqcx", """{"rows":[{"DM":"2026-2027-1","XNDM":"2026-2027","XQDM":"1"}]}""")
        response("rqzhzcjc", """{"ZC":"2","XQJ":"1"}""")
        response("cxjsqk", """{"rows":[],"totalSize":0}""")
        assertTrue(service.getClassroomOccupancy(cookies, false, "A", dateStr = "2026-09-14").isEmpty())
        coVerify { client.post(match { it.endsWith("cxjsqk.do") }, any(), match { it?.get("ZC") == "2" && it["XQ"] == "1" }, any(), any()) }
    }

    @Test fun `login HTML and bare 403 propagate session expiry for bounded recovery`() {
        for ((status, body) in listOf(200 to "<html>统一身份认证</html>", 403 to "")) {
            coEvery { client.post(any(), any(), any(), any(), any()) } coAnswers { HttpResponse(status, emptyMap(), body, firstArg()) }
            assertThrows(SchoolSessionExpired::class.java) {
                runBlocking { service.getClassroomOccupancy(cookies, false, "A") }
            }
        }
    }

    @Test fun `permission page is not converted into a login retry`() {
        coEvery { client.post(any(), any(), any(), any(), any()) } coAnswers {
            HttpResponse(403, emptyMap(), "<html>无权访问该资源</html>", firstArg())
        }
        assertThrows(SchoolAccessDenied::class.java) { runBlocking { service.getClassroomOccupancy(cookies, false, "A") } }
    }

    @Test fun `connection failure retains route so caller can switch once`() {
        coEvery { client.post(any(), any(), any(), any(), any()) } throws ConnectException("offline")
        val error = assertThrows(SchoolRouteUnavailable::class.java) { runBlocking { service.getClassroomOccupancy(cookies, true, "A") } }
        assertTrue(error.webVpn)
    }

    @Test fun `empty page before total is reached fails instead of returning incomplete results`() {
        response("cxjsqk", """{"rows":[],"totalSize":20}""")
        assertThrows(SchoolResponseError::class.java) { runBlocking { service.getClassroomOccupancy(cookies, false, "A") } }
        coVerify(exactly = 1) { client.post(match { it.endsWith("cxjsqk.do") }, any(), any(), any(), any()) }
    }
    @Test fun `same day building queries share school calendar and new day fetches it again`() = runBlocking {
        response("cxjsqk", """{"rows":[],"totalSize":0}""")
        service.getClassroomOccupancy(cookies, false, "A", "2026-09-14")
        service.getClassroomOccupancy(cookies, false, "B", "2026-09-14")
        coVerify(exactly = 1) { client.post(match { it.endsWith("dqxnxqcx.do") }, any(), any(), any(), any()) }
        coVerify(exactly = 1) { client.post(match { it.endsWith("rqzhzcjc.do") }, any(), any(), any(), any()) }
        service.getClassroomOccupancy(cookies, false, "A", "2026-09-15")
        coVerify(exactly = 2) { client.post(match { it.endsWith("rqzhzcjc.do") }, any(), any(), any(), any()) }
    }

    @Test fun `calendar is refreshed after credential change`() = runBlocking {
        response("cxjsqk", """{"rows":[],"totalSize":0}""")
        service.getClassroomOccupancy(cookies, false, "A", "2026-09-14")
        service.getClassroomOccupancy(cookies.map { it.copy(value = "new-session") }, false, "B", "2026-09-14")
        coVerify(exactly = 2) { client.post(match { it.endsWith("dqxnxqcx.do") }, any(), any(), any(), any()) }
    }

}
