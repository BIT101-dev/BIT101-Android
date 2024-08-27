package cn.bit101.api.helper

interface Logger {
    fun err(tag: String? = null, msg: String)
    fun warn(tag: String? = null, msg: String)
    fun info(tag: String? = null, msg: String)
    fun debug(tag: String? = null, msg: String)
}

internal val emptyLogger = object : Logger {
    override fun err(tag: String?, msg: String) {}
    override fun warn(tag: String?, msg: String) {}
    override fun info(tag: String?, msg: String) {}
    override fun debug(tag: String?, msg: String) {}
}