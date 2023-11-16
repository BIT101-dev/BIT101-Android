package cn.bit101.android.ui.gallery.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.bit101.android.ui.MainController
import cn.bit101.android.ui.gallery.common.SimpleDataState

@Composable
fun UserScreen(
    mainController: MainController,
    vm: UserViewModel = hiltViewModel(),
    id: Long = 0,
) {

    val getUserInfoState by vm.getUserInfoStateFlow.collectAsState()

    LaunchedEffect(getUserInfoState) {
        if(getUserInfoState == null) {
            vm.getUserInfo(id)
        }
    }

    when(getUserInfoState) {
        null, is SimpleDataState.Loading -> {
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
            val data = (getUserInfoState as SimpleDataState.Success).data
            Text(text = data.toString())
        }
        is SimpleDataState.Error -> {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "加载失败",
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}