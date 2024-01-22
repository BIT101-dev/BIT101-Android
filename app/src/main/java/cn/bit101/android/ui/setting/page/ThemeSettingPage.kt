package cn.bit101.android.ui.setting.page

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.config.setting.base.DarkThemeMode
import cn.bit101.android.config.setting.base.toDarkThemeData
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.setting.SettingItemData
import cn.bit101.android.ui.component.setting.SettingsColumn
import cn.bit101.android.ui.component.setting.SettingsGroup
import cn.bit101.android.ui.setting.viewmodel.ThemeViewModel

@Composable
private fun ThemeSettingPageContent(
    dynamic: Boolean,
    darkMode: String,
    rotate: Boolean,

    onChangeDynamic: (Boolean) -> Unit,
    onChangeRotate: (Boolean) -> Unit,

    onOpenDarkModeDialog: () -> Unit,
) {
    val settings = listOf(
        SettingItemData.Switch(
            title = "动态适配系统主题",
            subTitle = "需要Android 12及以上",
            onClick = onChangeDynamic,
            checked = dynamic
        ),
        SettingItemData.Button(
            title = "暗黑模式",
            subTitle = "跟随系统，亮色，暗色",
            onClick = onOpenDarkModeDialog,
            text = darkMode,
        ),
        SettingItemData.Switch(
            title = "自动旋转",
            subTitle = "设置是否自动旋转",
            onClick = onChangeRotate,
            checked = rotate
        )
    )

    SettingsColumn {
        SettingsGroup(
            items = settings,
        )
    }
}

@Composable
private fun DarkModeDialog(
    darkMode: DarkThemeMode,

    onChangeDarkMode: (DarkThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {

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
                DarkThemeMode.allModes.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = (item == darkMode),
                                onClick = {
                                    onChangeDarkMode(item)
                                    onDismiss()
                                },
                                role = Role.RadioButton
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (item == darkMode),
                            onClick = null
                        )
                        Text(
                            text = item.toDarkThemeData().name,
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
    vm: ThemeViewModel = hiltViewModel()
) {

    val dynamic by vm.dynamicTheme.flow.collectAsState(initial = false)
    val darkMode by vm.darkThemeMode.flow.collectAsState(initial = DarkThemeMode.System)
    val rotate by vm.autoRotate.flow.collectAsState(initial = false)

    var showDarkModeDialog by rememberSaveable { mutableStateOf(false) }

    ThemeSettingPageContent(
        dynamic = dynamic,
        darkMode = darkMode.toDarkThemeData().name,
        rotate = rotate,

        onChangeDynamic = vm::setDynamicTheme,
        onChangeRotate = vm::setAutoRotate,
        onOpenDarkModeDialog = { showDarkModeDialog = true },
    )

    if (showDarkModeDialog) {
        DarkModeDialog(
            darkMode = darkMode,
            onChangeDarkMode = vm::setDarkThemeMode,
            onDismiss = { showDarkModeDialog = false }
        )
    }
}