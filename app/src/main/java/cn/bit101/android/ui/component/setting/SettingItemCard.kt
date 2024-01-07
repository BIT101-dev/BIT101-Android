package cn.bit101.android.ui.component.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.rounded.ArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun SettingItemCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    onClick: (() -> Unit)? = null,
    suffix: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {


            Column(modifier = Modifier.fillMaxWidth(if(suffix == null) 1.0f else 0.7f)) {
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
                if (suffix != null) {
                    suffix()
                }
            }
        }
    }
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            content = content
        )
    }
}

@Composable
fun SettingItem(
    data: SettingItemData,
) {
    when(data) {
        is SettingItemData.Custom -> SettingItemCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = data.onClick,
            enabled = data.enable,
            title = data.title,
            subTitle = data.subTitle,
            suffix = data.suffix,
        )

        is SettingItemData.Button -> SettingItemCard(
            title = data.title,
            subTitle = data.subTitle,
            onClick = data.onClick,
            enabled = data.enable,
        )

        is SettingItemData.ButtonWithSuffixText -> SettingItemCard(
            title = data.title,
            subTitle = data.subTitle,
            onClick = data.onClick,
            enabled = data.enable,
            suffix = {
                Text(
                    text = data.text,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        )

        is SettingItemData.Card -> SettingItemCard(
            title = data.title,
            subTitle = data.subTitle
        )

        is SettingItemData.Switch -> SettingItemCard(
            title = data.title,
            subTitle = data.subTitle,
            onClick = { data.onClick(!data.checked) },
            enabled = data.enable,
            suffix = {
                Switch(checked = data.checked, onCheckedChange = data.onClick)
            }
        )
    }
}

sealed interface SettingItemData {
    val title: String
    data class Custom(
        override val title: String,
        val enable: Boolean = true,
        val subTitle: String? = null,
        val onClick: () -> Unit = {},
        val suffix: @Composable (RowScope.() -> Unit)? = null,
    ) : SettingItemData

    data class Button(
        override val title: String,
        val enable: Boolean = true,
        val subTitle: String? = null,
        val onClick: () -> Unit = {},
    ) : SettingItemData

    data class ButtonWithSuffixText(
        override val title: String,
        val enable: Boolean = true,
        val subTitle: String? = null,
        val onClick: () -> Unit = {},
        val text: String,
    ) : SettingItemData

    data class Switch(
        override val title: String,
        val enable: Boolean = true,
        val subTitle: String? = null,
        val checked: Boolean,
        val onClick: (Boolean) -> Unit = {},
    ) : SettingItemData

    data class Card(
        override val title: String,
        val subTitle: String? = null,
    ) : SettingItemData
}

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
        SettingItem(data = it)
        Spacer(modifier = Modifier.padding(4.dp))
    }

    item {
        Spacer(modifier = Modifier.padding(8.dp))
    }
}