package cn.bit101.android.data.repo

import cn.bit101.android.config.setting.base.DDLSettings
import cn.bit101.android.config.setting.base.LexueSubscription
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.database.BIT101Database
import cn.bit101.android.data.database.dao.DDLScheduleDao
import cn.bit101.android.data.database.entity.DDLScheduleEntity
import cn.bit101.android.data.net.base.APIManager
import cn.bit101.api.model.http.school.GetCalendarDataModel
import cn.bit101.api.service.school.SchoolLexueService
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class DefaultDDLScheduleRepoTest {
    private val status = MemoryLoginStatus()
    private val account = AccountSession(status, mockk(relaxed = true))
    private val db = mockk<BIT101Database>()
    private val dao = mockk<DDLScheduleDao>(relaxed = true)
    private val service = mockk<SchoolLexueService>()
    private val settings = mockk<DDLSettings>()
    private val subscription = MutableStateFlow(LexueSubscription("A", "https://lexue.bit.edu.cn/calendar-A"))
    private val manager = mockk<APIManager>(relaxed = true)
    private val repo: DefaultDDLScheduleRepo
    private val custom = DDLScheduleEntity(1, "main", "custom", "自定义", "", LocalDateTime.now(), false)
    private val schoolA = custom.copy(id = 2, group = "lexue", uid = "school", ownerSid = "A")

    init {
        every { db.DDLScheduleDao() } returns dao
        every { manager.api.schoolLexue } returns service
        every { settings.subscription } returns subscription
        coEvery { settings.setSubscription(any()) } coAnswers { subscription.value = firstArg() }
        repo = DefaultDDLScheduleRepo(db, manager, status, mockk<SmsCodeRequestHub>(relaxed = true), settings, account)
    }

    private suspend fun signIn(sid: String) {
        account.commit(account.identity(), sid) { status.password.set("fixture") }
    }

    @Test fun `subscription is usable only by its owner including legacy subscriptions`() = runBlocking<Unit> {
        signIn("A")
        assertEquals(subscription.value.url, repo.calendarUrl.first())
        signIn("B")
        assertEquals("", repo.calendarUrl.first())
        subscription.value = LexueSubscription("", "https://lexue.bit.edu.cn/legacy")
        assertEquals("", repo.calendarUrl.first())
        subscription.value = LexueSubscription("B", "https://lexue.bit.edu.cn/calendar-B")
        assertEquals(subscription.value.url, repo.calendarUrl.first())
        account.logout()
        assertEquals("", repo.calendarUrl.first())
    }

    @Test fun `account changes hide imported schedules while preserving custom DDL`() = runBlocking<Unit> {
        val schoolB = schoolA.copy(id = 3, ownerSid = "B")
        val unowned = schoolA.copy(id = 4, ownerSid = "")
        every { dao.getFuture(any()) } returns flowOf(listOf(custom, schoolA, schoolB, unowned))
        signIn("A")
        assertEquals(listOf(custom, schoolA), repo.getFutureDDL(LocalDateTime.now()).first())
        signIn("B")
        assertEquals(listOf(custom, schoolB), repo.getFutureDDL(LocalDateTime.now()).first())
        account.logout()
        assertEquals(listOf(custom), repo.getFutureDDL(LocalDateTime.now()).first())
    }

    @Test fun `old subscription response cannot overwrite a new account subscription`() = runBlocking<Unit> {
        signIn("A")
        val started = CompletableDeferred<Unit>()
        val response = CompletableDeferred<String>()
        coEvery { service.getCalendarUrl(any(), any(), any(), any()) } coAnswers {
            started.complete(Unit)
            response.await()
        }
        val request = launch { repo.refreshSubscription() }
        started.await()
        signIn("B")
        subscription.value = LexueSubscription("B", "https://lexue.bit.edu.cn/calendar-B")
        response.complete("https://lexue.bit.edu.cn/new-A")
        request.join()
        assertTrue(request.isCancelled)
        assertEquals("B", subscription.value.ownerSid)
        coVerify(exactly = 0) { settings.setSubscription(any()) }
    }

    @Test fun `logout and login to the same account rejects old calendar downloads before database writes`() = runBlocking<Unit> {
        signIn("A")
        val started = CompletableDeferred<Unit>()
        val response = CompletableDeferred<List<GetCalendarDataModel.CalendarEvent>>()
        coEvery { service.getCalendar(any(), any(), any()) } coAnswers {
            started.complete(Unit)
            response.await()
        }
        val request = launch { repo.syncCalendar() }
        started.await()
        account.logout()
        signIn("A")
        response.complete(emptyList())
        request.join()
        assertTrue(request.isCancelled)
        verify { db wasNot Called }
    }

    @Test fun `foreign subscription never reaches the network`() = runBlocking<Unit> {
        signIn("B")
        try { repo.syncCalendar(); fail("must reject a foreign URL") } catch (_: IllegalArgumentException) { }
        coVerify(exactly = 0) { service.getCalendar(any(), any(), any()) }
    }

    @Test fun `old imported checkbox cannot edit after account switch while custom checkbox still works`() = runBlocking<Unit> {
        signIn("B")
        try { repo.setDone(schoolA, true); fail("must reject an old-account event") } catch (_: CancellationException) { }
        coVerify(exactly = 0) { dao.setDone(any(), any(), any()) }
        repo.setDone(custom, true)
        coVerify(exactly = 1) { dao.setDone(custom.id, "", true) }
        coVerify(exactly = 0) { dao.update(any()) }
    }
}
