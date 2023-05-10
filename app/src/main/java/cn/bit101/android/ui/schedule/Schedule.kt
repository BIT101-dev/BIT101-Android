package cn.bit101.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.bit101.android.MainController
import cn.bit101.android.ui.component.TabPager
import cn.bit101.android.ui.component.TabPagerItem
import cn.bit101.android.ui.schedule.CourseSchedule
import cn.bit101.android.viewmodel.getCoursesFromNet
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


@Composable
fun Schedule(mainController: MainController) {
    val items = listOf(TabPagerItem("课表") {active->
        CourseSchedule(mainController,active)
    }, TabPagerItem("DDL") {
//        Text("Tab2", modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                mainController.route("login")
            }) {
                Text("登录")
            }
            Button(onClick = {
//            testSchedule()
                MainScope().launch {
                    getCoursesFromNet()
                }
//                mainController.navController.navigate("login")
            }) {
                Text("获取课程表")
            }
            Button(onClick = {
                mainController.route("map")
            }) {
                Text("地图")
            }
        }

    })
    TabPager(items)
}

