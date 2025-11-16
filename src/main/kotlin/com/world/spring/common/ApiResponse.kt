package com.world.spring.common

data class ApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): ApiResponse<T> {
            return ApiResponse(status = true, message = message, data = data)
        }

        fun success(message: String = "Success"): ApiResponse<Unit> {
            return ApiResponse(status = true, message = message, data = null)
        }

        fun <T> error(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(status = false, message = message, data = data)
        }

        // Generic convenience for callers who want the library to pick [] vs {} based on reified T
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> errorTyped(message: String = "Error"): ApiResponse<T> {
            return when {
                Collection::class.java.isAssignableFrom(T::class.java) ->
                    ApiResponse(status = false, message = message, data = emptyList<Any>() as T)

                Map::class.java.isAssignableFrom(T::class.java) ->
                    ApiResponse(status = false, message = message, data = emptyMap<String, Any>() as T)

                else ->
                    ApiResponse(status = false, message = message, data = null)
            }
        }

    }
}