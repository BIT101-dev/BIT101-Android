package cn.bit101.android.ui.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.MainController


@Composable
fun IndexSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.padding(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
    }
}


data class SubSettingItem(
    val title: String,
    val subTitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun SettingIndexPage(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,

    onOpenAboutPage: () -> Unit = {},
    onOpenPagesSettingPage: () -> Unit = {},
    onOpenThemeSettingPage: () -> Unit = {},
    onOpenCalendarSettingPage: () -> Unit = {},
    onOpenDDLSettingPage: () -> Unit = {},
) {
    val subSettings = listOf(
        SubSettingItem(
            title = "页面设置",
            subTitle = "主页，页面顺序",
            icon = Icons.Outlined.Dashboard,
            onClick = onOpenPagesSettingPage
        ),
        SubSettingItem(
            title = "外观设置",
            subTitle = "暗黑模式，动态主题",
            icon = Icons.Outlined.ColorLens,
            onClick = onOpenThemeSettingPage
        ),
        SubSettingItem(
            title = "课程表设置",
            subTitle = "显示周六/日，高亮今日，时间表",
            icon = Icons.Outlined.EditCalendar,
            onClick = onOpenCalendarSettingPage
        ),
        SubSettingItem(
            title = "DDL设置",
            subTitle = "变色天数，滞留天数",
            icon = Icons.AutoMirrored.Outlined.EventNote,
            onClick = onOpenDDLSettingPage
        ),
        SubSettingItem(
            title = "关于",
            subTitle = "关于BIT101-Android",
            icon = Icons.Outlined.Info,
            onClick = onOpenAboutPage
        )
    )


    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(subSettings) {
            IndexSettingItem(
                title = it.title,
                subTitle = it.subTitle,
                icon = {
                    Icon(
                        imageVector = it.icon,
                        contentDescription = "关于"
                    )
                },
                onClick = it.onClick
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }
    }
}