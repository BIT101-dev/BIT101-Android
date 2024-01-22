package cn.bit101.android.features.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.theme.BIT101Theme

/**
 * @author flwfdd
 * @date 2023/4/12 16:28
 * @description _(:з」∠)_
 */

sealed class ConfigItem {
    data class Button(
        val title: String = "",
        val content: String = "",
        val onClick: () -> Unit = {}
    ) : ConfigItem()

    data class Switch(
        val title: String = "",
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit = {}
    ) : ConfigItem()
}

@Composable
fun ConfigColumn(modifier: Modifier, scrollable: Boolean = true, items: List<ConfigItem>) {
    val md = if (scrollable) {
        val scrollState = rememberScrollState()
        Modifier.verticalScroll(scrollState)
    } else Modifier
    Column(
        // padding如果在verticalScroll之前会导致scroll over的阴影范围不对
        modifier = md.then(modifier)
    ) {
        items.forEach {
            when (it) {
                is ConfigItem.Button -> ConfigButton(
                    title = it.title,
                    content = it.content,
                    onClick = it.onClick
                )

                is ConfigItem.Switch -> ConfigSwitch(
                    title = it.title,
                    checked = it.checked,
                    onCheckedChange = it.onCheckedChange
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun ConfigButton(title: String, content: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        onClick = { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = content,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Rounded.ArrowRight,
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                contentDescription = "select term",
            )
        }
    }
}

@Composable
fun ConfigSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Switch(
                checked = checked,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    checkedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                ),
                modifier = Modifier.scale(0.75f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigColumnPreview() {
    BIT101Theme {
        var state by remember { mutableStateOf(true) }
        ConfigColumn(
            modifier = Modifier
                .fillMaxSize(),
            items = listOf(
                ConfigItem.Button(
                    title = "Button",
                    content = "Content",
                    onClick = {}
                ),
                ConfigItem.Switch(
                    title = "Switch",
                    checked = !state,
                    onCheckedChange = { state = !it }
                ),
                ConfigItem.Switch(
                    title = "Switch",
                    checked = state,
                    onCheckedChange = { state = it }
                )
            )
        )
    }

}