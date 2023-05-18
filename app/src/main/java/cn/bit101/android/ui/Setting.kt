package cn.bit101.android.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.bit101.android.MainController
import cn.bit101.android.ui.component.ConfigColumn
import cn.bit101.android.ui.component.ConfigItem
import cn.bit101.android.viewmodel.SettingViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author flwfdd
 * @date 2023/5/18 下午2:24
 * @description _(:з」∠)_
 */

@Composable
fun Setting(mainController: MainController, vm: SettingViewModel = viewModel()) {
    ConfigColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        items = listOf(
            ConfigItem.Button(
                title = "登陆管理",
                onClick = {
                    mainController.route("login")
                }
            ),
            ConfigItem.Switch(
                title = "自动旋转",
                checked = vm.rotateFlow.collectAsState(initial = false).value,
                onCheckedChange = {
                    MainScope().launch {
                        vm.setRotate(it)
                    }
                }
            ),
            ConfigItem.Switch(
                title = "动态适配系统主题",
                checked = vm.dynamicThemeFlow.collectAsState(initial = false).value,
                onCheckedChange = {
                    MainScope().launch {
                        vm.setDynamicTheme(it)
                    }
                }
            ),
            ConfigItem.Switch(
                title = "禁用暗黑模式",
                checked = vm.disableDarkThemeFlow.collectAsState(initial = false).value,
                onCheckedChange = {
                    MainScope().launch {
                        vm.setDisableDarkTheme(it)
                    }
                }
            ),
        ))
}