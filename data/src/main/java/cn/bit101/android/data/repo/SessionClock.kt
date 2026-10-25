package cn.bit101.android.data.repo

import javax.inject.Inject

internal class SessionClock @Inject constructor() {
    fun now(): Long = System.currentTimeMillis()
}
