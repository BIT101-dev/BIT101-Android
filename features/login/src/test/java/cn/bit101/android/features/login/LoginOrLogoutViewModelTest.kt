package cn.bit101.android.features.login

import cn.bit101.android.config.user.base.LoginStatus
import cn.bit101.android.data.common.SmsCodeRequestHub
import cn.bit101.android.data.repo.base.LoginRepo
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
class LoginOrLogoutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: LoginRepo
    private lateinit var vm: LoginOrLogoutViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk()
        val status = mockk<LoginStatus>()
        every { status.sid.flow } returns MutableStateFlow("sid")
        vm = LoginOrLogoutViewModel(status, repo, SmsCodeRequestHub())
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `rapid submissions start one login and publish loading immediately`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Boolean>()
        coEvery { repo.login(any(), any(), any()) } coAnswers { gate.await() }
        vm.login("sid", "password")
        assertTrue(vm.loginState.value is SimpleErrorDataState.Loading)
        vm.login("sid", "password")
        runCurrent()
        coVerify(exactly = 1) { repo.login(any(), any(), any()) }
        gate.complete(true)
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }

    @Test
    fun `initial check runs only once across repeated entry calls`() = runTest(dispatcher) {
        val gate = CompletableDeferred<Boolean>()
        coEvery { repo.checkLogin() } coAnswers { gate.await() }
        vm.checkLoginState()
        vm.checkLoginState()
        vm.login("sid", "password")
        runCurrent()
        coVerify(exactly = 1) { repo.checkLogin() }
        coVerify(exactly = 0) { repo.login(any(), any(), any()) }
        gate.complete(false)
        runCurrent()
        vm.checkLoginState()
        runCurrent()
        coVerify(exactly = 1) { repo.checkLogin() }
    }

    @Test
    fun `cancelled initial verification returns to login form`() = runTest(dispatcher) {
        coEvery { repo.checkLogin() } throws CancellationException("cancelled")
        vm.checkLoginState()
        runCurrent()
        assertEquals(false, (vm.checkLoginState.value as SimpleErrorDataState.Success).data)
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password")
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }

    @Test
    fun `failure allows a new submission`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } throws java.io.IOException("offline")
        vm.login("sid", "password")
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Fail)
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password")
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
        coVerify(exactly = 2) { repo.login(any(), any(), any()) }
    }

    @Test
    fun `cancelled verification does not show failure or leave loading stuck`() = runTest(dispatcher) {
        coEvery { repo.login(any(), any(), any()) } throws CancellationException("cancelled")
        vm.login("sid", "password")
        runCurrent()
        assertNull(vm.loginState.value)
        coEvery { repo.login(any(), any(), any()) } returns true
        vm.login("sid", "password")
        runCurrent()
        assertTrue(vm.loginState.value is SimpleErrorDataState.Success)
    }
}
