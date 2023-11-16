package cn.bit101.android.ui.schedule.course

import android.util.Log
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import cn.bit101.android.database.entity.CourseEntity
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.gallery.common.SimpleState
import kotlinx.coroutines.MainScope
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
     * 时间表
     */
    val timeTable by vm.timeTableFlow.collectAsState(initial = null)

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
    var showCourseDetailState by remember { mutableStateOf<CourseEntity?>(null) }

    /**
     * 设置时间表的这个动作的状态
     */
    val setTimeTableState by vm.setTimeTableStateLiveData.observeAsState()

    val coursesRefreshing by vm.forceRefreshCoursesStateLiveData.observeAsState()

    val getTermListState by vm.refreshTermListStateLiveData.observeAsState()

    val changeTermState by vm.changeTermStateLiveData.observeAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if(
            term.isNullOrEmpty() ||
            firstDay == null ||
            showDivider == null ||
            showSaturday == null ||
            showSunday == null ||
            showHighlightToday == null ||
            showBorder == null ||
            timeTable == null ||
            currentTime == null ||
            timeTableStr.isNullOrEmpty()
        ) {
            // 一开始要在datastore中加载数据，如果没有数据就显示加载中
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .width(64.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
//        else if(
//            term!!.isEmpty()
//        ) {
//            // datastore数据加载完了，但是数据库中没有数据，应该显示一个按钮，点击后获取
//            // 这里如果没有学期数据、课程数据，就显示一个按钮，点击后获取
//            Column(
//                modifier = Modifier
//                    .fillMaxSize(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Button(
//                    enabled = coursesRefreshing != SimpleState.Loading,
//                    onClick = vm::getCoursesFromNet
//                ) {
//                    if (coursesRefreshing == SimpleState.Loading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(20.dp),
//                            strokeWidth = 2.dp
//                        )
//                    } else Text("获取课程表")
//                }
//            }
//        }
        else {
            // 数据加载完毕
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 课程表
                CourseScheduleCalendar(
                    courses = courses,
                    week = week,
                    firstDay = firstDay!!,
                    showDivider = showDivider!!,
                    showSaturday = showSaturday!!,
                    showSunday = showSunday!!,
                    showHighlightToday = showHighlightToday!!,
                    showBorder = showBorder!!,
                    timeTable = timeTable!!,
                    currentTime = currentTime!!,

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
                    showDivider = showDivider!!,
                    showSaturday = showSaturday!!,
                    showSunday = showSunday!!,
                    showHighlightToday = showHighlightToday!!,
                    showBorder = showBorder!!,
                    timeTable = timeTableStr!!,
                    currentTime = currentTime!!,

                    coursesRefreshing = coursesRefreshing is SimpleState.Loading,

                    changeTermState = changeTermState,
                    setTimeTableState = setTimeTableState,
                    getTermListState = getTermListState,

                    onUpdateCourses = vm::getCoursesFromNet,
                    onSetShowDivider = vm::setShowDivider,
                    onSetShowSaturday = vm::setShowSaturday,
                    onSetShowSunday = vm::setShowSunday,
                    onSetShowHighlightToday = vm::setShowHighlightToday,
                    onSetShowBorder = vm::setShowBorder,
                    onSetCurrentTime = vm::setShowCurrentTime,


                    onClearChangeTermState = { vm.changeTermStateLiveData.value = null },

                    onRefreshTermList = vm::getTermsFromNet,
                    onChangeTerm = vm::changeTerm,
                    onSetTimeTable = vm::setTimeTable,

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
