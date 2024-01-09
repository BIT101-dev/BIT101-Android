package cn.bit101.android.ui.component.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun BasicSettingItemCard(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ),
    content: @Composable (ColumnScope.() -> Unit)
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            colors = colors,
            content = content
        )
    }
}

@Composable
private fun SettingItemCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String? = null,
    onClick: (() -> Unit)? = null,
    component: @Composable (ColumnScope.() -> Unit)? = null,
    suffix: @Composable (RowScope.() -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )
) {
    val content: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                if (suffix != null) {
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row {
                        suffix()
                    }
                }

            }
            if(component != null) {
                Spacer(modifier = Modifier.padding(4.dp))
                component()
            }
        }
    }

    BasicSettingItemCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        content = content,
    )
}

@Composable
private fun IndexSettingItem(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    icon: @Composable () -> Unit = {},
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.padding(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = subTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                )
            }
        }
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

        is SettingItemData.IndexCard -> IndexSettingItem(
            title = data.title,
            subTitle = data.subTitle,
            icon = {
                Icon(
                    imageVector = data.icon,
                    contentDescription = null
                )
            },
            onClick = data.onClick
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

    data class IndexCard(
        override val title: String,
        val subTitle: String,
        val icon: ImageVector,
        val onClick: () -> Unit,
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
    subTitle: String? = null,
    items: List<SettingItemData>,
) {
    if(title != null) {
        item(titleKey ?: title) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)

        }
    }
    if(subTitle != null) {
        item {
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = subTitle,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.padding(8.dp))
        }
    } else {
        item { Spacer(modifier = Modifier.padding(8.dp)) }
    }

    items(items, { itemsKey(it) }) {
        SettingItem(data = it)
        Spacer(modifier = Modifier.padding(4.dp))
    }

    item {
        Spacer(modifier = Modifier.padding(8.dp))
    }
}