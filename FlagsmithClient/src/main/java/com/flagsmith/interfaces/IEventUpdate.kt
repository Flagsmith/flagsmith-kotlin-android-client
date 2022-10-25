package com.flagsmith.interfaces

interface IEventUpdate {
    fun success()
    fun failed(exception: Exception)
}