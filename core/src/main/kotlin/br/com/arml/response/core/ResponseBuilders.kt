package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Encapsulates a suspend operation in a [Flow] that emits [Response] states.
 * @param initialData Optional data to be used as cache in the [Response.Loading] and [Response.Failure] states.
 * @param initialMetadata Optional metadata to be carried during the transition.
 */
fun <T> asResponseFlow(
    initialData: T? = null,
    initialMetadata: ResponseMetadata? = null,
    block: suspend () -> T
): Flow<Response<T>> = flow {
    val response = asResponse(metadata = initialMetadata) { block() }.let {
        if (it is Response.Failure && it.previousData == null) {
            it.copy(previousData = initialData)
        } else it
    }
    emit(response)
}.onStart { emit(Response.Loading(previousData = initialData, metadata = initialMetadata)) }

/**
 * Transforms a common [Flow] into a [Flow] of [Response].
 * @param initialData Optional data to be used as cache in the [Response.Loading] and [Response.Failure] states.
 * @param initialMetadata Optional metadata to be carried during the initial [Response.Loading] state.
 */
fun <T> Flow<T>.asResponseFlow(
    initialData: T? = null,
    initialMetadata: ResponseMetadata? = null
): Flow<Response<T>> {
    return this
        .map<T, Response<T>> { data -> Response.Success(data, ResponseMetadata()) }
        .onStart { emit(Response.Loading(previousData = initialData, metadata = initialMetadata)) }
        .catch { error ->
            emit(Response.Failure(error = error, previousData = initialData, metadata = initialMetadata))
        }
}

/**
 * Executes a suspend operation and returns a [Response].
 * Useful for single-shot operations that don't require a Flow.
 * @param metadata Optional metadata to associate with the resulting [Response].
 * If null, a default [ResponseMetadata] with the current timestamp will be created on Success.
 */
suspend fun <T> asResponse(
    metadata: ResponseMetadata? = null,
    block: suspend () -> T
): Response<T> = try {
    Response.Success(block(), metadata ?: ResponseMetadata())
} catch (e: Throwable) {
    Response.Failure(error = e, metadata = metadata)
}
