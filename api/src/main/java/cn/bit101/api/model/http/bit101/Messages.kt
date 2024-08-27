package cn.bit101.api.model.http.bit101

import cn.bit101.api.model.common.User

class GetMessagesNumberDataModel private constructor() {
    data class Response(
        val unreadNum: Int,
    )
}

class GetSeparateMessagesNumberDataModel private constructor() {
    data class Response(
        val comment: Int,
        val follow: Int,
        val like: Int,
        val system: Int
    )
}

class GetMessagesDataModel private constructor() {
    data class ResponseItem(
        val fromUser: User,
        val id: Int,
        val linkObj: String,
        val obj: String,
        val text: String,
        val updateTime: String
    )

    class Response : ArrayList<ResponseItem>()
}

class PostSystemMessageReadDataModel private constructor() {
    data class Body(
        val fromUid: Int,
        val linkObj: String,
        val obj: String,
        val text: String,
        val toUid: Int
    )
}