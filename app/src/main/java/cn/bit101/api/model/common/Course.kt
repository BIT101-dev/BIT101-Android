package cn.bit101.api.model.common

import java.io.Serializable

data class Course(
    val commentNum: Int,
    val id: Int,
    val likeNum: Int,
    val name: String,
    val number: String,
    val rate: Int,
    val teachersName: String,
    val teachersNumber: String
) : Serializable