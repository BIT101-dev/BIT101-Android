package cn.bit101.android.data.repo

import cn.bit101.android.data.net.base.APIManager
import cn.bit101.android.data.repo.base.MessageRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultMessageRepo @Inject constructor(
    private val apiManager: APIManager
) : MessageRepo {

    private val api = apiManager.api

    override suspend fun getUnreadMessageCount() = withContext(Dispatchers.IO) {
        api.message.getMessagesNumber().body()?.unreadNum
            ?: throw Exception("get unread message count error")
    }

    override suspend fun getSeparateUnreadMessageCount() = withContext(Dispatchers.IO) {
        api.message.getSeparateMessagesNumber().body()
            ?: throw Exception("get unread message count error")
    }

    override suspend fun getMessages(
        type: String,
        lastID: Int?
    ) = withContext(Dispatchers.IO) {
        api.message.getMessages(
            lastID = lastID,
            type = type,
        ).body() ?: throw Exception("get messages error")
    }

}