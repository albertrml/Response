package br.com.arml.response.test

import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
import org.junit.Assert.fail

/**
 * Asserts that the [Response] is an instance of [Response.Success].
 *
 * @param message Optional message to display on failure.
 * @param action Optional block to execute with the data and metadata if success.
 */
fun <T> Response<T>.assertSuccess(
    message: String? = null,
    action: ((data: T, metadata: ResponseMetadata?) -> Unit)? = null
): Response.Success<T> {
    if (this !is Response.Success) {
        fail(message ?: "Expected Success but was ${this::class.simpleName}")
    }
    val success = this as Response.Success<T>
    action?.invoke(success.result, success.metadata)
    return success
}

/**
 * Asserts that the [Response] is an instance of [Response.Failure].
 *
 * @param expectedReason Optional [ErrorReason] to validate.
 * @param message Optional message to display on failure.
 * @param action Optional block to execute with failure details.
 */
fun <T> Response<T>.assertFailure(
    expectedReason: ErrorReason? = null,
    message: String? = null,
    action: ((error: Throwable, reason: ErrorReason, cache: T?, metadata: ResponseMetadata?) -> Unit)? = null
): Response.Failure<T> {
    if (this !is Response.Failure) {
        fail(message ?: "Expected Failure but was ${this::class.simpleName}")
    }
    val failure = this as Response.Failure<T>
    
    if (expectedReason != null && failure.reason != expectedReason) {
        fail("Expected Failure reason $expectedReason but was ${failure.reason}")
    }
    
    action?.invoke(failure.error, failure.reason, failure.previousData, failure.metadata)
    return failure
}

/**
 * Asserts that the [Response] is an instance of [Response.Loading].
 *
 * @param message Optional message to display on failure.
 * @param action Optional block to execute with cache and metadata.
 */
fun <T> Response<T>.assertLoading(
    message: String? = null,
    action: ((cache: T?, metadata: ResponseMetadata?) -> Unit)? = null
): Response.Loading<T> {
    if (this !is Response.Loading) {
        fail(message ?: "Expected Loading but was ${this::class.simpleName}")
    }
    val loading = this as Response.Loading<T>
    action?.invoke(loading.previousData, loading.metadata)
    return loading
}