package br.com.arml.response.core

/**
 * Envelope for contextual information about a [Response].
 * Decisions: Kept separate from the data model to avoid polluting domain models with infrastructure concerns.
 *
 * @property timestamp Capture moment. Essential for cache expiration logic.
 * @property policies List of strategies (Pagination, Sync) applied to this data.
 * @property extra Open map for custom contextual data.
 */
data class ResponseMetadata(
    val timestamp: Long = System.currentTimeMillis(),
    val policies: List<ResponsePolicy> = emptyList(),
    val extra: Map<String, Any> = emptyMap()
) {
    /**
     * Extraction Utility: Find a policy by its type.
     * Decisions: Provides safe, reified access to pluggable strategies.
     */
    inline fun <reified P : ResponsePolicy> findPolicy(): P? {
        return policies.filterIsInstance<P>().firstOrNull()
    }
}
