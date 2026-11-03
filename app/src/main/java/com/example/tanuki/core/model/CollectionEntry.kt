package com.example.tanuki.core.model

/**
 * Domain model representing a single anime collection entry.
 */
data class CollectionEntry(
    val id: String,
    val title: String,
    val studio: String? = null,
    val coverUri: String? = null
)
