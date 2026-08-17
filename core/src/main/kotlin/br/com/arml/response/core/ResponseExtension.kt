package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Allows transforming data inside a [Response.Success] while keeping Loading and Failure states.
 * Metadata is preserved during transformation.
 */
fun <T, R> Flow<Response<T>>.mapSuccess(
    transform: (T) -> R
): Flow<Response<R>> = map { it.mapTo(transform) }

/**
 * Persists the last [Response.Success] data and its metadata across subsequent [Response.Loading] and [Response.Failure] states.
 * This is the engine behind "State Recovery".
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
        emit(updatedResponse)
    }
}

/**
 * Updates a [MutableStateFlow] with the current [Response] state.
 */
inline fun <T, S> Response<T>.update(
    uiState: MutableStateFlow<S>,
    updateState: (S, Response<T>) -> S
) {
    uiState.update { state ->
        updateState(state, this)
    }
}
