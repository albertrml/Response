package br.com.arml.response.core

/**
 * Marker interface for any strategy or policy associated with a [Response].
 */
interface ResponsePolicy

/**
 * Standard implementation for page-based pagination.
 */
data class PagePaginationPolicy(
    val currentPage: Int,
    val hasMore: Boolean,
    val totalItems: Long? = null
) : ResponsePolicy

/**
 * Strategy for cursor-based pagination.
 */
data class CursorPaginationPolicy(
    val nextCursor: String?,
    val hasMore: Boolean
) : ResponsePolicy

/**
 * Strategy for data synchronization.
 */
data class SyncPolicy(
    val lastVersion: Long,
    val isDelta: Boolean
) : ResponsePolicy
