package com.example.tanuki.core.storage

import android.content.Intent
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): Map<String, *> = map
    override fun getString(key: String, defValue: String?): String? = map[key] as? String ?: defValue
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? = @Suppress("UNCHECKED_CAST") (map[key] as? Set<String> ?: defValues)
    override fun getInt(key: String, defValue: Int): Int = map[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long): Long = map[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float): Float = map[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
    override fun contains(key: String): Boolean = map.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    inner class FakeEditor : SharedPreferences.Editor {
        private val tempMap = mutableMapOf<String, Any?>()

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            tempMap[key] = value
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            tempMap[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            tempMap[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            tempMap[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            tempMap[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            tempMap[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            tempMap.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            tempMap.clear()
            return this
        }

        override fun commit(): Boolean {
            map.putAll(tempMap)
            return true
        }

        override fun apply() {
            map.putAll(tempMap)
        }
    }
}

class SafStorageManagerTest {

    private lateinit var fakePrefs: FakeSharedPreferences

    @Before
    fun setUp() {
        fakePrefs = FakeSharedPreferences()
    }

    @Test
    fun getSavedRootUriString_initiallyNull() {
        assertNull(fakePrefs.getString(SafStorageManager.KEY_ROOT_URI, null))
    }

    @Test
    fun preferences_saveAndRetrieveString() {
        fakePrefs.edit().putString(SafStorageManager.KEY_ROOT_URI, "content://com.android.externalstorage.documents/tree/primary%3ATanuki").apply()
        val saved = fakePrefs.getString(SafStorageManager.KEY_ROOT_URI, null)
        assertTrue(saved != null && saved.contains("Tanuki"))
    }

    // --- Flag Extraction & Validation Tests ---

    @Test
    fun flagExtractor_readWritePersistable_hasValidAccessFlags() {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION

        assertTrue(SafFlagExtractor.isPersistableFlagPresent(flags))
        assertTrue(SafFlagExtractor.hasValidAccessFlags(flags))

        val takeFlags = SafFlagExtractor.extractTakeFlags(flags)
        val readGranted = (takeFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
        val writeGranted = (takeFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0

        assertTrue(readGranted)
        assertTrue(writeGranted)
    }

    @Test
    fun flagExtractor_readOnlyPersistable_extractsReadOnly() {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION

        assertTrue(SafFlagExtractor.isPersistableFlagPresent(flags))
        assertTrue(SafFlagExtractor.hasValidAccessFlags(flags))

        val takeFlags = SafFlagExtractor.extractTakeFlags(flags)
        val readGranted = (takeFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
        val writeGranted = (takeFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0

        assertTrue(readGranted)
        assertFalse(writeGranted)
    }

    @Test
    fun flagExtractor_writeOnlyPersistable_extractsWriteOnly() {
        val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION

        assertTrue(SafFlagExtractor.isPersistableFlagPresent(flags))
        assertTrue(SafFlagExtractor.hasValidAccessFlags(flags))

        val takeFlags = SafFlagExtractor.extractTakeFlags(flags)
        val readGranted = (takeFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
        val writeGranted = (takeFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0

        assertFalse(readGranted)
        assertTrue(writeGranted)
    }

    @Test
    fun flagExtractor_zeroFlags_failsValidation() {
        val flags = 0
        assertFalse(SafFlagExtractor.isPersistableFlagPresent(flags))
        assertFalse(SafFlagExtractor.hasValidAccessFlags(flags))
        assertEquals(0, SafFlagExtractor.extractTakeFlags(flags))
    }

    @Test
    fun flagExtractor_missingPersistableFlag_failsValidation() {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        assertFalse(SafFlagExtractor.isPersistableFlagPresent(flags))
        assertFalse(SafFlagExtractor.hasValidAccessFlags(flags))
    }
}
