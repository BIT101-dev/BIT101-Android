package cn.bit101.android.ui.gallery

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.gallery.image.ImageScreen
import cn.bit101.android.ui.gallery.index.GalleryIndexScreen
import cn.bit101.android.ui.gallery.message.MessageScreen
import cn.bit101.android.ui.gallery.postedit.PostEditScreen
import cn.bit101.android.ui.gallery.poster.PosterScreen
import cn.bit101.android.ui.gallery.report.ReportScreen
import cn.bit101.android.ui.gallery.user.UserScreen
import cn.bit101.api.model.common.Image
import java.net.URLDecoder
import java.net.URLEncoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    mainController: MainController,
    vm: GalleryViewModel = hiltViewModel(),
) {
    val galleryController = MainController(
        scope = mainController.scope,
        navController = rememberNavController(),
        snackbarHostState = mainController.snackbarHostState,
    )

    val currentBackStackEntry by galleryController.navController.currentBackStackEntryFlow.collectAsState(initial = null)

    val onOpenImage: (Image) -> Unit = { image ->
        val encodedUrl = URLEncoder.encode(image.url, "UTF-8")
        galleryController.navController.navigate("image/$encodedUrl")
    }

    val onOpenImages: (Int, List<Image>) -> Unit = { index, images ->
        val encodedUrls = images.map { URLEncoder.encode(it.url, "UTF-8") }.joinToString(",")
        Log.i("GalleryScreen", "images/$encodedUrls/$index")

        galleryController.navController.navigate("images/$encodedUrls/$index")
    }

    val unreadCount by vm.unreadMessageCountLiveData.observeAsState()

    SideEffect {
        vm.getUnreadMessageCount()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val title = when(currentBackStackEntry?.destination?.route) {
                        "index" -> "话廊 · 主页"
                        "poster/{id}" -> "话廊 · 帖子"
                        "image/{url}" -> "话廊 · 图片"
                        "images/{urls}/{index}" -> "话廊 · 图片组"
                        "post" -> "话廊 · 张贴"
                        "edit/{id}" -> "话廊 · 编辑"
                        "report/{type}/{id}" -> "话廊 · 举报"
                        "user/{id}" -> {
                            val id = currentBackStackEntry?.arguments?.getLong("id") ?: 0L
                            if(id == 0L) "话廊 · 我的"
                            else "话廊 · 用户"
                        }
                        "message" -> "话廊 · 消息"
                        else -> "话廊"
                    }

                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            galleryController.navController.popBackStack()
                        },
                        enabled = currentBackStackEntry?.destination?.route != "index",
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    if(currentBackStackEntry?.destination?.route == "index") {
                        Box {
                            if(unreadCount != null && unreadCount!! > 0) {
                                Badge(modifier = Modifier.align(Alignment.TopEnd)) {
                                    Text(text = unreadCount.toString())
                                }
                            }
                            IconButton(
                                onClick = { galleryController.navController.navigate("message") }
                            ) {
                                Icon(imageVector = Icons.Rounded.Mail, contentDescription = null)
                            }
                        }

                        IconButton(
                            onClick = { galleryController.navController.navigate("user/0") }
                        ) {
                            Icon(imageVector = Icons.Rounded.AccountCircle, contentDescription = null)
                        }

                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues),
            navController = galleryController.navController,
            startDestination = "index",
        ) {


            composable(
                route = "index",
            ) {
                GalleryIndexScreen(
                    mainController = galleryController,
                    onOpenImages = onOpenImages,
                )
            }
            composable(
                route = "poster/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                PosterScreen(
                    mainController = galleryController,
                    id = it.arguments?.getLong("id") ?: 0L,
                    onOpenImage = onOpenImage,
                    onOpenImages = onOpenImages,
                )
            }

            composable(
                route = "image/{url}",
                arguments = listOf(
                    navArgument("url") { type = NavType.StringType },
                ),
            ) {
                val encodedUrl = it.arguments?.getString("url") ?: ""
                val url = URLDecoder.decode(encodedUrl, "UTF-8")
                ImageScreen(url)
            }

            composable("post") {
                PostEditScreen(
                    mainController = galleryController,
                    onOpenImage = onOpenImage,
                )
            }

            composable(
                route = "images/{urls}/{index}",
                arguments = listOf(
                    navArgument("urls") { type = NavType.StringType },
                    navArgument("index") { type = NavType.IntType },
                ),
            ) {
                val urls = (it.arguments?.getString("urls")?.split(",") ?: emptyList()).map { encodedUrl ->
                    URLDecoder.decode(encodedUrl, "UTF-8")
                }
                val index = it.arguments?.getInt("index") ?: 0
                ImageScreen(
                    urls = urls,
                    index = index
                )
            }

            composable("post") {
                PostEditScreen(
                    mainController = galleryController,
                    onOpenImage = onOpenImage,
                )
            }

            composable(
                route = "edit/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val id = it.arguments?.getLong("id") ?: 0L
                PostEditScreen(
                    mainController = galleryController,
                    id = id,
                    onOpenImage = onOpenImage,
                )
            }

            composable(
                route = "report/{type}/{id}",
                arguments = listOf(
                    navArgument("type") { type = NavType.StringType },
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val type = it.arguments?.getString("type") ?: ""
                val id = it.arguments?.getLong("id") ?: 0L

                ReportScreen(
                    mainController = galleryController,
                    objType = type,
                    id = id,
                )
            }

            composable(
                route = "user/{id}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType },
                ),
            ) {
                val id = it.arguments?.getLong("id") ?: 0L
                 UserScreen(
                     mainController = galleryController,
                     id = id,
                     onOpenImage = onOpenImage,
                     onOpenImages = onOpenImages,
                 )
            }

            composable(
                route = "message",
            ) {
                MessageScreen(mainController = galleryController)
            }
        }
    }
}
