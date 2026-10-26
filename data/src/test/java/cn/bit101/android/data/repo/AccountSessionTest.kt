package cn.bit101.android.data.repo

import cn.bit101.android.data.repo.base.CoursesRepo
import io.mockk.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class AccountSessionTest {
    @Test fun `cache failure cannot prevent logout or allow old requests to commit`() = runBlocking<Unit> {
        val status = MemoryLoginStatus()
        status.seedSchool()
        status.fakeCookie.set("active-community-token")
        val courses = mockk<CoursesRepo>()
        coEvery { courses.saveSchedule(any(), any(), any(), any()) } throws IOException("disk failure")
        val account = AccountSession(status, courses)
        val old = account.identity()
        account.logout()
        assertEquals("", status.sid.get())
        assertEquals("", status.password.get())
        assertEquals("", status.fakeCookie.get())
        assertTrue(status.cookieManager.cookieStore.cookies.isEmpty())
        assertThrows(CancellationException::class.java) { runBlocking { account.commit(old) { fail("stale write") } } }
    }

    @Test fun `new account cannot access leftover cache until cleanup succeeds`() = runBlocking<Unit> {
        val status = MemoryLoginStatus()
        status.seedSchool()
        val courses = mockk<CoursesRepo>(relaxed = true)
        coEvery { courses.saveSchedule(any(), any(), any(), any()) } throws IOException("disk failure")
        val account = AccountSession(status, courses)
        account.logout()
        assertThrows(IOException::class.java) { runBlocking { account.commit(account.identity(), "another") {} } }
        assertEquals("", status.sid.get())
        coEvery { courses.saveSchedule(any(), any(), any(), any()) } returns Unit
        account.commit(account.identity(), "another") { }
        assertEquals("another", status.sid.get())
    }
}
