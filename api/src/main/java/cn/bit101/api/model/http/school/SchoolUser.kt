package cn.bit101.api.model.http.school

import com.google.gson.annotations.SerializedName

class PostSchoolLoginDataModel {
    data class Response(
        val html: String,
        val success: Boolean,
    )
}

class GetSchoolInitLoginDataModel {
    data class Response(
        val html: String,
        val salt: String?,
        val execution: String?,
        val ifLogin: Boolean,
    )
}


class GetCheckNeedCaptchaDataModel {
    data class Response(
        @SerializedName("isNeed") val isNeed: Boolean,
    )
}