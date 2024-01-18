package cn.bit101.android.ui.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.setting.SettingItem
import cn.bit101.android.ui.component.setting.SettingItemData

@Composable
fun SettingIndexPage(
    mainController: MainController,
    paddingValues: PaddingValues,
) {
    val subSettings = listOf(
        SettingItemData.IndexCard(
            title = "账号设置",
            subTitle = "个人信息编辑及登录状态管理",
            icon = Icons.Outlined.AccountCircle,
            onClick = { mainController.navigate("account") }
        ),
        SettingItemData.IndexCard(
            title = "页面设置",
            subTitle = "主页及页面顺序",
            icon = Icons.Outlined.Dashboard,
            onClick = { mainController.navigate("pages") },
        ),
        SettingItemData.IndexCard(
            title = "外观设置",
            subTitle = "主题及暗黑模式",
            icon = Icons.Outlined.ColorLens,
            onClick = { mainController.navigate("theme") },
        ),
        SettingItemData.IndexCard(
            title = "课程表设置",
            subTitle = "课程表数据及显示方式",
            icon = Icons.Outlined.EditCalendar,
            onClick = { mainController.navigate("calendar") },
        ),
        SettingItemData.IndexCard(
            title = "DDL设置",
            subTitle = "日程数据及显示方式",
            icon = Icons.Outlined.EventNote,
            onClick = { mainController.navigate("ddl") },
        ),
        SettingItemData.IndexCard(
            title = "关于",
            subTitle = "关于BIT101-Android",
            icon = Icons.Outlined.Info,
            onClick = { mainController.navigate("about") },
        )
    )


    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
    ) {
        items(subSettings) {
            SettingItem(data = it)
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}