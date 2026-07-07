package br.com.arml.core.response

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update

/**
 * Transforms a common [Flow] into a [Flow] of [Response].
 */
fun <T> Flow<T>.toResponseFlow(): Flow<Response<T>> {
    return this
        .map<T, Response<T>> { data -> Response.Success(data) }
        .onStart { emit(Response.Loading) }
        .catch { error ->
            val exception = error as? Exception
                ?: RuntimeException("Flow encountered an error", error)
            emit(Response.Failure(exception))
        }
}

/**
 * Allows transforming data inside a [Response.Success] while keeping Loading and Failure states.
 */
fun <T, R> Flow<Response<T>>.mapSuccess(
    transform: (T) -> R
): Flow<Response<R>> = this.map { response -> response.mapTo(transform) }

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