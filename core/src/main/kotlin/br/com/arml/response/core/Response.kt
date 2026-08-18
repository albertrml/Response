package br.com.arml.response.core

/**
 * A sealed class representing the state of an asynchronous operation.
 * It follows the "State Recovery" principle, where [Loading] and [Failure] can carry previousData.
 *
 * @param metadata Contextual information about the operation (timestamp, policies, etc.).
 */
sealed class Response<out T>(
    open val metadata: ResponseMetadata? = null
) {
    /**
     * Represents a successful operation.
     * Decisions: Metadata is mandatory via builders to ensure traceability (timestamping).
     */
    data class Success<out T>(
        val result: T,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    /**
     * Represents a failed operation.
     * Decisions: Uses [Throwable] instead of Exception for full JVM compatibility and captures [ErrorReason]
     * for semantic UI decision-making.
     */
    data class Failure<out T>(
        val error: Throwable,
        val reason: ErrorReason = ErrorReason.Unknown,
        val previousData: T? = null,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    /**
     * Represents an operation in progress.
     * Decisions: Can carry [previousData] to avoid UI flickering during refreshes (Stale-While-Revalidate).
     */
    data class Loading<out T>(
        val previousData: T? = null,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    /**
     * Exhaustive state handler.
     * Decisions: Exposes metadata to all branches to allow UI to react to data age or pagination.
     */
    inline fun <R> fold(
        onLoading: (cache: T?, metadata: ResponseMetadata?) -> R,
        onSuccess: (data: T, metadata: ResponseMetadata?) -> R,
        onFailure: (error: Throwable, reason: ErrorReason, cache: T?, metadata: ResponseMetadata?) -> R
    ): R = when (this) {
        is Loading -> onLoading(previousData, metadata)
        is Success -> onSuccess(result, metadata)
        is Failure -> onFailure(error, reason, previousData, metadata)
    }

    /**
     * Returns the data if Success, or the cached data if Loading/Failure.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> result
        is Failure -> previousData
        is Loading -> previousData
    }

    /**
     * Helper to extract the error from a Failure state.
     */
    fun exceptionOrNull(): Throwable? = (this as? Failure)?.error

    /**
     * Transforms the data [T] into [S] across all states. Metadata is preserved during transformation.
     * Decisions: Metadata is strictly preserved to maintain original capture context (timestamp/policies).
     */
    inline fun <S> mapTo(transform: (T) -> S): Response<S> {
        return when (this) {
            is Success -> Success(
                result = transform(result),
                metadata = metadata
            )
            is Failure -> Failure(
                error = error,
                reason = reason,
                previousData = previousData?.let(transform),
                metadata = metadata
            )
            is Loading -> Loading(
                previousData = previousData?.let(transform),
                metadata = metadata
            )
        }
    }

    /**
     * Side effect: Executes [action] only on [Success].
     */
    inline fun onSuccess(action: (T) -> Unit): Response<T> {
        if (this is Success) action(result)
        return this
    }

    /**
     * Side effect: Executes [action] only on [Failure].
     */
    inline fun onFailure(action: (Throwable, ErrorReason, T?) -> Unit): Response<T> {
        if (this is Failure) action(error, reason, previousData)
        return this
    }

    /**
     * Side effect: Executes [action] only on [Loading].
     */
    inline fun onLoading(action: (T?) -> Unit): Response<T> {
        if (this is Loading) action(previousData)
        return this
    }

    /**
     * Functional Transformation: Changes the error inside [Failure].
     */
    inline fun mapError(transform: (Throwable) -> Throwable): Response<T> {
        return if (this is Failure) {
            Failure(
                error = transform(error),
                reason = reason,
                previousData = previousData,
                metadata = metadata
            )
        } else this
    }

    /**
     * Recovery: Automatically converts [Failure] to [Success] if previousData exists.
     * Decisions: Prioritizes UX continuity over showing an error screen.
     */
    fun onRecover(): Response<T> {
        if (this !is Failure) return this
        return previousData?.let { Success(it, metadata) } ?: this
    }

    /**
     * Recovery: Converts [Failure] to [Success] using a custom [transform] lambda.
     * Decisions: Provides access to both error and cache for informed recovery.
     */
    inline fun onRecover(transform: (error: Throwable, cache: T?) -> @UnsafeVariance T): Response<T> {
        return when (this) {
            is Failure -> Success(transform(error, previousData), metadata)
            else -> this
        }
    }
}
