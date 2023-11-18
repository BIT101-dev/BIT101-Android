package cn.bit101.android.ui.gallery.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.PreviewImages
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterCard(
    data: GetPostersDataModel.ResponseItem,
    colors: CardColors = CardDefaults.cardColors(),

    onOpenPoster: (Long) -> Unit,
    onOpenImage: (Image) -> Unit = {},
    onOpenUserDetail: ((User?) -> Unit)? = null,
) {
    Card(
        onClick = {
            onOpenPoster(data.id)
        },
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        colors = colors,
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(),

        ) {
            // 头像
            // 信息展示
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                ) {
                    Avatar(
                        user = data.user,
                        low = true,
                        size = 45.dp,
                        onClick = onOpenUserDetail,
                    )

                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp),
                    ) {
                        Text(
                            text = data.user.nickname,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.padding(vertical = 1.dp))
                        val time = DateTimeUtils.formatTime(data.editTime)
                        val diff = if(time == null) "未知"
                        else DateTimeUtils.calculateTimeDiff(time)

                        Text(
                            text = diff,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                if(data.claim.id != 0) {
                    SuggestionChip(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        shape = CircleShape,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "claim",
                            )
                        },
                        onClick = {},
                        label = {
                            Text(
                                text = data.claim.text,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = data.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            if(data.text.isNotEmpty()) {
                Text(
                    text = data.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 展示图片
            if(data.images.isNotEmpty()) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                PreviewImages(
                    images = data.images,
                    maxCount = 4,
                    onClick = onOpenImage,
                    size = 100.dp,
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${data.likeNum}赞 | ${data.commentNum}评" + if(data.public) " | 公开" else " | 仅自己可见",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.End
            )
        }
    }
}