package com.blu.flagsmith.util

sealed class ResultEntity<out R> {

    data class Success<out T>(val data: T) : ResultEntity<T>()
    data class Error(var error: ErrorEntity) : ResultEntity<Nothing>()
    object Loading : ResultEntity<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[error=${error.message}]"
            Loading -> "Loading"
        }
    }
}
