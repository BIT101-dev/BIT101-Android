package cn.bit101.android.features.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class ImagePicker(
    private val launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        launcher.launch(intent)
    }
}

@Composable
fun rememberImagePicker(
    onPickImage: (uri: Uri) -> Unit,
): ImagePicker {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val uri = data.data
                if (uri != null) onPickImage(uri)
            }
        }
    }
    return remember {
        ImagePicker(launcher)
    }
}