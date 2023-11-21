package cn.bit101.android.ui.setting2.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.bit101.android.App
import cn.bit101.android.BuildConfig
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting2.component.SettingItem
import cn.bit101.android.ui.setting2.component.SettingItemData
import cn.bit101.android.ui.setting2.component.itemsGroup
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun AboutPage(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection
) {
    val logo = App.context.applicationInfo.loadIcon(App.context.packageManager)
    val painter = rememberDrawablePainter(logo)

    val versionItems = listOf(
        SettingItemData(
            title = "当前版本",
            onClick = {},
            suffix = {
                Text(
                    text = BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        ),
        SettingItemData(
            title = "自动检查更新",
            subTitle = "在启动时自动检查更新",
            onClick = {},
            suffix = {
                Switch(checked = false, onCheckedChange = {})
            }
        )
    )

    val contactItems = listOf(
        SettingItemData(
            title = "GitHub",
            suffix = {
                Text(
                    text = "BIT101-Android",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {}
        ),
        SettingItemData(
            title = "QQ群",
            suffix = {
                Text(
                    text = "726965926",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {}
        ),
        SettingItemData(
            title = "邮箱",
            suffix = {
                Text(
                    text = "bit101@qq.com",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            },
            onClick = {}
        ),
    )

    val aboutAppItems = listOf(
        SettingItemData(
            title = "开源声明",
            onClick = {},
        ),
        SettingItemData(
            title = "关于BIT101-Android",
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
        item("logo") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Image(
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painter,
                    contentDescription = "logo",
                )
                Spacer(modifier = Modifier.padding(2.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "BIT101",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                )
            }
            Spacer(modifier = Modifier.padding(16.dp))
        }

        itemsGroup(
            title = "版本信息",
            items = versionItems,
        )

        itemsGroup(
            title = "联系我们",
            items = contactItems,
        )

        itemsGroup(
            title = "关于本APP",
            items = aboutAppItems,
        )
    }
}