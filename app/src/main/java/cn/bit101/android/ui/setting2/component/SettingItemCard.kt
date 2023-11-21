package cn.bit101.android.ui.setting2.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingItemCardContent(
    title: String,
    enabled: Boolean = true,
    subTitle: String? = null,
    suffix: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.7f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.6f),
                    fontWeight = FontWeight.Bold
                )
            )

            if (subTitle != null) {
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.6f else 0.4f)
                    )
                )
            }
        }
        Row {
            suffix()
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    enable: Boolean = true,
    subTitle: String? = null,
    onClick: (() -> Unit)? = null,
    suffix: @Composable RowScope.() -> Unit = {},
) {
    if(onClick != null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            enabled = enable,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            SettingItemCardContent(
                title = title,
                enabled = enable,
                subTitle = subTitle,
                suffix = suffix,
            )
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            SettingItemCardContent(
                title = title,
                enabled = enable,
                subTitle = subTitle,
                suffix = suffix,
            )
        }
    }
}


data class SettingItemData(
    val title: String,
    val enable: Boolean = true,
    val subTitle: String? = null,
    val onClick: () -> Unit = {},
    val suffix: @Composable RowScope.() -> Unit = {},
)


fun LazyListScope.itemsGroup(
    titleKey: Any? = null,
    itemsKey: (SettingItemData) -> Any = { it.title },
    title: String? = null,
    items: List<SettingItemData>,
) {
    if(title != null) {
        item(titleKey ?: title) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(8.dp))
        }
    }

    items(items, { itemsKey(it) }) {
        SettingItem(
            title = it.title,
            enable = it.enable,
            subTitle = it.subTitle,
            onClick = it.onClick,
            suffix = it.suffix,
        )
        Spacer(modifier = Modifier.padding(4.dp))
    }

    item {
        Spacer(modifier = Modifier.padding(8.dp))
    }
}