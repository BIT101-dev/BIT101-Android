package cn.bit101.android.ui.component.gallery

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.bit101.android.ui.component.Avatar
import cn.bit101.android.ui.component.image.PreviewImage
import cn.bit101.android.ui.component.image.PreviewImages
import cn.bit101.android.ui.component.image.PreviewImagesWithGridLayout
import cn.bit101.android.utils.DateTimeUtils
import cn.bit101.api.model.common.Image
import cn.bit101.api.model.common.User
import cn.bit101.api.model.http.bit101.GetPostersDataModel
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson

@Composable
private fun AvatarWithName(
    data: GetPostersDataModel.ResponseItem,
    onOpenUserDetail: ((User?) -> Unit)?,
) {
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
}

@Composable
private fun Info(
    data: GetPostersDataModel.ResponseItem,
) {
    val time = DateTimeUtils.formatTime(data.editTime)
    val diff = if(time == null) "未知"
    else DateTimeUtils.calculateTimeDiff(time)

    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "${data.likeNum}赞 · ${data.commentNum}评" + (if(data.public) "" else " · 仅自己可见") + " · $diff",
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        ),
    )
}

@Composable
fun PosterCard(
    data: GetPostersDataModel.ResponseItem,

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


            if(data.images.isNotEmpty() && data.images.size <= 2) {
                val height = 128.dp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        AvatarWithName(data, onOpenUserDetail)
                        Spacer(modifier = Modifier.padding(2.dp))
                        BoxWithConstraints(modifier = Modifier.weight(1f)) {
                            val lines = LocalDensity.current.run {
                                (constraints.maxHeight / MaterialTheme.typography.bodyMedium.lineHeight.toPx()).toInt()
                            }

                            Text(
                                text = data.text,
                                maxLines = lines,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.padding(4.dp))
                        Info(data)
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    PreviewImage(
                        image = data.images[0],
                        onClick = { onOpenImages(0, data.images) },
                        size = height,
                    )
                }
            } else {
                AvatarWithName(data, onOpenUserDetail)
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = data.text,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                // 展示图片
                if(data.images.isNotEmpty() && data.images.size >= 3) {
                    val images = if(data.images.size == 3) data.images.subList(0, 3)
                    else data.images.subList(0, 4)
                    Spacer(modifier = Modifier.padding(4.dp))
                    PreviewImagesWithGridLayout(
                        images = images,
                        maxCountInEachRow = 4,
                        onClick = { onOpenImages(it, data.images) },
                    )
                }
                Spacer(modifier = Modifier.padding(4.dp))
                Info(data)
            }
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