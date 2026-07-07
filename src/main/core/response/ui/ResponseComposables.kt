package br.com.arml.core.response.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.arml.core.response.Response
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
    loadingContent: @Composable () -> Unit = {},
    failureContent: @Composable (Exception) -> Unit = {},
    actionOnSuccess: (T) -> Unit = {},
    actionOnFailure: (Exception) -> Unit = {},
    delay: Long = 500
) {
    LaunchedEffect(this) {
        if (delay > 0) delay(delay.milliseconds)
        when (this@ShowResults) {
            is Response.Success -> actionOnSuccess(this@ShowResults.result)
            is Response.Failure -> actionOnFailure(this@ShowResults.exception)
            else -> {}
        }
    }

    when (this) {
        is Response.Success -> successContent(this.result)
        is Response.Loading -> loadingContent()
        is Response.Failure -> failureContent(this.exception)
    }
}
