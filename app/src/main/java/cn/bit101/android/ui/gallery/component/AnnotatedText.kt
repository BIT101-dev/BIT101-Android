package cn.bit101.android.ui.gallery.component

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import cn.bit101.android.ui.MainController
import cn.bit101.android.utils.TextUtils

@Composable
fun AnnotatedText(
    mainController: MainController,
    modifier: Modifier = Modifier,
    text: String,
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
        matches.forEach {
            val start = it.range.first
            val end = it.range.last + 1
            val url = it.value
            append(text.substring(lastEnd, start))
            pushStringAnnotation(
                tag = "URL",
                annotation = url
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                )
            ) {
                append(url)
            }
            pop()
            lastEnd = end
        }
        append(text.substring(lastEnd, text.length))
    }

    ClickableText(
        modifier = modifier,
        onClick = { offset ->
            var url: String? = null
            annotatedText.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).forEach {
                if(url == null || url!!.length < it.item.length) url = it.item
            }
            url?.let {
                if (it.startsWith("https://bit101.cn/gallery/")) {
                    val id = it.substring("https://bit101.cn/gallery/".length).toLongOrNull()

                    if (id != null) {
                        mainController.navController.navigate("poster/$id")
                        return@ClickableText
                    }
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                ctx.startActivity(intent)
            }
        },
        text = annotatedText,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
    )
}