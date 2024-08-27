package cn.bit101.android.data.repo.base

import cn.bit101.api.model.http.bit101.GetMessagesDataModel
import cn.bit101.api.model.http.bit101.GetSeparateMessagesNumberDataModel

interface MessageRepo {

    /**
     * 获取未读消息数量
     */
    suspend fun getUnreadMessageCount(): Int

    /**
     * 获取分开的未读消息数量
     */
    suspend fun getSeparateUnreadMessageCount(): GetSeparateMessagesNumberDataModel.Response

    /**
     * 获取消息
     */
    suspend fun getMessages(
        type: String,
        lastID: Int? = null
    ): GetMessagesDataModel.Response
}