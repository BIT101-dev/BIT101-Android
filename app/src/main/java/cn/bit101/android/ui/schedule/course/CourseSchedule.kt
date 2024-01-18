package cn.bit101.android.ui.schedule.course

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.common.SimpleState

/**
 * @author flwfdd
 * @date 2023/4/12 14:29
 * @description 课程表主页面
 * _(:з」∠)_
 */

@Composable
fun CourseSchedule(
    mainController: MainController,
    active: Boolean,
    vm: CourseScheduleViewModel = hiltViewModel()
) {
    /**
     * 当前选择的学期
     */
    val term by vm.currentTermFlow.collectAsState(initial = null)

    /**
     * 课表数据
     */
    val courses by vm.courses.collectAsState()

    /**
     * 当前周
     */
    val week by vm.weekFlow.collectAsState()

    /**
     * 学期开始日期
     */
    val firstDay by vm.firstDayFlow.collectAsState(initial = null)

    /**
     * 课表相关配置
     */
    val showDivider by vm.showDividerFlow.collectAsState(initial = null)
    val showSaturday by vm.showSaturdayFlow.collectAsState(initial = null)
    val showSunday by vm.showSundayFlow.collectAsState(initial = null)
    val showHighlightToday by vm.showHighlightTodayFlow.collectAsState(initial = null)
    val showBorder by vm.showBorderFlow.collectAsState(initial = null)

    /**
     * 时间表字符串
     */
    val timeTable by vm.timeTableStringFlow.collectAsState(initial = null)

    /**
     * 显示当前时间线
     */
    val currentTime by vm.showCurrentTimeFlow.collectAsState(initial = null)

    /**
     * 课程详情数据，如果为null就不显示该对话框
     */
    var showCourseDetailState by remember { mutableStateOf<CourseScheduleEntity?>(null) }

    val refreshCoursesState by vm.refreshCoursesStateLiveData.observeAsState()

    val forceRefreshCoursesState by vm.forceRefreshCoursesStateLiveData.observeAsState()

    // 强制刷新的状态在这里管理
    DisposableEffect(forceRefreshCoursesState) {
        if(forceRefreshCoursesState == SimpleState.Success) {
            mainController.snackbar("刷新成功OvO")
        } else if(forceRefreshCoursesState == SimpleState.Fail) {
            mainController.snackbar("刷新失败Orz")
        }
        onDispose {}
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.forceRefreshCoursesStateLiveData.value = null
            vm.refreshCoursesStateLiveData.value = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if(
            term == null ||
            showDivider == null ||
            showSaturday == null ||
            showSunday == null ||
            showHighlightToday == null ||
            showBorder == null ||
            currentTime == null ||
            timeTable == null
        ) { }
        else if(
            term!!.isEmpty() ||
            week == Int.MAX_VALUE ||
            firstDay == null
        ) {
            // datastore数据加载完了，但是数据库中没有数据，应该显示一个按钮，点击后获取
            // 这里如果没有学期数据、课程数据，就显示一个按钮，点击后获取
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    enabled = refreshCoursesState != SimpleState.Loading && forceRefreshCoursesState != SimpleState.Loading,
                    onClick = vm::forceRefreshCourses
                ) {
                    if (refreshCoursesState == SimpleState.Loading || forceRefreshCoursesState == SimpleState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else Text("获取课程表")
                }
            }
        }
        else {
            val settingData = SettingData(
                showDivider = showDivider!!,
                showSaturday = showSaturday!!,
                showSunday = showSunday!!,
                showHighlightToday = showHighlightToday!!,
                showBorder = showBorder!!,
                showCurrentTime = currentTime!!,
            )

            // 数据加载完毕
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 课程表
                CourseScheduleCalendar(
                    courses = courses,
                    week = week,
                    firstDay = firstDay!!,
                    timeTable = timeTable!!,
                    settingData = settingData,

                    onConfig = { mainController.navController.navigate("setting?route=calendar") },
                    onShowDetailDialog = { showCourseDetailState = it },
                    onChangeWeek = { vm.changeWeek(it) }
                )
                if(showCourseDetailState != null) {
                    // 课程详情对话框
                    CourseScheduleDetailDialog(
                        course = showCourseDetailState!!,
                        onDismiss = { showCourseDetailState = null }
                    )
                }
            }
        }
    }
}
