package cn.bit101.android.ui.component.gallery

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import cn.bit101.android.ui.MainController
import cn.bit101.android.utils.TextUtils
import cn.bit101.api.model.common.User


@OptIn(ExperimentalTextApi::class)
fun getUrl(
    layoutResult: MutableState<TextLayoutResult?>,
    annotatedText: AnnotatedString,
    pos: Offset,
): String? {
    return layoutResult.value?.let {
        val offset = it.getOffsetForPosition(pos)
        annotatedText.getUrlAnnotations(offset, offset).firstOrNull()?.item?.url
    }
}


fun getUser(
    layoutResult: MutableState<TextLayoutResult?>,
    annotatedText: AnnotatedString,
    pos: Offset,
): String? {
    return layoutResult.value?.let {
        val offset = it.getOffsetForPosition(pos)
        var user: String? = null
        annotatedText.getStringAnnotations(
            tag = "USER",
            start = offset,
            end = offset
        ).firstOrNull()?.item
    }
}

/**
 * 带有超链接和回复用户高亮显示的文本
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalTextApi::class)
@Composable
fun AnnotatedText(
    mainController: MainController,
    modifier: Modifier = Modifier,
    text: String,
    replyUser: User? = null,
    style: TextStyle = TextStyle.Default,
    softWrap: Boolean = true,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val ctx = LocalContext.current

    // 识别链接并自动添加超链接，点击链接自动跳转
    val annotatedText = buildAnnotatedString {
        val matches = TextUtils.findUrl(text)
        var lastEnd = 0

        if(replyUser != null && replyUser.id != 0) {
            pushStringAnnotation(
                tag = "USER",
                annotation = replyUser.id.toString()
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                )
            ) {
                append("@${replyUser.nickname} ")
            }
            pop()
        }

        matches.forEach {
            val start = it.range.first
            val end = it.range.last + 1
            val url = it.value
            append(text.substring(lastEnd, start))
            pushUrlAnnotation(urlAnnotation = UrlAnnotation(url))
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                )
            ) { append(url) }
            pop()
            lastEnd = end
        }
        append(text.substring(lastEnd, text.length))
    }

    // 目前这里的点击事件分发有问题
    // 要的效果应该是：点击链接时触发ClickableText的点击事件，点击其他地方时不触发ClickableText的点击事件
    // 完成这个效果

    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

    BasicText(
        text = annotatedText,
        modifier = modifier
            .pointerInteropFilter {
                val pos = Offset(it.x, it.y)

                val url = getUrl(layoutResult, annotatedText, pos)
                val user = getUser(layoutResult, annotatedText, pos)
                if(url == null && user == null) false
                else {
                    when (it.action) {
                        MotionEvent.ACTION_DOWN -> {
                            Log.d("AnnotatedText", "down")
                            true
                        }
                        MotionEvent.ACTION_UP -> {
                            Log.d("AnnotatedText", "up")

                            if(url != null) {
                                if (url.startsWith("https://bit101.cn/gallery/")) {
                                    val id = url.substring("https://bit101.cn/gallery/".length).toLongOrNull()
                                    if (id != null) {
                                        mainController.navController.navigate("poster/$id")
                                    }
                                }
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                ctx.startActivity(intent)
                                true
                            } else if(user != null) {
                                val id = user.toLongOrNull()
                                if(id != null) {
                                    mainController.navController.navigate("user/$id")
                                    true
                                } else false
                            } else false
                        }
                        else -> {
                            false
                        }
                    }
                }
            },
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        onTextLayout = { layoutResult.value = it }
    )
}