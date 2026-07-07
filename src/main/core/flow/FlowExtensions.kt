package br.com.arml.core.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion

/**
 * Exception used internally by [until] to break the flow collection.
 */
sealed class FlowToolsException(override val message: String) : Exception() {
    class CollectUntilPredicateException : FlowToolsException(
        "Predicate was collected before completion"
    )
}

/**
 * Emits values from the Flow until the [predicate] is satisfied.
 * The value that satisfies the predicate is also emitted before closing.
 */
fun <T> Flow<T>.until(predicate: (T) -> Boolean): Flow<T> = flow {
    try {
        collect { value ->
            emit(value)
            if (predicate(value)) {
                throw FlowToolsException.CollectUntilPredicateException()
            }
        }
    } catch (e: FlowToolsException.CollectUntilPredicateException) {
        // Exception caught to stop collection
    }
}.onCompletion { cause ->
    if (cause != null && cause !is FlowToolsException.CollectUntilPredicateException) {
        throw cause
    }
}
