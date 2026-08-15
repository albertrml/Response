package br.com.arml.response.core

sealed class Response<out T> {
    data class Success<out T>(val result: T) : Response<T>()
    data class Failure(val exception: Exception) : Response<Nothing>()
    data object Loading : Response<Nothing>()

    inline fun <R> fold(
        onLoading: () -> R,
        onSuccess: (T) -> R,
        onFailure: (Exception) -> R
    ): R = when (this) {
        is Success -> onSuccess(result)
        is Failure -> onFailure(exception)
        is Loading -> onLoading()
    }

    fun getOrNull(): T? = (this as? Success)?.result
    fun exceptionOrNull(): Exception? = (this as? Failure)?.exception

    /**
     * Transforms the data inside a [Success] state.
     */
    inline fun <S> mapTo(transform: (T) -> S): Response<S> {
        return when (this) {
            is Success -> Success(transform(this.result))
            is Failure -> Failure(this.exception)
            is Loading -> Loading
        }
    }
}
