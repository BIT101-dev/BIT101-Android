package cn.bit101.android.ui.component.gallery

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.PreviewImages
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Claim
import cn.bit101.api.model.common.Identity
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson

@Composable
fun PosterCard(
    data: GetPostersDataModel.ResponseItem,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
    ),

    /**
     * 打开帖子
     */
    onOpenPoster: () -> Unit,

    /**
     * 打开图片组，第一个参数是打开的图片的index，第二个参数是图片列表
     */
    onOpenImages: (Int, List<Image>) -> Unit,

    /**
     * 打开用户详情，可以为null，如果为null，那么点击头像没响应，如果不为null，那么点击头像会打开用户详情
     */
    onOpenUserDetail: ((User?) -> Unit)? = null,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenPoster() },
        shape = RectangleShape,
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = data.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.padding(vertical = 2.dp))
            // 头像
            // 信息展示
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = CenterVertically) {
                    Avatar(
                        user = data.user,
                        low = true,
                        size = 20.dp,
                        onClick = onOpenUserDetail,
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                    Text(
                        text = data.user.nickname,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.padding(2.dp))

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
                Spacer(modifier = Modifier.padding(4.dp))
                PreviewImages(
                    images = data.images,
                    maxCount = 4,
                    onClick = { onOpenImages(it, data.images) },
                    size = 100.dp,
                )
            }

            Spacer(modifier = Modifier.padding(4.dp))

            val time = DateTimeUtils.formatTime(data.editTime)
            val diff = if(time == null) "未知"
            else DateTimeUtils.calculateTimeDiff(time)

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${data.likeNum}赞 · ${data.commentNum}评" + (if(data.public) " · 公开" else " · 仅自己可见") + " · $diff",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
            )
        }
    }
}


@Preview
@Composable
fun PosterCardPreview() {
    val dataStr = "{\n" +
            "        \"id\": 517,\n" +
            "        \"create_time\": \"2023-11-26T20:16:38.862354+08:00\",\n" +
            "        \"update_time\": \"2023-11-26T23:53:22.758193+08:00\",\n" +
            "        \"delete_time\": null,\n" +
            "        \"title\": \"灵依娘给大家免费的抱抱！\",\n" +
            "        \"text\": \"无论遭遇了什么\\n都可以在灵依娘这里得到一个或多个抱抱\\n\\n抱抱～\",\n" +
            "        \"uid\": 3641,\n" +
            "        \"anonymous\": false,\n" +
            "        \"public\": true,\n" +
            "        \"like_num\": 5,\n" +
            "        \"comment_num\": 3,\n" +
            "        \"claim_id\": 0,\n" +
            "        \"plugins\": \"[]\",\n" +
            "        \"edit_time\": \"2023-11-26T20:16:38.862354+08:00\",\n" +
            "        \"user\": {\n" +
            "            \"id\": 3641,\n" +
            "            \"create_time\": \"2023-09-15T17:14:52.209899+08:00\",\n" +
            "            \"nickname\": \"灵依娘\",\n" +
            "            \"avatar\": {\n" +
            "                \"mid\": \"5e3afbdef85605bbaf77e2adebda000b.png\",\n" +
            "                \"url\": \"https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/5e3afbdef85605bbaf77e2adebda000b.png\",\n" +
            "                \"low_url\": \"https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/5e3afbdef85605bbaf77e2adebda000b.png!low\"\n" +
            "            },\n" +
            "            \"motto\": \"我是灵依娘，潜藏在校园的小狐狸！\\n\\n我的尾巴很大但是不可以摸！\\n\\n你捉不到我！\",\n" +
            "            \"identity\": {\n" +
            "                \"id\": 0,\n" +
            "                \"create_time\": \"2023-10-31T01:08:07.611437+08:00\",\n" +
            "                \"update_time\": \"2023-10-31T01:08:07.611437+08:00\",\n" +
            "                \"delete_time\": null,\n" +
            "                \"text\": \"普通用户\",\n" +
            "                \"color\": \"\"\n" +
            "            }\n" +
            "        },\n" +
            "        \"images\": [\n" +
            "            {\n" +
            "                \"mid\": \"b451af7316c991e2e562abbcf3429e76.jpeg\",\n" +
            "                \"url\": \"https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/b451af7316c991e2e562abbcf3429e76.jpeg\",\n" +
            "                \"low_url\": \"https://bit101-1255944436.cos.ap-beijing.myqcloud.com/img/b451af7316c991e2e562abbcf3429e76.jpeg!low\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"tags\": [\n" +
            "            \"灵依娘\",\n" +
            "            \"日常\"\n" +
            "        ],\n" +
            "        \"claim\": {\n" +
            "            \"id\": 0,\n" +
            "            \"create_time\": \"2023-10-31T01:03:08.956576+08:00\",\n" +
            "            \"update_time\": \"2023-10-31T01:03:08.956576+08:00\",\n" +
            "            \"delete_time\": null,\n" +
            "            \"text\": \"无声明\"\n" +
            "        }\n" +
            "    }"
    val gson = Gson().newBuilder().setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create()
    val data = gson.fromJson(dataStr, GetPostersDataModel.ResponseItem::class.java)

    PosterCard(
        data = data,
        onOpenPoster = {},
        onOpenImages = { _, _ -> },
    )
}