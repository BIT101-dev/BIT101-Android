package cn.bit101.api.model.common

import cn.bit101.api.model.UniqueData
import java.io.Serializable

data class User(
    override val id: Int,

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
) : Serializable, UniqueData