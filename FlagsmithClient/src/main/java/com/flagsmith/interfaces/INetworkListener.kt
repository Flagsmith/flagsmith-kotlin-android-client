package com.flagsmith.interfaces

interface INetworkListener {
    fun success(response: String?)
    fun failed(exception: Exception)
}