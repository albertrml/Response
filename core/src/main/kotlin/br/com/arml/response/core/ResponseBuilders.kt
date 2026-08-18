package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Description: Creates a [Flow] that emits [Response.Loading] then the result of [block].
 * Decisions: Injects [initialData] as cache to enable immediate UI rendering during refresh.
 * @param initialData Optional data to be used as cache in the [Response.Loading] and [Response.Failure] states.
 * @param initialMetadata Optional metadata to be carried during the initial [Response.Loading] state.
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
 * Description: Converts a regular [Flow] into a [Flow] of [Response].
 * Decisions: Uses [initialData] as fallback for Loading/Failure states.
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
 * Description: Executes a suspend operation and returns a [Response]. Useful for single-shot operations that
 * don't require a Flow.
 * Decisions: Ensures that [Response.Success] always has a [ResponseMetadata] (with timestamp) for traceability.
 * @param metadata Optional metadata to associate with the resulting [Response].
 */
suspend fun <T> asResponse(
    metadata: ResponseMetadata? = null,
    block: suspend () -> T
): Response<T> = try {
    Response.Success(block(), metadata ?: ResponseMetadata())
} catch (e: Throwable) {
    Response.Failure(error = e, metadata = metadata)
}

/**
 * Description: Converts a Kotlin [Result] into a [Response].
 * Decisions: Mails the [Throwable] from Result directly into our Failure state.
 * @param metadata Optional metadata to associate with the resulting [Response].
 */
fun <T> Result<T>.asResponse(metadata: ResponseMetadata? = null): Response<T> {
    return fold(
        onSuccess = { Response.Success(it, metadata ?: ResponseMetadata()) },
        onFailure = { Response.Failure(error = it, metadata = metadata) }
    )
}

/**
 * Description: Converts a [Flow] of [Result] into a [Flow] of [Response].
 * Decisions: Symmetric naming with standard asResponseFlow. Use @JvmName to avoid signature clash.
 */
@JvmName("asResponseResultFlow")
fun <T> Flow<Result<T>>.asResponseFlow(
    initialData: T? = null,
    initialMetadata: ResponseMetadata? = null
): Flow<Response<T>> = this
    .map { it.asResponse() }
    .onStart { emit(Response.Loading(initialData, initialMetadata)) }
