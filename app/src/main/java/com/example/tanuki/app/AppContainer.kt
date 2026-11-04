package com.example.tanuki.app

import android.content.Context
import com.example.tanuki.core.storage.SafStorageManager

/**
 * AppContainer for manual dependency injection following project architecture guidelines.
 */
class AppContainer(context: Context) {
    val storageManager: SafStorageManager = SafStorageManager(context)
}
