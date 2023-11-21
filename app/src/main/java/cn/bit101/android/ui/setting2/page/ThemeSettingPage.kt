package cn.bit101.android.ui.setting2.page

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting2.IndexSettingItem
import cn.bit101.android.ui.setting2.component.SettingItem
import cn.bit101.android.ui.setting2.component.SettingItemData

@Composable
fun ThemeSettingPageContent(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: androidx.compose.ui.input.nestedscroll.NestedScrollConnection,
) {
    var checked by remember { mutableStateOf(false) }


    val settings = listOf(
        SettingItemData(
            title = "动态适配系统主题",
            subTitle = "需要Android 12及以上",
            onClick = {},
            suffix = {
                Switch(checked = checked, onCheckedChange = { checked = it })
            }
        ),
        SettingItemData(
            title = "暗黑模式",
            subTitle = "设置暗黑模式",
            onClick = {},
            suffix = {
                Text(
                    text = "跟随系统",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        ),
    )

    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(settings) {
            SettingItem(
                title = it.title,
                subTitle = it.subTitle,
                onClick = it.onClick,
                suffix = it.suffix,
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}