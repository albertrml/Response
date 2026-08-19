package br.com.arml.response.test

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata

/**
 * Creates a [Response.Success] with default metadata.
 */
fun <T> successResponse(data: T, metadata: ResponseMetadata = ResponseMetadata()): Response<T> {
    return Response.Success(data, metadata)
}

/**
 * Creates a [Response.Failure] with a [RuntimeException].
 */
fun <T> failureResponse(
    message: String = "Mock Error",
    reason: ErrorReason = ErrorReason.Unknown,
    cache: T? = null
): Response<T> {
    return Response.Failure(
        error = RuntimeException(message),
        reason = reason,
        previousData = cache,
        metadata = ResponseMetadata()
    )
}

/**
 * Creates a [Response.Failure] specifically for network errors.
 */
fun <T> networkErrorResponse(cache: T? = null): Response<T> {
    return Response.Failure(
        error = RuntimeException("No Internet"),
        reason = ErrorReason.Network,
        previousData = cache,
        metadata = ResponseMetadata()
    )
}
