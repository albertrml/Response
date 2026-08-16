package br.com.arml.response.core

sealed class Response<out T> {
    data class Success<out T>(val result: T) : Response<T>()
    
    data class Failure<out T>(
        val error: Throwable,
        val reason: ErrorReason = ErrorReason.Unknown,
        val previousData: T? = null
    ) : Response<T>()
    
    data class Loading<out T>(val previousData: T? = null) : Response<T>()

    inline fun <R> fold(
        onLoading: (T?) -> R,
        onSuccess: (T) -> R,
        onFailure: (Throwable, ErrorReason, T?) -> R
    ): R = when (this) {
        is Success -> onSuccess(result)
        is Failure -> onFailure(error, reason, previousData)
        is Loading -> onLoading(previousData)
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
     */
    inline fun <S> mapTo(transform: (T) -> S): Response<S> {
        return when (this) {
            is Success -> Success(transform(result))
            is Failure -> Failure(
                error = error,
                reason = reason,
                previousData = previousData?.let(transform)
            )
            is Loading -> Loading(previousData = previousData?.let(transform))
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
                previousData = previousData
            )
        } else this
    }

    /**
     * Attempts to recover from a [Failure] state using its cached data.
     * If there is no cached data (previousData is null), it remains as [Failure].
     */
    fun onRecover(): Response<T> {
        if (this !is Failure) return this
        return previousData?.let { Success(it) } ?: this
    }

    /**
     * Attempts to recover from a [Failure] state by providing a custom [transform] logic.
     * The transform block receives the [Throwable] and the cached data (T?).
     */
    inline fun onRecover(transform: (error: Throwable, cache: T?) -> @UnsafeVariance T): Response<T> {
        return when(this){
            is Failure -> Success(transform(error, previousData))
            else -> this
        }
    }
}
