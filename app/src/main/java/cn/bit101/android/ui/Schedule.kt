package cn.bit101.android.view

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


@Composable
fun Schedule(mainController: MainController) {
    val items = listOf(TabPagerItem("课表") {
        Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = {
                //testLogin()
                mainController.navController.navigate("login")
            }) {
                Text("Tab1")
            }
        }
    }, TabPagerItem("DDL") {
        Text("Tab2", modifier = Modifier.fillMaxSize())
    })
    TabPager(items)
}

