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

// 课程表鉴权初始化
val scheduleInitUrl: String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/sys/funauthapp/api/getAppConfig/wdkbby-5959167891382285.do"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/sys/funauthapp/api/getAppConfig/wdkbby-5959167891382285.do"
    }

// 设置课程表语言为中文
val scheduleLangUrl: String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/i18n.do?appName=wdkbby&EMAP_LANG=zh"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/i18n.do?appName=wdkbby&EMAP_LANG=zh"
    }

// 获取当前学期
val scheduleNowTermUrl:String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/sys/wdkbby/modules/jshkcb/dqxnxq.do"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/modules/jshkcb/dqxnxq.do"
    }

// 获取学期列表
val scheduleTermListUrl:String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/sys/wdkbby/modules/jshkcb/xnxqcx.do"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/modules/jshkcb/xnxqcx.do"
    }

// 获取星期日期
val scheduleDateUrl: String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/sys/wdkbby/wdkbByController/cxzkbrq.do"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/wdkbByController/cxzkbrq.do"
    }

// 获取课程表
val scheduleUrl: String
    get() {
        return if (webvpn) "https://webvpn.bit.edu.cn/http/77726476706e69737468656265737421faef5b842238695c720999bcd6572a216b231105adc27d/jwapp/sys/wdkbby/modules/xskcb/cxxszhxqkb.do"
        else "http://jxzxehallapp.bit.edu.cn/jwapp/sys/wdkbby/modules/xskcb/cxxszhxqkb.do"
    }