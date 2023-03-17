package cn.bit101.android.net.school

/**
 * @author flwfdd
 * @date 2023/3/17 1:01
 * @description _(:з」∠)_
 */

const val webvpn = false

val schoolLoginUrl: String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/https/77726476706e69737468656265737421fcf84695297e6a596a468ca88d1b203b/authserver/login?service=https%3A%2F%2Fwebvpn.bit.edu.cn%2Flogin%3Fcas_login%3Dtrue"
        else "https://login.bit.edu.cn/authserver/login"
    }