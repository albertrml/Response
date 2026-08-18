package br.com.arml.response.retrofit

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import kotlinx.coroutines.flow.*
import retrofit2.Response as RetrofitResponse
import java.io.IOException

/**
 * Bridge: Converts Retrofit's raw response ([RetrofitResponse]) to our domain [Response].
 */
inline fun <reified T> RetrofitResponse<T>.asResponse(
    metadata: ResponseMetadata? = null,
    previousData: T? = null
): Response<T> = toResponse(
    metadata = metadata,
    previousData = previousData,
    isUnit = Unit is T
)

/**
 * High-level Builder: Executes Retrofit call and auto-maps errors.
 */
suspend inline fun <reified T> asRetrofitResponse(
    metadata: ResponseMetadata? = null,
    previousData: T? = null,
    crossinline block: suspend () -> RetrofitResponse<T>
): Response<T> = try {
    block().asResponse(metadata, previousData)
} catch (e: Throwable) {
    Response.Failure(e, mapRetrofitException(e), previousData, metadata ?: ResponseMetadata())
}

/**
 * High-level Builder: Retrofit call as a stream with State Recovery support.
 */
inline fun <reified T> asRetrofitResponseFlow(
    initialData: T? = null,
    initialMetadata: ResponseMetadata? = null,
    crossinline block: suspend () -> RetrofitResponse<T>
): Flow<Response<T>> = flow {
    emit(asRetrofitResponse(initialMetadata, initialData, block))
}.onStart { emit(Response.Loading(initialData, initialMetadata)) }

/**
 * Internal: The actual mapping logic (Non-inline to ensure 100% branch coverage).
 */
@PublishedApi
internal fun <T> RetrofitResponse<T>.toResponse(
    metadata: ResponseMetadata?,
    previousData: T?,
    isUnit: Boolean
): Response<T> {
    val meta = metadata ?: ResponseMetadata()
    if (!isSuccessful) {
        return Response.Failure(
            RuntimeException(message()),
            mapRetrofitErrorCode(code()),
            previousData,
            meta
        )
    }

    val body = body()
    if (body != null) return Response.Success(body, meta)

    return if (isUnit) {
        @Suppress("UNCHECKED_CAST")
        Response.Success(Unit as T, meta)
    } else {
        Response.Failure(
            RuntimeException("Response body is null"),
            ErrorReason.Server(code()),
            previousData,
            meta
        )
    }
}

/**
 * Internal: Maps Retrofit error codes to [ErrorReason].
 */
fun mapRetrofitErrorCode(code: Int): ErrorReason {
    return when (code) {
        in 400..499 -> ErrorReason.Client(code)
        in 500..599 -> ErrorReason.Server(code)
        else -> ErrorReason.Unknown
    }
}

/**
 * Internal: Maps exceptions to [ErrorReason].
 */
fun mapRetrofitException(e: Throwable): ErrorReason =
    if (e is IOException) ErrorReason.Network else ErrorReason.Unknown
