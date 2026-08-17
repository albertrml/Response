package br.com.arml.response.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.arml.response.core.ErrorReason
import br.com.arml.response.core.Response
import br.com.arml.response.core.ResponseMetadata
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
    successContent: @Composable (data: T, metadata: ResponseMetadata?) -> Unit = { _, _ -> },
    loadingContent: @Composable (cache: T?, metadata: ResponseMetadata?) -> Unit = { _, _ -> },
    failureContent: @Composable (error: Throwable, reason: ErrorReason, cache: T?, metadata: ResponseMetadata?) -> Unit = { _, _, _, _ -> },
    actionOnSuccess: (data: T, metadata: ResponseMetadata?) -> Unit = { _, _ -> },
    actionOnFailure: (error: Throwable, reason: ErrorReason, cache: T?, metadata: ResponseMetadata?) -> Unit = { _, _, _, _ -> },
    delay: Long = 500
) {
    LaunchedEffect(this) {
        if (delay > 0) delay(delay.milliseconds)
        when (this@ShowResults) {
            is Response.Success -> actionOnSuccess(this@ShowResults.result, this@ShowResults.metadata)
            is Response.Failure -> actionOnFailure(
                this@ShowResults.error,
                this@ShowResults.reason,
                this@ShowResults.previousData,
                this@ShowResults.metadata
            )
            else -> {}
        }
    }

    when (this) {
        is Response.Success -> successContent(this.result, this.metadata)
        is Response.Loading -> loadingContent(this.previousData, this.metadata)
        is Response.Failure -> failureContent(this.error, this.reason, this.previousData, this.metadata)
    }
}
