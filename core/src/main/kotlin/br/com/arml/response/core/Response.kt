package br.com.arml.response.core

sealed class Response<out T>(
    open val metadata: ResponseMetadata? = null
) {
    data class Success<out T>(
        val result: T,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    data class Failure<out T>(
        val error: Throwable,
        val reason: ErrorReason = ErrorReason.Unknown,
        val previousData: T? = null,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    data class Loading<out T>(
        val previousData: T? = null,
        override val metadata: ResponseMetadata? = null
    ) : Response<T>(metadata)

    inline fun <R> fold(
        onLoading: (cache: T?, metadata: ResponseMetadata?) -> R,
        onSuccess: (data: T, metadata: ResponseMetadata?) -> R,
        onFailure: (error: Throwable, reason: ErrorReason, cache: T?, metadata: ResponseMetadata?) -> R
    ): R = when (this) {
        is Loading -> onLoading(previousData, metadata)
        is Success -> onSuccess(result, metadata)
        is Failure -> onFailure(error, reason, previousData, metadata)
    }

    fun getOrNull(): T? = when (this) {
        is Success -> result
        is Failure -> previousData
        is Loading -> previousData
    }

    fun exceptionOrNull(): Throwable? = (this as? Failure)?.error

    /**
     * Transforms the data inside a [Success] state and also any cached data
     * in [Loading] or [Failure].
     *
     * Metadata is preserved during transformation.
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
     * Executes the [action] if the state is [Success].
     */
    inline fun onSuccess(action: (T) -> Unit): Response<T> {
        if (this is Success) action(result)
        return this
    }

    /**
     * Executes the [action] if the state is [Failure].
     */
    inline fun onFailure(action: (Throwable, ErrorReason, T?) -> Unit): Response<T> {
        if (this is Failure) action(error, reason, previousData)
        return this
    }

    /**
     * Executes the [action] if the state is [Loading].
     */
    inline fun onLoading(action: (T?) -> Unit): Response<T> {
        if (this is Loading) action(previousData)
        return this
    }

    /**
     * Transforms the [Throwable] inside a [Failure] state.
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
     * Attempts to recover from a [Failure] state using its cached data.
     * If there is no cached data (previousData is null), it remains as [Failure].
     */
    fun onRecover(): Response<T> {
        if (this !is Failure) return this
        return previousData?.let { Success(it, metadata) } ?: this
    }

    /**
     * Attempts to recover from a [Failure] state by providing a custom [transform] logic.
     * The transform block receives the [Throwable] and the cached data (T?).
     */
    inline fun onRecover(transform: (error: Throwable, cache: T?) -> @UnsafeVariance T): Response<T> {
        return when (this) {
            is Failure -> Success(transform(error, previousData), metadata)
            else -> this
        }
    }
}
