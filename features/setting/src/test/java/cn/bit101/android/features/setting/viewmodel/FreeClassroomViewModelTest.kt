package cn.bit101.android.features.setting.viewmodel

import cn.bit101.android.config.setting.base.FreeClassroomSettings
import cn.bit101.android.data.repo.base.FreeClassroomRepo
import cn.bit101.android.data.repo.base.SchoolLoginRepo
import cn.bit101.android.features.common.helper.SimpleErrorDataState
import cn.bit101.api.model.common.CampusInfo
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class FreeClassroomViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo: FreeClassroomRepo
    private lateinit var vm: FreeClassroomViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repo = mockk()
        val settings = mockk<FreeClassroomSettings>()
        every { settings.currentCampusCode.flow } returns MutableStateFlow("")
        every { settings.currentCampusDisplayName.flow } returns MutableStateFlow("")
        val loginRepo = mockk<SchoolLoginRepo>()
        coEvery { loginRepo.withSchoolSession<List<CampusInfo>>(any()) } coAnswers {
            firstArg<suspend () -> List<CampusInfo>>().invoke()
        }
        vm = FreeClassroomViewModel(settings, repo, loginRepo)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `persistent failure stays idle until explicit retry`() = runTest(dispatcher) {
        coEvery { repo.getCampusInfos() } throws IOException("offline")
        vm.loadCampusInfos()
        runCurrent()
        assertTrue(vm.campusState.value is SimpleErrorDataState.Fail)
        repeat(10) { vm.loadCampusInfos() }
        advanceUntilIdle()
        coVerify(exactly = 1) { repo.getCampusInfos() }
        vm.loadCampusInfos(retry = true)
        runCurrent()
        coVerify(exactly = 2) { repo.getCampusInfos() }
    }

    @Test
    fun `repeated retries share a single active load`() = runTest(dispatcher) {
        val gate = CompletableDeferred<List<CampusInfo>>()
        coEvery { repo.getCampusInfos() } coAnswers { gate.await() }
        vm.loadCampusInfos()
        assertTrue(vm.campusState.value is SimpleErrorDataState.Loading)
        repeat(10) { vm.loadCampusInfos(retry = true) }
        runCurrent()
        coVerify(exactly = 1) { repo.getCampusInfos() }
        gate.complete(emptyList())
        runCurrent()
        assertEquals(emptyList<CampusInfo>(), (vm.campusState.value as SimpleErrorDataState.Success).data)
        vm.loadCampusInfos()
        runCurrent()
        coVerify(exactly = 1) { repo.getCampusInfos() }
    }
}
