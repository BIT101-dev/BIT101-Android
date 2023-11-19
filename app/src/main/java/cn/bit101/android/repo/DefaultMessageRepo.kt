package cn.bit101.android.repo

import cn.bit101.android.net.BIT101API
import cn.bit101.android.repo.base.MessageRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultMessageRepo @Inject constructor(
) : MessageRepo {
    override suspend fun getUnreadMessageCount() = withContext(Dispatchers.IO) {
        BIT101API.message.getMessagesNumber().body()?.unreadNum
            ?: throw Exception("get unread message count error")
    }

    override suspend fun getSeparateUnreadMessageCount() = withContext(Dispatchers.IO) {
        BIT101API.message.getSeparateMessagesNumber().body()
            ?: throw Exception("get unread message count error")
    }

    override suspend fun getMessages(
        type: String,
        lastID: Int?
    ) = withContext(Dispatchers.IO) {
        BIT101API.message.getMessages(
            lastID = lastID,
            type = type,
        ).body() ?: throw Exception("get messages error")
    }

}