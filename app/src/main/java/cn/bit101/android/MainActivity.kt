package cn.bit101.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cn.bit101.android.ui.theme.BIT101Theme
import cn.bit101.android.view.Schedule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BIT101Theme {
                MainContent()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val navController = rememberNavController()

    data class Screen(val route: String, val label: String, val icon: ImageVector)

    // 底部导航栏
    val routes = listOf(
        Screen("schedule", "日程", Icons.Rounded.Event),
        Screen("home", "墙", Icons.Rounded.Dashboard)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                routes.forEach { screen ->
                    val selected= currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                        label = { Text(text = screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                }

            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "schedule",
            Modifier.padding(it)
        ) {
            composable("schedule") {
                Schedule()
            }
            composable("home") {
                Wall()
            }
        }
    }
}

@Composable
fun Wall() {
    Text(text = "Wall")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BIT101Theme {
        MainContent()
    }
}