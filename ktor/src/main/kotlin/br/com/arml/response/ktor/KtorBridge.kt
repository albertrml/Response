package br.com.arml.response.ktor

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import io.ktor.client.call.*
import io.ktor.client.statement.*
import kotlinx.coroutines.flow.*
import java.io.IOException

/**
 * Bridge: Converts Ktor's HttpResponse to our domain [Response].
 */
suspend inline fun <reified T> HttpResponse.asResponse(
    metadata: ResponseMetadata? = null,
    previousData: T? = null
): Response<T> {
    val meta = metadata ?: ResponseMetadata()
    if (status.value !in 200..299) {
        return Response.Failure(
            RuntimeException("HTTP error ${status.value}"),
            mapKtorErrorCode(status.value),
            previousData,
            meta
        )
    }

    return try {
        Response.Success(body<T>(), meta)
    } catch (e: Throwable) {
        Response.Failure(e, ErrorReason.Unknown, previousData, meta)
    }
}

/**
 * High-level Builder: Ktor call with auto error reasoning.
 */
suspend inline fun <reified T> asKtorResponse(
    metadata: ResponseMetadata? = null,
    previousData: T? = null,
    crossinline block: suspend () -> HttpResponse
): Response<T> = try {
    block().asResponse<T>(metadata, previousData)
} catch (e: Throwable) {
    Response.Failure(e, mapKtorException(e), previousData, metadata ?: ResponseMetadata())
}

/**
 * High-level Builder: Ktor call as a stream with State Recovery support.
 */
inline fun <reified T> asKtorResponseFlow(
    initialData: T? = null,
    initialMetadata: ResponseMetadata? = null,
    crossinline block: suspend () -> HttpResponse
): Flow<Response<T>> = flow {
    emit(asKtorResponse<T>(initialMetadata, initialData, block))
}.onStart { emit(Response.Loading(initialData, initialMetadata)) }

/**
 * Internal: Maps Ktor status codes to [ErrorReason].
 */
fun mapKtorErrorCode(code: Int): ErrorReason {
    return when (code) {
        in 400..499 -> ErrorReason.Client(code)
        in 500..599 -> ErrorReason.Server(code)
        else -> ErrorReason.Unknown
    }
}

/**
 * Internal: Maps exceptions to [ErrorReason].
 */
fun mapKtorException(e: Throwable): ErrorReason =
    if (e is IOException) ErrorReason.Network else ErrorReason.Unknown
