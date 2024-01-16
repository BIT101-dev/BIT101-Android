package cn.bit101.android.ui.schedule

import androidx.compose.runtime.Composable
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.TabPager
import cn.bit101.android.ui.component.TabPagerItem
import cn.bit101.android.ui.schedule.course.CourseSchedule
import cn.bit101.android.ui.schedule.ddl.DDLSchedule

@Composable
fun ScheduleScreen(
    mainController: MainController,
) {
    val items = listOf(TabPagerItem("课表") {
        CourseSchedule(mainController, it)
    }, TabPagerItem("DDL") {
        DDLSchedule(mainController, it)
    })

    TabPager(items)
}