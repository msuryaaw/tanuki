package com.example.tanuki.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.tanuki.R
import com.example.tanuki.feature.library.LibraryScreen
import com.example.tanuki.feature.playlists.PlaylistsScreen

enum class TanukiDestination(val titleRes: Int) {
    LIBRARY(R.string.nav_library),
    PLAYLISTS(R.string.nav_playlists)
}

@Composable
fun TanukiApp(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(TanukiDestination.LIBRARY) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination == TanukiDestination.LIBRARY,
                    onClick = { currentDestination = TanukiDestination.LIBRARY },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_library)) }
                )
                NavigationBarItem(
                    selected = currentDestination == TanukiDestination.PLAYLISTS,
                    onClick = { currentDestination = TanukiDestination.PLAYLISTS },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_playlists)) }
                )
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            TanukiDestination.LIBRARY -> LibraryScreen(modifier = Modifier.padding(innerPadding))
            TanukiDestination.PLAYLISTS -> PlaylistsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}
