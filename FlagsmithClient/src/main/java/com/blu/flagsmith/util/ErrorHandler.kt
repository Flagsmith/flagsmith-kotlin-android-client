package com.blu.flagsmith.util

interface ErrorHandler {
    fun getError(throwable: Throwable): ErrorEntity
}