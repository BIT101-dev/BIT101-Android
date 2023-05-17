package cn.bit101.android.viewmodel

import androidx.lifecycle.ViewModel
import cn.bit101.android.database.EncryptedStore

/**
 * @author flwfdd
 * @date 14/05/2023 18:46
 * @description _(:з」∠)_
 */
class BIT101WebViewModel : ViewModel() {
    val BASE_URL="https://bit101.cn"
    val sid: String?
        get() {
            return EncryptedStore.getString(EncryptedStore.SID)
        }
    val password: String?
        get() {
            return EncryptedStore.getString(EncryptedStore.PASSWORD)
        }
}