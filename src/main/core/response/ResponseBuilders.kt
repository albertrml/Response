package br.com.arml.core.response

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

/**
 * Encapsulates a suspend operation in a [Flow] that emits [Response] states.
 */
fun <T> asResponse(
    block: suspend () -> T
): Flow<Response<T>> = flow {
    val response = try {
        Response.Success(block())
    } catch (e: Exception) {
        Response.Failure(e)
    }
    emit(response)
}.onStart { emit(Response.Loading) }

/**
 * Executes a suspend operation and returns a [Response].
 * Useful for single-shot operations that don't require a Flow.
 */
suspend fun <T> runAsResponse(
    block: suspend () -> T
): Response<T> = try {
    Response.Success(block())
} catch (e: Exception) {
    Response.Failure(e)
}