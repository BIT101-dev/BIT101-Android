package cn.bit101.android.config.datastore

import android.content.SharedPreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cn.bit101.android.config.datastore.basic.Preferences
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

class UserDataStoreTest {
    @get:Rule val temporaryFolder = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore by lazy {
        PreferenceDataStoreFactory.create(scope = scope) {
            temporaryFolder.root.resolve("settings.preferences_pb")
        }
    }
    private val encrypted = mockk<SharedPreferences>(relaxed = true)
    private val cookies = CookieManager().cookieStore
    private val preferences by lazy {
        mockk<Preferences> {
            every { SETTING_DATASTORE } returns dataStore
            every { ENCRYPTED_SHARED_PREFERENCES } returns encrypted
            every { COOKIE_PREFERENCES_STORE } returns mockk(relaxed = true)
            every { clearSchoolCookies() } answers { cookies.removeAll(); Unit }
        }
    }

    @After fun close() = runBlocking { scope.coroutineContext.job.cancelAndJoin() }

    @Test(timeout = 10000) fun `upgrade clears only school session and preserves credentials and settings`() = runBlocking {
        val connected = booleanPreferencesKey("login_status")
        val vpn = booleanPreferencesKey("login_web_vpn")
        dataStore.edit {
            it[connected] = true
            it[vpn] = true
            it[stringPreferencesKey("fake_cookie")] = "community-token"
            it[stringPreferencesKey("personal_setting")] = "keep"
        }
        every { encrypted.getString("login_sid", null) } returns "student"
        every { encrypted.getString("login_password", null) } returns "saved-password"
        every { encrypted.getString("community_password_digest", null) } returns "community-digest"
        cookies.add(URI("https://sso.bit.edu.cn"), HttpCookie("session", "stale"))
        val original = dataStore.data.first().asMap()

        val user = UserDataStore(preferences)

        assertTrue(cookies.cookies.isEmpty())
        assertFalse(user.loginStatus.get())
        assertFalse(user.loginWebVpn.get())
        assertEquals("student", user.loginSid.get())
        assertEquals("saved-password", user.loginPassword.get())
        assertEquals("community-digest", user.communityPasswordDigest.get())
        assertEquals(original.filterKeys { it != connected && it != vpn } +
            Pair(booleanPreferencesKey("school_session_reset_1_4_4"), true),
            dataStore.data.first().asMap())
        verify(exactly = 0) { encrypted.edit() }
    }

    @Test(timeout = 10000) fun `next initialization keeps newly established school session`() = runBlocking {
        val first = UserDataStore(preferences)
        first.loginStatus.set(true)
        first.loginWebVpn.set(true)
        cookies.add(URI("https://sso.bit.edu.cn"), HttpCookie("session", "new"))
        scope.coroutineContext.job.cancelAndJoin()

        // 重新打开持久化文件，模拟重启，迁移标记不能仅存在于内存中。
        val restartedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val restarted = PreferenceDataStoreFactory.create(scope = restartedScope) {
                temporaryFolder.root.resolve("settings.preferences_pb")
            }
            every { preferences.SETTING_DATASTORE } returns restarted
            val user = UserDataStore(preferences)
            assertTrue(user.loginStatus.get())
            assertTrue(user.loginWebVpn.get())
            assertEquals("new", cookies.cookies.single().value)
            verify(exactly = 1) { preferences.clearSchoolCookies() }
        } finally {
            restartedScope.coroutineContext.job.cancelAndJoin()
        }
    }

    @Test(timeout = 10000) fun `failed cleanup does not mark migration complete`() = runBlocking {
        dataStore.edit { it[booleanPreferencesKey("login_status")] = true }
        val original = dataStore.data.first()
        val store = preferences
        every { store.clearSchoolCookies() } throws IllegalStateException("disk failure")

        assertThrows(IllegalStateException::class.java) { UserDataStore(preferences) }
        assertEquals(original, dataStore.data.first())

        every { preferences.clearSchoolCookies() } just Runs
        assertFalse(UserDataStore(preferences).loginStatus.get())
        verify(exactly = 2) { preferences.clearSchoolCookies() }
    }
}
