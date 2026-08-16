package br.com.arml.response.core

/**
 * Semantic representation of why an operation failed.
 */
sealed interface ErrorReason {
    /**
     * Issues related to internet connection or transport layer.
     */
    data object Network : ErrorReason

    /**
     * Issues from the server (e.g., 5xx errors).
     */
    data class Server(val code: Int? = null) : ErrorReason

    /**
     * Client-side issues or bad requests (e.g., 4xx errors).
     */
    data class Client(val code: Int? = null) : ErrorReason

    /**
     * Specific business logic violations.
     */
    data class Business(val code: String? = null) : ErrorReason

    /**
     * Uncategorized or unexpected exceptions.
     */
    data object Unknown : ErrorReason
}
