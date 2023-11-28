package cn.bit101.android.ui.component

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.android.App
import cn.bit101.android.R
import cn.bit101.api.model.common.Avatar
import cn.bit101.api.model.common.Identity
import cn.bit101.api.model.common.User
import coil.compose.AsyncImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun Avatar(
    size: Dp = 50.dp,
    user: User? = null,
    low: Boolean? = true,
    onClick: ((User?) -> Unit)? = null,
    showIdentity: Boolean = true
) {
    // 头像
    // 默认头像为APP图标
//    val icon = App.context.applicationInfo.loadIcon(App.context.packageManager)
    val icon: Drawable? = null
    val painter = rememberDrawablePainter(icon)
    Box {

        val offset = size / 10

        Box(modifier = Modifier.padding(offset)) {
            var modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.Black)
//                .background(Color(App.context.getColor(R.color.ic_launcher_background)))

            if(onClick != null) {
                modifier = modifier.clickable {
                    onClick(user)
                }
            }

            AsyncImage(
                modifier = modifier,
                contentScale = ContentScale.FillBounds,
                model = if(low == true) user?.avatar?.lowUrl else user?.avatar?.url,
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
                    .size(size * 2 / 5),
                imageVector = Icons.Rounded.Verified,
                contentDescription = "认证",
                tint = color,
            )
        }
    }
}