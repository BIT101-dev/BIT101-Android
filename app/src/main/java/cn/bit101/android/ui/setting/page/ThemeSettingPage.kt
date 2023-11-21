package cn.bit101.android.ui.setting.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.bit101.android.datastore.SettingDataStore
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.setting.component.SettingItem
import cn.bit101.android.ui.setting.component.SettingItemData
import cn.bit101.api.model.common.NameAndValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingPageContent(
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,

    dynamic: Boolean,
    darkMode: String,
    rotate: Boolean,

    onChangeDynamic: (Boolean) -> Unit,
    onChangeRotate: (Boolean) -> Unit,

    onOpenDarkModeDialog: () -> Unit,
) {
    val settings = listOf(
        SettingItemData(
            title = "动态适配系统主题",
            subTitle = "需要Android 12及以上",
            onClick = { onChangeDynamic(!dynamic) },
            suffix = {
                Switch(checked = dynamic, onCheckedChange = onChangeDynamic)
            }
        ),
        SettingItemData(
            title = "暗黑模式",
            subTitle = "跟随系统，亮色，暗色",
            onClick = onOpenDarkModeDialog,
            suffix = {
                Text(
                    text = darkMode,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        ),
        SettingItemData(
            title = "自动旋转",
            subTitle = "设置是否自动旋转",
            onClick = { onChangeRotate(!rotate) },
            suffix = {
                Switch(checked = rotate, onCheckedChange = onChangeRotate)
            }
        )
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

@Composable
fun DarkModeDialog(
    darkMode: String,

    onChangeDarkMode: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val nameAndValues = listOf(
        NameAndValue("跟随系统", "system"),
        NameAndValue("亮色", "light"),
        NameAndValue("暗色", "dark"),
    )

    val selectedOption = if(darkMode in nameAndValues.map { it.value }) darkMode else "system"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text(text = "暗黑模式") },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(scrollState)
            ) {
                nameAndValues.forEach { nameValue ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = (nameValue.value == selectedOption),
                                onClick = {
                                    onChangeDarkMode(nameValue.value)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (nameValue.value == selectedOption),
                            onClick = null
                        )
                        Text(
                            text = nameValue.name,
                            modifier = Modifier.padding(start = 10.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun ThemeSettingPage(
    mainController: MainController,
    paddingValues: PaddingValues,
    nestedScrollConnection: NestedScrollConnection,
) {

    val dynamic by SettingDataStore.settingDynamicTheme.flow.collectAsState(initial = false)
    val darkMode by SettingDataStore.settingDarkTheme.flow.collectAsState(initial = null)
    val rotate by SettingDataStore.settingRotate.flow.collectAsState(initial = false)

    var showDarkModeDialog by rememberSaveable { mutableStateOf(false) }

    val darkModeCh = when(darkMode) {
        null -> ""
        "light" -> "亮色"
        "dark" -> "暗色"
        else -> "跟随系统"
    }

    ThemeSettingPageContent(
        paddingValues = paddingValues,
        nestedScrollConnection = nestedScrollConnection,

        dynamic = dynamic,
        darkMode = darkModeCh,
        rotate = rotate,

        onChangeDynamic = {
            MainScope().launch(Dispatchers.IO) { SettingDataStore.settingDynamicTheme.set(it) }
        },
        onChangeRotate = {
            MainScope().launch(Dispatchers.IO) { SettingDataStore.settingRotate.set(it) }
        },
        onOpenDarkModeDialog = { showDarkModeDialog = true },
    )

    if (showDarkModeDialog) {
        DarkModeDialog(
            darkMode = darkMode ?: "",
            onChangeDarkMode = {
                MainScope().launch(Dispatchers.IO) { SettingDataStore.settingDarkTheme.set(it) }
            },
            onDismiss = { showDarkModeDialog = false }
        )
    }
}