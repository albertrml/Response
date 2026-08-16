package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Encapsulates a suspend operation in a [Flow] that emits [Response] states.
 * @param initialData Optional data to be used as cache in the [Response.Loading] and [Response.Failure] states.
 */
fun <T> asResponseFlow(
    initialData: T? = null,
    block: suspend () -> T
): Flow<Response<T>> = flow {
    val response = asResponse { block() }.let {
        if (it is Response.Failure && it.previousData == null) {
            it.copy(previousData = initialData)
        } else it
    }
    emit(response)
}.onStart { emit(Response.Loading(previousData = initialData)) }

/**
 * Transforms a common [Flow] into a [Flow] of [Response].
 * @param initialData Optional data to be used as cache in the [Response.Loading] and [Response.Failure] states.
 */
fun <T> Flow<T>.asResponseFlow(initialData: T? = null): Flow<Response<T>> {
    return this
        .map<T, Response<T>> { data -> Response.Success(data) }
        .onStart { emit(Response.Loading(previousData = initialData)) }
        .catch { error ->
            emit(Response.Failure(error = error, previousData = initialData))
        }
}

/**
 * Executes a suspend operation and returns a [Response].
 * Useful for single-shot operations that don't require a Flow.
 */
suspend fun <T> asResponse(
    block: suspend () -> T
): Response<T> = try {
    Response.Success(block())
} catch (e: Throwable) {
    Response.Failure(error = e)
}
