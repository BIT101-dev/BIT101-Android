package cn.bit101.api.model.common

import cn.bit101.api.model.UniqueData
import java.io.Serializable

data class CourseDetail(
    val commentNum: Int,
    override val id: Int,
    val like: Boolean,
    val likeNum: Int,
    val name: String,
    val number: String,
    val rate: Int,
    val teachersName: String,
    val teachersNumber: String,
) : Serializable, UniqueData