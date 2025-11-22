package com.world.spring.shared.response

sealed class ApiResponse<out T> {
    abstract val status: Boolean
    abstract val message: String

    data class Success<T>(
        override val status: Boolean = true,
        override val message: String,
        val data: T,
    ) : ApiResponse<T>()

    data class Error<T>(
        override val status: Boolean = false,
        override val message: String,
        val data: T? = null,
    ) : ApiResponse<T>()

    companion object {
        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> {
            return Success(message = message, data = data)
        }

        fun success(message: String = "Success"): ApiResponse<Unit> {
            return Success(message = message, data = Unit)
        }

        fun <T> error(message: String, data: T? = null): ApiResponse<T> {
            return Error(message = message, data = data)
        }
    }
}