package com.flagsmith.android.network

interface INetworkListener {
    fun success(response: String?)
    fun failed(error: String?)
}