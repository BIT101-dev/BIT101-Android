package cn.bit101.android.ui.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cn.bit101.android.database.DDLScheduleEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author flwfdd
 * @date 13/05/2023 21:13
 * @description _(:з」∠)_
 */

@Composable
fun DDLScheduleDetailDialog(event: DDLScheduleEntity, showDialog: MutableState<Boolean>) {
    AlertDialog(
        modifier = Modifier.fillMaxSize(0.9f),
        onDismissRequest = {
            showDialog.value = false
        },
        title = {
            Text(text = event.title)
        },
        text = {
            val scrollState = rememberScrollState()
            SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                    Item(
                        title = "DDL：",
                        content = event.time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
                    Item(title = "详情：", content = event.text)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showDialog.value = false
                }
            ) {
                Text("关闭")
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
}

@Composable
private fun Item(title: String, content: String) {
    Spacer(modifier = Modifier.height(10.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DDLDetailPreview() {
    val show = remember { mutableStateOf(false) }
    DDLScheduleDetailDialog(
        DDLScheduleEntity(
            0,
            "test",
            "test",
            "test",
            "test\n\nfdsfaad\nfdsaf\nfsfds\nfdsafn\nfsaddddd范德萨啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊法大赛",
            LocalDateTime.now(),
            false,
        ),
        show
    )
}

