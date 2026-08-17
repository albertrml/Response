package br.com.arml.response.core

/**
 * Encapsulates contextual information about a [Response].
 *
 * @property timestamp The exact moment (in milliseconds) when the data was captured.
 * @property policies A list of strategies applied to this data (e.g., Pagination, Sync).
 * @property extra Open map for custom contextual data.
 */
data class ResponseMetadata(
    val timestamp: Long = System.currentTimeMillis(),
    val policies: List<ResponsePolicy> = emptyList(),
    val extra: Map<String, Any> = emptyMap()
) {
    /**
     * Helper to find a specific policy by type.
     */
    inline fun <reified P : ResponsePolicy> findPolicy(): P? {
        return policies.filterIsInstance<P>().firstOrNull()
    }
}
