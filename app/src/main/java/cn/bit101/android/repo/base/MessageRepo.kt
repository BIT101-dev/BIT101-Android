package cn.bit101.android.repo.base

import cn.bit101.api.model.http.bit101.GetMessagesDataModel
import cn.bit101.api.model.http.bit101.GetSeparateMessagesNumberDataModel

interface MessageRepo {
    suspend fun getUnreadMessageCount(): Int

    suspend fun getSeparateUnreadMessageCount(): GetSeparateMessagesNumberDataModel.Response

    suspend fun getMessages(
        type: String,
        lastID: Int? = null
    ): GetMessagesDataModel.Response
}