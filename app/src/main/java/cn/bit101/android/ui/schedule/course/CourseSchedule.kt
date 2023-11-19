package cn.bit101.android.ui.schedule.course

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.database.entity.CourseScheduleEntity
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.gallery.common.SimpleState
import kotlinx.coroutines.launch

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
    val term by vm.termFlow.collectAsState(initial = null)

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
    val firstDay by vm.firstDayFlow.collectAsState()

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
    val timeTableStr by vm.timeTableStringFlow.collectAsState(initial = null)

    /**
     * 显示当前时间线
     */
    val currentTime by vm.showCurrentTimeFlow.collectAsState(initial = null)

    /**
     * 显示配置界面
     */
    var showConfigDialog by rememberSaveable { mutableStateOf(false) }

    /**
     * 课程详情数据，如果为null就不显示该对话框
     */
    var showCourseDetailState by remember { mutableStateOf<CourseScheduleEntity?>(null) }

    /**
     * 设置时间表的这个动作的状态
     */
    val setTimeTableState by vm.setTimeTableStateLiveData.observeAsState()

    val refreshCoursesState by vm.refreshCoursesStateLiveData.observeAsState()

    val forceRefreshCoursesState by vm.forceRefreshCoursesStateLiveData.observeAsState()

    val getTermListState by vm.refreshTermListStateLiveData.observeAsState()

    val changeTermState by vm.changeTermStateLiveData.observeAsState()

    // 强制刷新的状态在这里管理
    DisposableEffect(forceRefreshCoursesState) {
        if(forceRefreshCoursesState == SimpleState.Success) {
            mainController.snackbar("刷新成功OvO")
        } else if(forceRefreshCoursesState == SimpleState.Error) {
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
            timeTableStr == null
        ) {
            // 一开始要在datastore中加载数据，如果没有数据就显示加载中
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .wrapContentSize(Alignment.Center)
//                    .width(64.dp)
//            ) {
//                CircularProgressIndicator(
//                    modifier = Modifier.width(64.dp),
//                    color = MaterialTheme.colorScheme.primary
//                )
//            }
        }
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
            val timeTable = vm.parseTimeTable(timeTableStr!!)
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
                    timeTable = timeTable,
                    settingData = settingData,

                    onConfig = { showConfigDialog = true },
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

            // 设置对话框 自定义进入和退出动画
            AnimatedVisibility(
                visible = showConfigDialog,
                enter = slideIn(
                    initialOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                ),
                exit = slideOut(
                    targetOffset = { IntOffset(0, it.height) },
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = IntOffset.VisibilityThreshold
                    )
                )
            ) {
                CourseScheduleConfigDialog(
                    mainController = mainController,
                    term = term!!,
                    settingData = settingData,
                    timeTableStr = timeTableStr!!,

                    coursesRefreshing = forceRefreshCoursesState is SimpleState.Loading,

                    getTermListState = getTermListState,
                    setTimeTableState = setTimeTableState,
                    changeTermState = changeTermState,

                    onClearStates = {
                        vm.setTimeTableStateLiveData.value = null
                        vm.changeTermStateLiveData.value = null
                    },

                    onForceRefreshCourses = vm::forceRefreshCourses,
                    onSetSetting = vm::setSettingData,
                    onSetTimeTableStr = vm::setTimeTable,

                    onRefreshTermList = vm::refreshTermList,
                    onChangeTerm = vm::changeTerm,

                    onDismiss = { showConfigDialog = false },
                )
            }
        }
    }
    // 响应返回键 收起设置对话框
    BackHandler(enabled = showConfigDialog && active) {
        showConfigDialog = false
    }
}
