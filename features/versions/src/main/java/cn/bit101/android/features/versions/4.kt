package cn.bit101.android.features.versions

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun Version4Dialog(
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "确定")
            }
        },
        title = { Text(text = "版本1.2.0") },
        text = {
            Text(text = "由于版本1.2.0的更新，您需要重新登录。")
        }
    )
}