package cn.bit101.android.features.setting.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavController
import cn.bit101.android.features.common.MainController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingPage(
    mainController: MainController,
    title: String,
    navController: NavController,
    content: @Composable () -> Unit,
) {
    val topAppBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    // 沉浸式状态栏
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar (
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                scrollBehavior = topAppBarBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if(navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                mainController.popBackStack()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content()
        }
    }
}