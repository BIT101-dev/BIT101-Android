package cn.bit101.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cn.bit101.android.MainController
import cn.bit101.android.database.DataStore
import cn.bit101.android.ui.component.TabPager
import cn.bit101.android.ui.component.TabPagerItem
import cn.bit101.android.ui.schedule.CourseSchedule
import cn.bit101.android.ui.schedule.DDLSchedule


// 课表+DDL主界面
@Composable
fun Schedule(mainController: MainController) {
    val items = listOf(TabPagerItem("课表") {
        CourseSchedule(mainController, it)
    }, TabPagerItem("DDL") {
        DDLSchedule(mainController, it)
    })

    // 检查登陆状态 没有登陆则显示登陆按钮
    val loginStatus = DataStore.loginStatusFlow.collectAsState(initial = null)
    if (loginStatus.value == false) {
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
        }
    }
    if (loginStatus.value == true) {
        TabPager(items)
    }
}
