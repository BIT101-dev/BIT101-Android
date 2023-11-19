package cn.bit101.android.ui.gallery.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.gallery.common.SimpleDataState
import cn.bit101.android.ui.gallery.common.SimpleState
import cn.bit101.api.model.common.ObjTypes
import cn.bit101.api.model.common.ReportType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTypeDropDownBox(
    selected: ReportType,
    reportTypes: List<ReportType>,

    onSelectIndex: (ReportType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            value = selected.text,
            onValueChange = {
                expanded = false
            },
            readOnly = true,
            shape = RoundedCornerShape(10.dp),
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
        )
        ExposedDropdownMenu(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onDismissRequest = { expanded = !expanded }
        ) {
            reportTypes.forEachIndexed { index, claim ->
                DropdownMenuItem(
                    text = { Text(text = claim.text) },
                    onClick = {
                        onSelectIndex(reportTypes[index])
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
fun ReportScreenContent(
    objType: String,
    id: Long,
    reportTypes: List<ReportType>,

    selectReportType: ReportType,
    text: String,
    reportState: SimpleState?,

    onSelectReportType: (ReportType) -> Unit,
    onSetText: (String) -> Unit,
    onReport: () -> Unit,
) {
    val title = when(objType) {
        ObjTypes.POSTER -> "举报帖子#$id："
        ObjTypes.COMMENT -> "举报评论#$id："
        else -> "举报$objType#$id："
    }
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        // 标题
        item("title") {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        }

        item(0) {
            Spacer(modifier = Modifier.padding(16.dp))
        }

        item("type") {
            Text(text = "举报类型", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(4.dp))
            ReportTypeDropDownBox(
                selected = selectReportType,
                reportTypes = reportTypes,
                onSelectIndex = onSelectReportType
            )
        }

        item(1) {
            Spacer(modifier = Modifier.padding(8.dp))
        }

        // 内容
        item("text") {
            Text(text = "内容", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.padding(4.dp))
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = text,
                onValueChange = onSetText,
                placeholder = { Text(text = "在这里输入内容哦") },
                shape = RoundedCornerShape(10.dp),
                minLines = 5,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
        }

        item(23) {
            Spacer(modifier = Modifier.padding(8.dp))
        }

        // 发布按钮
        item("report") {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(
                        onClick = onReport,
                        enabled = reportState !is SimpleState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error,
                        )
                    ) {
                        if(reportState is SimpleState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Row {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "举报",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = "提交举报",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        }
                    }
                }
            }
        }

        item(25) {
            Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ReportScreen(
    mainController: MainController,
    objType: String,
    id: Long,
    vm: ReportViewModel = hiltViewModel(),
) {
    val loadReportTypeState by vm.loadReportTypeStateFlow.collectAsState()

    val state by vm.stateLiveData.observeAsState()

    LaunchedEffect(state) {
        if (state is SimpleState.Success) {
            mainController.navController.popBackStack()
            mainController.snackbar("举报成功")
            vm.stateLiveData.value = null
        } else {
            if (state is SimpleState.Error) {
                mainController.snackbar("举报失败（Tips: 不要举报自己哦！）")
                vm.stateLiveData.value = null
            }
        }
    }

    val text by vm.textFlow.collectAsState()

    LaunchedEffect(loadReportTypeState) {
        if(loadReportTypeState == null) {
            vm.loadReportType()
        }
    }

    val selectedReportType by vm.selectedReportTypeLiveData.observeAsState()

    when(loadReportTypeState) {
        is SimpleDataState.Loading, null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .width(64.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is SimpleDataState.Success -> {
            val reportTypes = (loadReportTypeState as SimpleDataState.Success<List<ReportType>>).data

            LaunchedEffect(selectedReportType) {
                if(selectedReportType == null) {
                    vm.setSelectedReportType(reportTypes[0])
                }
            }

            if(selectedReportType != null) {
                val reportType = selectedReportType!!
                ReportScreenContent(
                    objType = objType,
                    id = id,
                    reportTypes = reportTypes,

                    selectReportType = reportType,
                    text = text,
                    reportState = state,

                    onSelectReportType = vm::setSelectedReportType,
                    onSetText = vm::setText,
                    onReport = {
                        when(objType) {
                            ObjTypes.POSTER -> vm.reportPoster(id, reportType.id.toLong(), text)
                            ObjTypes.COMMENT -> vm.reportComment(id, reportType.id.toLong(), text)
                            else -> { vm.stateLiveData.value = SimpleState.Error }
                        }
                    },
                )
            }
        }
        is SimpleDataState.Error -> {

        }
    }
}