package com.example.tanuki.core.storage

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri

/**
 * Result state when persisting or validating a SAF storage root.
 */
sealed interface StorageSelectionResult {
    data class Success(val uri: Uri, val readGranted: Boolean, val writeGranted: Boolean) : StorageSelectionResult
    data class Failure(val message: String) : StorageSelectionResult
    data object Cancelled : StorageSelectionResult
}

/**
 * Helper object providing pure flag extraction and validation logic.
 */
object SafFlagExtractor {
    fun extractTakeFlags(intentFlags: Int): Int {
        return intentFlags and (
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
    }

    fun isPersistableFlagPresent(intentFlags: Int): Boolean {
        return (intentFlags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) != 0
    }

    fun hasValidAccessFlags(intentFlags: Int): Boolean {
        return extractTakeFlags(intentFlags) != 0 && isPersistableFlagPresent(intentFlags)
    }
}

/**
 * Manages Storage Access Framework (SAF) tree URI selection,
 * persistable URI permissions, and storage location preferences.
 */
class SafStorageManager(
    private val context: Context,
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
) {
    private val contentResolver: ContentResolver get() = context.contentResolver

    /**
     * Persists the selected tree URI using SAF takePersistableUriPermission
     * and records the selection in preferences upon success.
     */
    fun saveStorageRoot(
        uri: Uri?,
        intentFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
    ): StorageSelectionResult {
        if (uri == null) {
            return StorageSelectionResult.Cancelled
        }

        if (!SafFlagExtractor.isPersistableFlagPresent(intentFlags)) {
            return StorageSelectionResult.Failure("FLAG_GRANT_PERSISTABLE_URI_PERMISSION not provided by system picker.")
        }

        val takeFlags = SafFlagExtractor.extractTakeFlags(intentFlags)
        if (takeFlags == 0) {
            return StorageSelectionResult.Failure("No READ or WRITE access permissions granted by system picker.")
        }

        val readGranted = (takeFlags and Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0
        val writeGranted = (takeFlags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0

        return try {
            contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Verify that the permission is recorded in OS persisted permissions list
            if (!hasPersistedPermission(uri)) {
                return StorageSelectionResult.Failure("Failed to record persisted URI permission with OS.")
            }

            // Save string representation to SharedPreferences only after OS grant succeeds
            preferences.edit {
                putString(KEY_ROOT_URI, uri.toString())
            }

            StorageSelectionResult.Success(uri, readGranted, writeGranted)
        } catch (e: SecurityException) {
            StorageSelectionResult.Failure("SecurityException taking persistable URI permission: ${e.localizedMessage}")
        } catch (e: Exception) {
            StorageSelectionResult.Failure("Error saving storage root: ${e.localizedMessage}")
        }
    }

    /**
     * Returns the currently saved storage root URI string, if any.
     */
    fun getSavedRootUriString(): String? {
        return preferences.getString(KEY_ROOT_URI, null)
    }

    /**
     * Returns the currently saved storage root Uri if valid and permitted.
     */
    fun getSavedRootUri(): Uri? {
        val uriString = getSavedRootUriString() ?: return null
        val uri = uriString.toUri()
        return if (hasPersistedPermission(uri)) uri else null
    }

    /**
     * Checks if Android OS currently holds an active persisted URI permission for the given URI.
     */
    fun hasPersistedPermission(uri: Uri): Boolean {
        val persistedPermissions = try {
            contentResolver.persistedUriPermissions
        } catch (_: Exception) {
            emptyList()
        }
        return persistedPermissions.any { permission ->
            permission.uri == uri && (permission.isReadPermission || permission.isWritePermission)
        }
    }

    /**
     * Checks whether storage has been configured and has an active persisted grant.
     */
    fun isStorageConfigured(): Boolean {
        return getSavedRootUri() != null
    }

    companion object {
        const val PREFS_NAME = "tanuki_storage_prefs"
        const val KEY_ROOT_URI = "pref_storage_root_uri"
    }
}
