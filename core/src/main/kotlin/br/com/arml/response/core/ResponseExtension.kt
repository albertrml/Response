package br.com.arml.response.core

import kotlinx.coroutines.flow.*

/**
 * Allows transforming data inside a [Response.Success] while keeping Loading and Failure states.
 */
fun <T, R> Flow<Response<T>>.mapSuccess(
    transform: (T) -> R
): Flow<Response<R>> = map { it.mapTo(transform) }

/**
 * Persists the last [Response.Success] data across subsequent [Response.Loading] and [Response.Failure] states.
 * This is the engine behind "State Recovery".
 */
fun <T> Flow<Response<T>>.withCache(): Flow<Response<T>> = flow {
    var lastData: T? = null
    collect { response ->
        val updatedResponse = when (response) {
            is Response.Success -> response.also { lastData = it.result }
            is Response.Loading -> response.copy(previousData = response.previousData ?: lastData)
            is Response.Failure -> response.copy(previousData = response.previousData ?: lastData)
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
