package cn.bit101.android.ui.setting2.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting2.component.SettingItemData
import cn.bit101.android.ui.setting2.component.itemsGroup

@Composable
fun DDLSettingPageContent(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
) {

    val dataItems = listOf(
        SettingItemData(
            title = "重新获取订阅链接",
            subTitle = "重新获取订阅链接",
            onClick = {},
        ),
        SettingItemData(
            title = "重新拉取乐学日程",
            subTitle = "重新拉取乐学日程",
            onClick = {},
        ),
    )

    val displayItems = listOf(
        SettingItemData(
            title = "滞留天数",
            subTitle = "过期日程会继续显示",
            onClick = {},
        ),
        SettingItemData(
            title = "变色天数",
            subTitle = "临近日程会改变颜色",
            onClick = {},
        ),
    )

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(16.dp),
    ) {
        itemsGroup(
            title = "数据设置",
            items = dataItems,
        )

        itemsGroup(
            title = "显示设置",
            items = displayItems,
        )
    }
}