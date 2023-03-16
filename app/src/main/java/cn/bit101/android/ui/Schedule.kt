package cn.bit101.android.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import cn.bit101.android.ui.component.TabPager
import cn.bit101.android.ui.component.TabPagerItem


@Preview(showBackground = true)
@Composable
fun Schedule() {
    val items = listOf(TabPagerItem("课表") {
        Text("Tab1", modifier = Modifier.fillMaxSize())
    }, TabPagerItem("待办") {
        Text("Tab2", modifier = Modifier.fillMaxSize())
    })
    TabPager(items)
}

