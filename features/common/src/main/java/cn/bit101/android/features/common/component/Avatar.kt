package cn.bit101.android.features.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.common.R
import cn.bit101.api.model.common.User
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers

@Composable
fun Avatar(
    size: Dp = 50.dp,
    user: User? = null,
    low: Boolean? = true,
    onClick: ((User?) -> Unit)? = null,
    showIdentity: Boolean = true
) {
    val ctx = LocalContext.current

    // 头像
    // 默认头像为APP图标
    val icon = ctx.applicationInfo.loadIcon(ctx.packageManager)
    val painter = rememberDrawablePainter(icon)
    Box {

        val offset = size / 20

        Box {
            var modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color(ctx.getColor(R.color.ic_launcher_background)))

            if(onClick != null) {
                modifier = modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = { onClick(user) })
                }
            }

            AsyncImage(
                modifier = modifier,
                contentScale = ContentScale.FillBounds,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if(low == true) user?.avatar?.lowUrl else user?.avatar?.url)
                    .crossfade(true)
                    .dispatcher(Dispatchers.IO)
                    .build(),
                placeholder = painter,
                error = painter,
                fallback = painter,
                contentDescription = "avatar"
            )
        }

        if(user != null && user.identity.id != 0 && showIdentity) {
            val colorStr = user.identity.color
            val color = Color(android.graphics.Color.parseColor(colorStr))

            Icon(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = offset, y = offset)
                    .size(size * 2 / 5),
                imageVector = Icons.Rounded.Verified,
                contentDescription = "认证",
                tint = color,
            )
        }
    }
}