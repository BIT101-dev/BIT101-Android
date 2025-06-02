package cn.bit101.android.features.common.component.snackbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

data class MessageData(
    val message: String,
    val id: UUID,
)

class SnackbarState(
    private val messageState: MutableState<MessageData?>,
    val scope: CoroutineScope,
    private val delayMillis: Long = 4000,
) {
    val message: MessageData?
        get() = messageState.value

    private var job: Job? = null

    private fun show(message: String, id: UUID) {
        synchronized(this) {
            job?.cancel()
            job = scope.launch {
                messageState.value = MessageData(message, id)
                delay(delayMillis)
                if (messageState.value?.id == id) {
                    messageState.value = null
                }
            }
        }
    }

    fun show(message: String) {
        show(message, UUID.randomUUID())
    }

    fun dismiss() {
        messageState.value = null
    }
}

@Composable
fun rememberSnackbarState(
    scope: CoroutineScope,
) = remember {
    SnackbarState(mutableStateOf(null), scope)
}