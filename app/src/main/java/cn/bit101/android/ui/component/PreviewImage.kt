package cn.bit101.android.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.bit101.api.model.common.Image
import coil.compose.AsyncImage

@Composable
fun PreviewImage(
    image: Image,
    onClick: () -> Unit = {},
    size: Dp = 150.dp,
) {
    AsyncImage(
        modifier = Modifier
            .padding(end = 5.dp)
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },

        contentScale = ContentScale.Crop,
        model = image.lowUrl,
        contentDescription = "image",
    )
}

@Composable
fun PreviewImages(
    modifier: Modifier = Modifier,
    images: List<Image>,
    maxCount: Int? = null,
    size: Dp = 150.dp,

    onClick: (Int) -> Unit = {},
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        images.forEachIndexed { index, image ->
            if(maxCount == null || index < maxCount) {
                item(index) {
                    PreviewImage(
                        image = image,
                        onClick = { onClick(index) },
                        size = size,
                    )
                    Spacer(modifier = Modifier.padding(2.dp))
                }
            } else if(index > maxCount) {
                return@forEachIndexed
            }
            else {
                // 省略号
                item(index) {
                    Text(
                        modifier = Modifier.padding(10.dp),
                        text = "...",
                    )
                }
            }
        }
    }
}