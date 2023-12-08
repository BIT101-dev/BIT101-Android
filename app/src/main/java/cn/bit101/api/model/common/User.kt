package cn.bit101.api.model.common

import java.io.Serializable

data class User(
    val id: Int,

    // 注册时间
    val createTime: String,

    // 昵称
    val nickname: String,

    // 头像
    val avatar: Avatar,

    // 格言/简介
    val motto: String,

    // 身份
    val identity: Identity,
) : Serializable