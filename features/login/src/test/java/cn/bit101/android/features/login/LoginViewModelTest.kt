package cn.bit101.android.features.login

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.data.repo.base.CommunityLoginRepo
import cn.bit101.android.data.repo.base.LoginException
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: SchoolLoginRepo
    private lateinit var community: CommunityLoginRepo
    private lateinit var vm: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk()
        community = mockk()
        coEvery { community.loginWithSchool(any(), any(), any()) } returns Unit
        val status = mockk<LoginStatus>()
        every { status.sid.flow } returns MutableStateFlow("sid")
        every { status.schoolConnected.flow } returns MutableStateFlow(false)
        vm = LoginViewModel(status, repo, community, SmsCodeRequestHub())
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `rapid submissions start one login and publish loading immediately`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Boolean>()
        coEvery { repo.login(any(), any(), any()) } coAnswers { gate.await() }
        vm.login("sid", "password", communityOnly = false)
        assertTrue(vm.loginState.value is SimpleErrorDataState.Loading)
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        coVerify(exactly = 1) { repo.login(any(), any(), any()) }
        gate.complete(true)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }

    @Test
    fun `opening login does not issue network calls`() = runTest(dispatcher) {
        runCurrent()
        coVerify(exactly = 0) { repo.login(any(), any(), any()); community.login(any(), any()) }
    }

    @Test
    fun `manual community login does not call school`() = runTest(dispatcher) {
        coEvery { community.login(any(), any()) } returns Unit
        vm.login("sid", "independent-password", communityOnly = true)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 1) { community.login("sid", "independent-password") }
        coVerify(exactly = 0) { repo.login(any(), any(), any()) }
    }

    @Test
    fun `failure allows a new submission`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } throws java.io.IOException("offline")
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Fail)
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 2) { repo.login(any(), any(), any()) }
    }

    @Test
    fun `cancelled verification does not show failure or leave loading stuck`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } throws CancellationException("cancelled")
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertNull(vm.loginState.value)
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }
    @Test fun `campus success survives community identity service failure`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } returns true
        coEvery { community.loginWithSchool(any(), any(), any()) } throws mockk<LoginException>()
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        val message = (vm.loginState.value as SimpleErrorDataState.Success).data
        assertTrue(message.contains("校园登录成功"))
        assertTrue(message.contains("BIT101 暂未连接"))
        coVerifyOrder {
            repo.login("sid", "password", any())
            community.loginWithSchool("sid", "password", false)
        }
        coVerify(exactly = 0) { repo.logout() }
    }

    @Test fun `explicit community identity login does not reauthenticate local campus`() = runTest(dispatcher) {
        vm.login("sid", "school-password", communityOnly = true, schoolIdentity = true)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 1) { community.loginWithSchool("sid", "school-password", true) }
        coVerify(exactly = 0) { repo.login(any(), any(), any()); community.login(any(), any()) }
    }

    @Test fun `campus is ready while community identity request is still suspended`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        coEvery { repo.login(any(), any(), any()) } returns true
        coEvery { community.loginWithSchool(any(), any(), any()) } coAnswers { gate.await() }
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.schoolReady.value)
        assertTrue(vm.loginState.value is SimpleErrorDataState.Loading)
        gate.complete(Unit)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }

    @Test fun `entering campus cancels pending community request without logging out campus`() = runTest(dispatcher) {
        val cancelled = CompletableDeferred<Unit>()
        coEvery { repo.login(any(), any(), any()) } returns true
        coEvery { community.loginWithSchool(any(), any(), any()) } coAnswers {
            try { awaitCancellation() } finally { cancelled.complete(Unit) }
        }
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.enterCampus())
        assertFalse(vm.enterCampus())
        runCurrent()
        assertTrue(cancelled.isCompleted)
        assertTrue(vm.schoolReady.value)
        assertNull(vm.loginState.value)
        coVerify(exactly = 1) { community.loginWithSchool(any(), any(), any()) }
        coVerify(exactly = 0) { repo.logout() }
    }

    @Test fun `completed login leaves navigation solely to success callback`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password", communityOnly = false)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
        assertFalse(vm.enterCampus())
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }

}
