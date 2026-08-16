package br.com.arml.response.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A Composable that handles the rendering and side effects of a [Response].
 *
 * @param successContent Content to show on [Response.Success].
 * @param loadingContent Content to show on [Response.Loading].
 * @param failureContent Content to show on [Response.Failure].
 * @param actionOnSuccess Side-effect to run once on [Response.Success].
 * @param actionOnFailure Side-effect to run once on [Response.Failure].
 * @param delay Artificial delay for the [LaunchedEffect] (default 500ms).
 */
@Composable
fun <T> Response<T>.ShowResults(
    successContent: @Composable (T) -> Unit = {},
    loadingContent: @Composable (cache: T?) -> Unit = {},
    failureContent: @Composable (error: Throwable, reason: ErrorReason, cache: T?) -> Unit = { _, _, _ -> },
    actionOnSuccess: (T) -> Unit = {},
    actionOnFailure: (error: Throwable, reason: ErrorReason, cache: T?) -> Unit = { _, _, _ -> },
    delay: Long = 500
) {
    LaunchedEffect(this) {
        if (delay > 0) delay(delay.milliseconds)
        when (this@ShowResults) {
            is Response.Success -> actionOnSuccess(this@ShowResults.result)
            is Response.Failure -> actionOnFailure(
                this@ShowResults.error,
                this@ShowResults.reason,
                this@ShowResults.previousData
            )
            else -> {}
        }
    }

    when (this) {
        is Response.Success -> successContent(this.result)
        is Response.Loading -> loadingContent(this.previousData)
        is Response.Failure -> failureContent(this.error, this.reason, this.previousData)
    }
}
