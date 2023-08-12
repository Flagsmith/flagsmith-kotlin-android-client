package com.blu.flagsmith.util

import android.util.Log

open class BaseRemoteDataSource(private val errorHandler: ErrorHandler) {

    suspend fun <T> safeRequest(apiCall: suspend () -> T): ResultEntity<T> {
        return try {
            ResultEntity.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            errorHandler.getError(throwable).let {
                Log.e("FlagSmith","Error Code: " + it.code)
                Log.e("FlagSmith","Error Message: " + it.message)
                ResultEntity.Error(it)
            }
        }
    }
}
