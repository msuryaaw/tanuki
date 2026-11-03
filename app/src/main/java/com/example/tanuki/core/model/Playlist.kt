package com.example.tanuki.core.model

/**
 * Domain model representing a user-created playlist.
 */
data class Playlist(
    val id: String,
    val name: String,
    val itemCount: Int = 0
)
