package cn.bit101.api.model.http.bit101

class GetVariablesDataModel private constructor() {
    data class Response(
        val data: String,
    )
}

class PostVariablesDataModel private constructor() {
    data class Body(
        val obj: String,
        val data: String,
    )
}