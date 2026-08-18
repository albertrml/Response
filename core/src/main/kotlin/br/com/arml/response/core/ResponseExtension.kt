package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Description: Applies [transform] to the [Response.Success] value while preserving Loading/Failure states.
 * Decisions: Strictly preserves original metadata (timestamps/policies) to maintain context integrity.
 */
fun <T, R> Flow<Response<T>>.mapSuccess(
    transform: (T) -> R
): Flow<Response<R>> = map { it.mapTo(transform) }

/**
 * Description: Automatically persists the last [Response.Success] data into subsequent
 * [Response.Loading] or [Response.Failure] states. This is the engine behind "State Recovery".
 * Decisions: Eliminates manual cache management in ViewModels.
 */
fun <T> Flow<Response<T>>.withCache(): Flow<Response<T>> = flow {
    var lastData: T? = null
    var lastMetadata: ResponseMetadata? = null
    collect { response ->
        val updatedResponse = when (response) {
            is Response.Loading -> response.copy(
                previousData = response.previousData ?: lastData,
                metadata = response.metadata ?: lastMetadata
            )
            is Response.Success -> response.also {
                lastData = it.result
                lastMetadata = it.metadata
            }
            is Response.Failure -> response.copy(
                previousData = response.previousData ?: lastData,
                metadata = response.metadata ?: lastMetadata
            )
        }
        updatedResponse.metadata // Just to use it
        emit(updatedResponse)
    }
}

/**
 * Description: Zips two [Response]s into a single [Response]. The resulting state is
 * [Response.Success] only if both states are [Response.Success].
 * Decisions: Follows the "Short-circuit Failure" rule: if one fails, the result is Failure.
 * Both must be Success for the result to be Success.
 */
fun <T1, T2, R> Response<T1>.zipWith(
    other: Response<T2>,
    transform: (T1, T2) -> R
): Response<R> {
    val d1 = this.getOrNull()
    val d2 = other.getOrNull()
    val cache = if (d1 != null && d2 != null) transform(d1, d2) else null

    return when {
        this is Response.Success && other is Response.Success -> {
            Response.Success(transform(this.result, other.result), this.metadata)
        }
        this is Response.Failure -> {
            Response.Failure(error = this.error, reason = this.reason, previousData = cache, metadata = this.metadata)
        }
        other is Response.Failure -> {
            Response.Failure(error = other.error, reason = other.reason, previousData = cache, metadata = other.metadata)
        }
        else -> {
            Response.Loading(previousData = cache, metadata = this.metadata)
        }
    }
}

/**
 *
 * Description: Combines two [Flow]s of [Response] into a single [Flow]:
 *  - The resulting state is [Response.Success] only if both source states are [Response.Success].
 *  - If any state is [Response.Failure], the result is [Response.Failure].
 *  - Otherwise, the result is [Response.Loading].
 * Decisions: Propagates cache and metadata from the first stream as primary context.
 */
fun <T1, T2, R> Flow<Response<T1>>.combineResponse(
    other: Flow<Response<T2>>,
    transform: (T1, T2) -> R
): Flow<Response<R>> = combine(other) { r1, r2 ->
    r1.zipWith(r2, transform)
}

/**
 * Description: Updates a [MutableStateFlow] with the current [Response] state.
 */
inline fun <T, S> Response<T>.update(
    uiState: MutableStateFlow<S>,
    updateState: (S, Response<T>) -> S
) {
    uiState.update { state ->
        updateState(state, this)
    }
}
