package cn.bit101.android.config.datastore.basic

import android.content.SharedPreferences
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import org.junit.Assert.*
import org.junit.Test

class EncryptedPreferencesItemTest {
    @Test(timeout = 10000) fun `flow observes changed credentials and unregisters on cancellation`() = runBlocking {
        val preferences = mockk<SharedPreferences>(relaxed = true)
        var value = "old"
        val listener = slot<SharedPreferences.OnSharedPreferenceChangeListener>()
        every { preferences.getString("sid", null) } answers { value }
        every { preferences.registerOnSharedPreferenceChangeListener(capture(listener)) } just Runs
        val events = Channel<String>(Channel.UNLIMITED)
        val job = launch { EncryptedPreferencesItem(preferences, "sid").flow.collect { events.send(it) } }
        assertEquals("old", events.receive())
        value = "new"
        listener.captured.onSharedPreferenceChanged(preferences, "sid")
        assertEquals("new", events.receive())
        value = ""
        listener.captured.onSharedPreferenceChanged(preferences, null)
        assertEquals("", events.receive())
        job.cancelAndJoin()
        verify(exactly = 1) { preferences.unregisterOnSharedPreferenceChangeListener(listener.captured) }
    }
}
