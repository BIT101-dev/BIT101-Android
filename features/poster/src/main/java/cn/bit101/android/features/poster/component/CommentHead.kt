package cn.bit101.android.features.poster.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Reorder
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import cn.bit101.android.features.poster.CommentsOrderWithName
import cn.bit101.android.features.poster.CommentsOrdersWithName

@Composable
internal fun CommentHeader(
    title: String,
    commentsOrder: CommentsOrderWithName,
    onSelectCommentsOrder: (CommentsOrderWithName) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        var expanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier.clickable { expanded = true },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val height = LocalDensity.current.run {
                MaterialTheme.typography.titleSmall.lineHeight.toDp() * 0.75f
            }
            Icon(
                modifier = Modifier.size(height),
                imageVector = Icons.Outlined.Reorder,
                contentDescription = commentsOrder.name,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = commentsOrder.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                CommentsOrdersWithName.all.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            onSelectCommentsOrder(it)
                            expanded = false
                        },
                        leadingIcon = {
                            if (commentsOrder.value == it.value) {
                                Icon(
                                    imageVector = Icons.Sharp.Check,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.padding(6.dp))
    Divider(thickness = 0.3.dp)
    Spacer(modifier = Modifier.padding(6.dp))
}